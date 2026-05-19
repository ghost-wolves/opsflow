package com.opsflow.ticket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class SlaBreachUpdater {

    private final TicketRepository ticketRepository;
    private final Clock clock;

    @Autowired
    public SlaBreachUpdater(TicketRepository ticketRepository) {
        this(ticketRepository, Clock.systemDefaultZone());
    }

    SlaBreachUpdater(TicketRepository ticketRepository, Clock clock) {
        this.ticketRepository = ticketRepository;
        this.clock = clock;
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    @Transactional
    public void markOverdueTicketsBreached() {
        OffsetDateTime now = OffsetDateTime.now(clock);

        List<Ticket> overdueTickets = ticketRepository.findActiveOverdueTickets(
                now,
                TicketStatus.RESOLVED,
                TicketStatus.CLOSED
        );

        overdueTickets.forEach(Ticket::markSlaBreached);

        ticketRepository.saveAll(overdueTickets);
    }
}
