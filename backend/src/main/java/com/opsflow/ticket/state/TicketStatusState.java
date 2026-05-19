package com.opsflow.ticket.state;

import com.opsflow.ticket.TicketStatus;

import java.util.Set;

public interface TicketStatusState {

    TicketStatus status();

    Set<TicketStatus> allowedTransitions();

    default boolean canTransitionTo(TicketStatus nextStatus) {
        return allowedTransitions().contains(nextStatus);
    }
}
