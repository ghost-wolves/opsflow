package com.opsflow.dashboard;

import com.opsflow.ticket.*;
import com.opsflow.user.AppUser;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ManagerDashboardServiceTest {

    private final TicketRepository ticketRepository = mock(TicketRepository.class);

    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2026-05-19T12:00:00Z"),
            ZoneOffset.UTC
    );

    private final ManagerDashboardService service = new ManagerDashboardService(
            ticketRepository,
            fixedClock
    );

    @Test
    void dashboardCountsTicketsByStatusAssignmentAndSlaRisk() {
        AppUser requester = mock(AppUser.class);

        Ticket newTicket = createTicket("OPS-2026-9001", TicketStatus.NEW, requester, "2026-05-19T10:00:00Z");
        Ticket triagedTicket = createTicket("OPS-2026-9002", TicketStatus.TRIAGED, requester, "2026-05-19T18:00:00Z");

        Ticket assignedTicket = createTicket("OPS-2026-9003", TicketStatus.ASSIGNED, requester, "2026-05-19T15:00:00Z");
        assignedTicket.assignTo(mock(AppUser.class));

        Ticket inProgressTicket = createTicket("OPS-2026-9004", TicketStatus.IN_PROGRESS, requester, "2026-05-19T09:00:00Z");
        inProgressTicket.markSlaBreached();

        Ticket waitingTicket = createTicket("OPS-2026-9005", TicketStatus.WAITING_ON_USER, requester, "2026-05-19T20:00:00Z");
        Ticket resolvedTicket = createTicket("OPS-2026-9006", TicketStatus.RESOLVED, requester, "2026-05-19T08:00:00Z");
        Ticket closedTicket = createTicket("OPS-2026-9007", TicketStatus.CLOSED, requester, "2026-05-19T08:00:00Z");
        Ticket reopenedTicket = createTicket("OPS-2026-9008", TicketStatus.REOPENED, requester, "2026-05-19T14:00:00Z");

        when(ticketRepository.findAll()).thenReturn(List.of(
                newTicket,
                triagedTicket,
                assignedTicket,
                inProgressTicket,
                waitingTicket,
                resolvedTicket,
                closedTicket,
                reopenedTicket
        ));

        ManagerDashboardResponse response = service.getDashboard();

        assertEquals(8, response.totalTickets());
        assertEquals(1, response.newTickets());
        assertEquals(1, response.triagedTickets());
        assertEquals(1, response.assignedTickets());
        assertEquals(1, response.inProgressTickets());
        assertEquals(1, response.waitingOnUserTickets());
        assertEquals(1, response.resolvedTickets());
        assertEquals(1, response.closedTickets());
        assertEquals(1, response.reopenedTickets());
        assertEquals(7, response.unassignedTickets());
        assertEquals(2, response.overdueTickets());
        assertEquals(2, response.dueSoonTickets());
        assertEquals(1, response.breachedSlaTickets());
    }

    private Ticket createTicket(String ticketNumber, TicketStatus status, AppUser requester, String slaDueAt) {
        return new Ticket(
                ticketNumber,
                "Dashboard test ticket",
                "Dashboard test description",
                "OpsFlow",
                Impact.MEDIUM,
                Urgency.MEDIUM,
                Priority.P3,
                status,
                requester,
                OffsetDateTime.parse(slaDueAt)
        );
    }
}
