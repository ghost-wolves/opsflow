package com.opsflow.ticket;

import com.opsflow.user.AppUser;
import com.opsflow.user.AppUserRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TicketAuditServiceTest {

    private final TicketAuditEventRepository auditEventRepository = mock(TicketAuditEventRepository.class);
    private final TicketRepository ticketRepository = mock(TicketRepository.class);
    private final AppUserRepository appUserRepository = mock(AppUserRepository.class);

    private final TicketAuditService service = new TicketAuditService(
            auditEventRepository,
            ticketRepository,
            appUserRepository
    );

    @Test
    void recordCreatesAuditEvent() {
        Ticket ticket = mock(Ticket.class);
        AppUser actor = mock(AppUser.class);

        when(auditEventRepository.save(any(TicketAuditEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TicketAuditEvent event = service.record(
                ticket,
                actor,
                TicketAuditEventType.STATUS_CHANGED,
                "NEW",
                "TRIAGED",
                "Status changed from NEW to TRIAGED."
        );

        assertEquals(ticket, event.getTicket());
        assertEquals(actor, event.getActor());
        assertEquals(TicketAuditEventType.STATUS_CHANGED, event.getEventType());
        assertEquals("NEW", event.getOldValue());
        assertEquals("TRIAGED", event.getNewValue());
        assertEquals("Status changed from NEW to TRIAGED.", event.getMessage());

        verify(auditEventRepository).save(any(TicketAuditEvent.class));
    }
}
