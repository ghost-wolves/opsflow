package com.opsflow.ticket;

import com.opsflow.user.AppUser;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class SlaRiskCalculationServiceTest {

    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2026-05-19T12:00:00Z"),
            ZoneOffset.UTC
    );

    private final SlaRiskCalculationService service = new SlaRiskCalculationService(fixedClock);

    @Test
    void breachedTicketReturnsBreachedRisk() {
        Ticket ticket = createTicket(TicketStatus.IN_PROGRESS, "2026-05-19T16:00:00Z");
        ticket.markSlaBreached();

        assertEquals(SlaRisk.BREACHED, service.calculate(ticket));
    }

    @Test
    void resolvedTicketReturnsCompletedRisk() {
        Ticket ticket = createTicket(TicketStatus.RESOLVED, "2026-05-19T10:00:00Z");

        assertEquals(SlaRisk.COMPLETED, service.calculate(ticket));
    }

    @Test
    void closedTicketReturnsCompletedRisk() {
        Ticket ticket = createTicket(TicketStatus.CLOSED, "2026-05-19T10:00:00Z");

        assertEquals(SlaRisk.COMPLETED, service.calculate(ticket));
    }

    @Test
    void overdueTicketReturnsOverdueRisk() {
        Ticket ticket = createTicket(TicketStatus.IN_PROGRESS, "2026-05-19T11:59:00Z");

        assertEquals(SlaRisk.OVERDUE, service.calculate(ticket));
    }

    @Test
    void ticketDueWithinFourHoursReturnsDueSoonRisk() {
        Ticket ticket = createTicket(TicketStatus.IN_PROGRESS, "2026-05-19T15:00:00Z");

        assertEquals(SlaRisk.DUE_SOON, service.calculate(ticket));
    }

    @Test
    void ticketDueAfterFourHoursReturnsOnTrackRisk() {
        Ticket ticket = createTicket(TicketStatus.IN_PROGRESS, "2026-05-19T18:00:00Z");

        assertEquals(SlaRisk.ON_TRACK, service.calculate(ticket));
    }

    @Test
    void nullTicketThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate(null));
    }

    private Ticket createTicket(TicketStatus status, String slaDueAt) {
        return new Ticket(
                "OPS-2026-7777",
                "SLA risk test ticket",
                "Testing SLA risk calculation.",
                "OpsFlow",
                Impact.MEDIUM,
                Urgency.MEDIUM,
                Priority.P3,
                status,
                mock(AppUser.class),
                OffsetDateTime.parse(slaDueAt)
        );
    }
}
