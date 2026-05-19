package com.opsflow.ticket.state;

import com.opsflow.ticket.TicketStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ResolvedTicketState implements TicketStatusState {

    @Override
    public TicketStatus status() {
        return TicketStatus.RESOLVED;
    }

    @Override
    public Set<TicketStatus> allowedTransitions() {
        return Set.of(TicketStatus.CLOSED, TicketStatus.REOPENED);
    }
}
