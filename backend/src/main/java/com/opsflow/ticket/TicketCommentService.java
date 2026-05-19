package com.opsflow.ticket;

import com.opsflow.user.AppUser;
import com.opsflow.user.AppUserRepository;
import com.opsflow.user.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TicketCommentService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final AppUserRepository appUserRepository;
    private final TicketAuditService ticketAuditService;

    public TicketCommentService(
            TicketRepository ticketRepository,
            TicketCommentRepository ticketCommentRepository,
            AppUserRepository appUserRepository,
            TicketAuditService ticketAuditService
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketCommentRepository = ticketCommentRepository;
        this.appUserRepository = appUserRepository;
        this.ticketAuditService = ticketAuditService;
    }

    @Transactional
    public TicketCommentResponse addComment(
            Long ticketId,
            CreateTicketCommentRequest request,
            String authorEmail
    ) {
        AppUser author = findEnabledUserByEmail(authorEmail);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found."));

        if (!canViewTicket(author, ticket)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this ticket.");
        }

        if (request.internal() && hasRole(author, "REQUESTER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Requesters cannot create internal comments.");
        }

        TicketComment comment = new TicketComment(
                ticket,
                author,
                request.body(),
                request.internal()
        );

        TicketComment savedComment = ticketCommentRepository.save(comment);

        ticketAuditService.record(
                ticket,
                author,
                TicketAuditEventType.COMMENT_ADDED,
                null,
                savedComment.isInternal() ? "INTERNAL" : "PUBLIC",
                "Comment added by " + author.getDisplayName() + "."
        );

        return TicketCommentResponse.from(savedComment);
    }

    @Transactional(readOnly = true)
    public List<TicketCommentResponse> listComments(Long ticketId, String userEmail) {
        AppUser user = findEnabledUserByEmail(userEmail);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found."));

        if (!canViewTicket(user, ticket)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this ticket.");
        }

        return ticketCommentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .filter(comment -> !comment.isInternal() || hasRole(user, "ANALYST") || hasRole(user, "MANAGER"))
                .map(TicketCommentResponse::from)
                .toList();
    }

    private boolean canViewTicket(AppUser user, Ticket ticket) {
        if (hasRole(user, "MANAGER")) {
            return true;
        }

        if (hasRole(user, "ANALYST")) {
            return ticket.getAssignedTo() == null
                    || ticket.getAssignedTo().getEmail().equalsIgnoreCase(user.getEmail());
        }

        return ticket.getRequester().getEmail().equalsIgnoreCase(user.getEmail());
    }

    private AppUser findEnabledUserByEmail(String email) {
        return appUserRepository.findByEmailIgnoreCase(email)
                .filter(AppUser::isEnabled)
                .orElseThrow(() -> new BadCredentialsException("User not found."));
    }

    private boolean hasRole(AppUser user, String roleName) {
        return user.getRoles()
                .stream()
                .map(Role::getName)
                .anyMatch(roleName::equals);
    }
}
