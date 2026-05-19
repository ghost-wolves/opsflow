package com.opsflow.ticket;

import com.opsflow.user.AppUser;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class TicketStatusTimestampTest {

    @Test
    void resolvedStatusSetsResolvedAt() {
        Ticket ticket = createTicket(TicketStatus.IN_PROGRESS);
        OffsetDateTime resolvedAt = OffsetDateTime.parse("2026-05-19T12:00:00Z");

        ticket.applyStatus(TicketStatus.RESOLVED, resolvedAt);

        assertEquals(TicketStatus.RESOLVED, ticket.getStatus());
        assertEquals(resolvedAt, ticket.getResolvedAt());
        assertNull(ticket.getClosedAt());
    }

    @Test
    void closedStatusSetsClosedAt() {
        Ticket ticket = createTicket(TicketStatus.RESOLVED);
        OffsetDateTime closedAt = OffsetDateTime.parse("2026-05-19T13:00:00Z");

        ticket.applyStatus(TicketStatus.CLOSED, closedAt);

        assertEquals(TicketStatus.CLOSED, ticket.getStatus());
        assertEquals(closedAt, ticket.getClosedAt());
    }

    @Test
    void reopenedStatusClearsClosedAt() {
        Ticket ticket = createTicket(TicketStatus.RESOLVED);
        OffsetDateTime closedAt = OffsetDateTime.parse("2026-05-19T13:00:00Z");
        OffsetDateTime reopenedAt = OffsetDateTime.parse("2026-05-19T14:00:00Z");

        ticket.applyStatus(TicketStatus.CLOSED, closedAt);
        assertEquals(closedAt, ticket.getClosedAt());

        ticket.applyStatus(TicketStatus.REOPENED, reopenedAt);

        assertEquals(TicketStatus.REOPENED, ticket.getStatus());
        assertNull(ticket.getClosedAt());
    }

    private Ticket createTicket(TicketStatus status) {
        return new Ticket(
                "OPS-2026-9999",
                "Test ticket",
                "Test description",
                "Test System",
                Impact.MEDIUM,
                Urgency.MEDIUM,
                Priority.P3,
                status,
                mock(AppUser.class),
                OffsetDateTime.parse("2026-05-20T12:00:00Z")
        );
    }
}
