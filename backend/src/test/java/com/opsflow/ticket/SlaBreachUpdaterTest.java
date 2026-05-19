package com.opsflow.ticket;

import com.opsflow.user.AppUser;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class SlaBreachUpdaterTest {

    private final TicketRepository ticketRepository = mock(TicketRepository.class);

    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2026-05-19T12:00:00Z"),
            ZoneOffset.UTC
    );

    private final SlaBreachUpdater updater = new SlaBreachUpdater(ticketRepository, fixedClock);

    @Test
    void marksActiveOverdueTicketsAsBreached() {
        Ticket overdueTicket = new Ticket(
                "OPS-2026-8888",
                "Overdue ticket",
                "Testing SLA breach updater.",
                "OpsFlow",
                Impact.HIGH,
                Urgency.HIGH,
                Priority.P1,
                TicketStatus.IN_PROGRESS,
                mock(AppUser.class),
                OffsetDateTime.parse("2026-05-19T10:00:00Z")
        );

        when(ticketRepository.findActiveOverdueTickets(
                OffsetDateTime.parse("2026-05-19T12:00:00Z"),
                TicketStatus.RESOLVED,
                TicketStatus.CLOSED
        )).thenReturn(List.of(overdueTicket));

        updater.markOverdueTicketsBreached();

        assertTrue(overdueTicket.isSlaBreached());
        verify(ticketRepository).saveAll(List.of(overdueTicket));
    }
}
