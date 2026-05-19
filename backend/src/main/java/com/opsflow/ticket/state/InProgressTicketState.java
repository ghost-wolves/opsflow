package com.opsflow.ticket.state;

import com.opsflow.ticket.TicketStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class InProgressTicketState implements TicketStatusState {

    @Override
    public TicketStatus status() {
        return TicketStatus.IN_PROGRESS;
    }

    @Override
    public Set<TicketStatus> allowedTransitions() {
        return Set.of(TicketStatus.WAITING_ON_USER, TicketStatus.RESOLVED);
    }
}
