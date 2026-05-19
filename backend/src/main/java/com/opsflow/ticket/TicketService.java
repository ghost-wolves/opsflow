package com.opsflow.ticket;

import com.opsflow.user.AppUser;
import com.opsflow.user.AppUserRepository;
import com.opsflow.user.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AppUserRepository appUserRepository;
    private final TicketNumberGenerator ticketNumberGenerator;
    private final PriorityCalculationService priorityCalculationService;
    private final SlaCalculationService slaCalculationService;
    private final TicketStatusTransitionService ticketStatusTransitionService;

    public TicketService(
            TicketRepository ticketRepository,
            AppUserRepository appUserRepository,
            TicketNumberGenerator ticketNumberGenerator,
            PriorityCalculationService priorityCalculationService,
            SlaCalculationService slaCalculationService,
            TicketStatusTransitionService ticketStatusTransitionService
    ) {
        this.ticketRepository = ticketRepository;
        this.appUserRepository = appUserRepository;
        this.ticketNumberGenerator = ticketNumberGenerator;
        this.priorityCalculationService = priorityCalculationService;
        this.slaCalculationService = slaCalculationService;
        this.ticketStatusTransitionService = ticketStatusTransitionService;
    }

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, String requesterEmail) {
        AppUser requester = findEnabledUserByEmail(requesterEmail);

        long nextSequence = ticketRepository.count() + 1;
        String ticketNumber = ticketNumberGenerator.generate(nextSequence);

        Priority priority = priorityCalculationService.calculate(request.impact(), request.urgency());

        Ticket ticket = new Ticket(
                ticketNumber,
                request.title(),
                request.description(),
                request.affectedSystem(),
                request.impact(),
                request.urgency(),
                priority,
                TicketStatus.NEW,
                requester,
                slaCalculationService.calculateDueAt(priority)
        );

        Ticket savedTicket = ticketRepository.save(ticket);

        return TicketResponse.from(savedTicket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> listTicketsForUser(String email) {
        AppUser user = findEnabledUserByEmail(email);

        List<Ticket> tickets;

        if (hasRole(user, "MANAGER")) {
            tickets = ticketRepository.findAllByOrderByCreatedAtDesc();
        } else if (hasRole(user, "ANALYST")) {
            tickets = ticketRepository.findVisibleToAnalyst(user.getEmail());
        } else {
            tickets = ticketRepository.findByRequesterEmailIgnoreCaseOrderByCreatedAtDesc(user.getEmail());
        }

        return tickets.stream()
                .sorted(Comparator.comparing(Ticket::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(TicketResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketForUser(Long ticketId, String email) {
        AppUser user = findEnabledUserByEmail(email);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found."));

        if (!canViewTicket(user, ticket)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this ticket.");
        }

        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse updateTicketStatus(Long ticketId, UpdateTicketStatusRequest request, String email) {
        AppUser user = findEnabledUserByEmail(email);

        if (!hasRole(user, "ANALYST") && !hasRole(user, "MANAGER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only analysts and managers can update ticket status.");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found."));

        if (!canViewTicket(user, ticket)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this ticket.");
        }

        ticketStatusTransitionService.validateTransition(ticket.getStatus(), request.status());
        ticket.changeStatus(request.status());

        return TicketResponse.from(ticketRepository.save(ticket));
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
