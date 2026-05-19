package com.opsflow.ticket;

import com.opsflow.user.AppUser;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class TicketAssignmentStatusTest {

    @Test
    void assigningNewTicketCanMoveStatusToAssigned() {
        Ticket ticket = createTicket(TicketStatus.NEW);
        AppUser analyst = mock(AppUser.class);

        ticket.assignTo(analyst);
        ticket.changeStatus(TicketStatus.ASSIGNED);

        assertEquals(analyst, ticket.getAssignedTo());
        assertEquals(TicketStatus.ASSIGNED, ticket.getStatus());
    }

    @Test
    void assigningTriagedTicketCanMoveStatusToAssigned() {
        Ticket ticket = createTicket(TicketStatus.TRIAGED);
        AppUser analyst = mock(AppUser.class);

        ticket.assignTo(analyst);
        ticket.changeStatus(TicketStatus.ASSIGNED);

        assertEquals(analyst, ticket.getAssignedTo());
        assertEquals(TicketStatus.ASSIGNED, ticket.getStatus());
    }

    private Ticket createTicket(TicketStatus status) {
        return new Ticket(
                "OPS-2026-9998",
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
