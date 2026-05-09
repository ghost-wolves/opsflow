package com.opsflow.ticket;

import com.opsflow.user.AppUser;
import com.opsflow.user.AppUserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AppUserRepository appUserRepository;
    private final TicketNumberGenerator ticketNumberGenerator;
    private final PriorityCalculationService priorityCalculationService;
    private final SlaCalculationService slaCalculationService;

    public TicketService(
            TicketRepository ticketRepository,
            AppUserRepository appUserRepository,
            TicketNumberGenerator ticketNumberGenerator,
            PriorityCalculationService priorityCalculationService,
            SlaCalculationService slaCalculationService
    ) {
        this.ticketRepository = ticketRepository;
        this.appUserRepository = appUserRepository;
        this.ticketNumberGenerator = ticketNumberGenerator;
        this.priorityCalculationService = priorityCalculationService;
        this.slaCalculationService = slaCalculationService;
    }

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, String requesterEmail) {
        AppUser requester = appUserRepository.findByEmailIgnoreCase(requesterEmail)
                .filter(AppUser::isEnabled)
                .orElseThrow(() -> new BadCredentialsException("Requester not found."));

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
}
