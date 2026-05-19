package com.opsflow.ticket;

import com.opsflow.user.AppUser;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TicketResponseTest {

    @Test
    void ticketResponseIncludesSlaRisk() {
        AppUser requester = mock(AppUser.class);

        when(requester.getId()).thenReturn(1L);
        when(requester.getEmail()).thenReturn("requester@opsflow.demo");
        when(requester.getDisplayName()).thenReturn("Riley Requester");

        Ticket ticket = new Ticket(
                "OPS-2026-5555",
                "SLA risk response test",
                "Testing SLA risk in ticket API responses.",
                "OpsFlow",
                Impact.MEDIUM,
                Urgency.HIGH,
                Priority.P2,
                TicketStatus.IN_PROGRESS,
                requester,
                OffsetDateTime.parse("2026-05-19T16:00:00Z")
        );

        TicketResponse response = TicketResponse.from(ticket, SlaRisk.DUE_SOON);

        assertEquals("OPS-2026-5555", response.ticketNumber());
        assertEquals(SlaRisk.DUE_SOON, response.slaRisk());
    }
}
