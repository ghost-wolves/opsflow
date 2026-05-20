package com.opsflow.dashboard;

import com.opsflow.ticket.Ticket;
import com.opsflow.ticket.TicketRepository;
import com.opsflow.ticket.TicketStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ManagerDashboardService {

    private static final long DUE_SOON_HOURS = 4;

    private final TicketRepository ticketRepository;
    private final Clock clock;

    @Autowired
    public ManagerDashboardService(TicketRepository ticketRepository) {
        this(ticketRepository, Clock.systemDefaultZone());
    }

    ManagerDashboardService(TicketRepository ticketRepository, Clock clock) {
        this.ticketRepository = ticketRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ManagerDashboardResponse getDashboard() {
        List<Ticket> tickets = ticketRepository.findAll();

        return new ManagerDashboardResponse(
                tickets.size(),
                countByStatus(tickets, TicketStatus.NEW),
                countByStatus(tickets, TicketStatus.TRIAGED),
                countByStatus(tickets, TicketStatus.ASSIGNED),
                countByStatus(tickets, TicketStatus.IN_PROGRESS),
                countByStatus(tickets, TicketStatus.WAITING_ON_USER),
                countByStatus(tickets, TicketStatus.RESOLVED),
                countByStatus(tickets, TicketStatus.CLOSED),
                countByStatus(tickets, TicketStatus.REOPENED),
                tickets.stream().filter(ticket -> ticket.getAssignedTo() == null).count(),
                tickets.stream().filter(this::isOverdue).count(),
                tickets.stream().filter(this::isDueSoon).count(),
                tickets.stream().filter(Ticket::isSlaBreached).count()
        );
    }

    private long countByStatus(List<Ticket> tickets, TicketStatus status) {
        return tickets.stream()
                .filter(ticket -> ticket.getStatus() == status)
                .count();
    }

    private boolean isClosedOrResolved(Ticket ticket) {
        return ticket.getStatus() == TicketStatus.CLOSED
                || ticket.getStatus() == TicketStatus.RESOLVED;
    }

    private boolean isOverdue(Ticket ticket) {
        return !isClosedOrResolved(ticket)
                && ticket.getSlaDueAt().isBefore(OffsetDateTime.now(clock));
    }

    private boolean isDueSoon(Ticket ticket) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        OffsetDateTime dueSoonCutoff = now.plusHours(DUE_SOON_HOURS);

        return !isClosedOrResolved(ticket)
                && !ticket.getSlaDueAt().isBefore(now)
                && !ticket.getSlaDueAt().isAfter(dueSoonCutoff);
    }
}
