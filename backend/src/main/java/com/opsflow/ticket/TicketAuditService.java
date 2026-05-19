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
public class TicketAuditService {

    private final TicketAuditEventRepository ticketAuditEventRepository;
    private final TicketRepository ticketRepository;
    private final AppUserRepository appUserRepository;

    public TicketAuditService(
            TicketAuditEventRepository ticketAuditEventRepository,
            TicketRepository ticketRepository,
            AppUserRepository appUserRepository
    ) {
        this.ticketAuditEventRepository = ticketAuditEventRepository;
        this.ticketRepository = ticketRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public TicketAuditEvent record(
            Ticket ticket,
            AppUser actor,
            TicketAuditEventType eventType,
            String oldValue,
            String newValue,
            String message
    ) {
        TicketAuditEvent event = new TicketAuditEvent(
                ticket,
                actor,
                eventType,
                oldValue,
                newValue,
                message
        );

        return ticketAuditEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<TicketAuditEventResponse> listAuditEvents(Long ticketId, String userEmail) {
        AppUser user = findEnabledUserByEmail(userEmail);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found."));

        if (!canViewTicket(user, ticket)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this ticket.");
        }

        return ticketAuditEventRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(TicketAuditEventResponse::from)
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
