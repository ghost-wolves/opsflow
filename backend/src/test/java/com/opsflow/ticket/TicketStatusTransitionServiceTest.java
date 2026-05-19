package com.opsflow.ticket;

import com.opsflow.ticket.state.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TicketStatusTransitionServiceTest {

    private final TicketStatusTransitionService service = new TicketStatusTransitionService(List.of(
            new NewTicketState(),
            new TriagedTicketState(),
            new AssignedTicketState(),
            new InProgressTicketState(),
            new WaitingOnUserTicketState(),
            new ResolvedTicketState(),
            new ClosedTicketState(),
            new ReopenedTicketState()
    ));

    @Test
    void validTransitionsAreAllowed() {
        assertTrue(service.canTransition(TicketStatus.NEW, TicketStatus.TRIAGED));
        assertTrue(service.canTransition(TicketStatus.TRIAGED, TicketStatus.ASSIGNED));
        assertTrue(service.canTransition(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS));
        assertTrue(service.canTransition(TicketStatus.IN_PROGRESS, TicketStatus.WAITING_ON_USER));
        assertTrue(service.canTransition(TicketStatus.WAITING_ON_USER, TicketStatus.IN_PROGRESS));
        assertTrue(service.canTransition(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED));
        assertTrue(service.canTransition(TicketStatus.RESOLVED, TicketStatus.CLOSED));
        assertTrue(service.canTransition(TicketStatus.RESOLVED, TicketStatus.REOPENED));
        assertTrue(service.canTransition(TicketStatus.CLOSED, TicketStatus.REOPENED));
        assertTrue(service.canTransition(TicketStatus.REOPENED, TicketStatus.IN_PROGRESS));
    }

    @Test
    void invalidTransitionsAreRejected() {
        assertFalse(service.canTransition(TicketStatus.NEW, TicketStatus.IN_PROGRESS));
        assertFalse(service.canTransition(TicketStatus.NEW, TicketStatus.CLOSED));
        assertFalse(service.canTransition(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS));
        assertFalse(service.canTransition(TicketStatus.ASSIGNED, TicketStatus.CLOSED));
        assertFalse(service.canTransition(TicketStatus.RESOLVED, TicketStatus.IN_PROGRESS));
    }

    @Test
    void validateTransitionDoesNotThrowForValidTransition() {
        assertDoesNotThrow(() -> service.validateTransition(TicketStatus.NEW, TicketStatus.TRIAGED));
    }

    @Test
    void validateTransitionThrowsForInvalidTransition() {
        assertThrows(
                IllegalStateException.class,
                () -> service.validateTransition(TicketStatus.NEW, TicketStatus.CLOSED)
        );
    }

    @Test
    void nullStatusesAreRejected() {
        assertFalse(service.canTransition(null, TicketStatus.NEW));
        assertFalse(service.canTransition(TicketStatus.NEW, null));
    }
}
