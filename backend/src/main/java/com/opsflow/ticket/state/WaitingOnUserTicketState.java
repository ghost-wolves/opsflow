package com.opsflow.ticket.state;

import com.opsflow.ticket.TicketStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class WaitingOnUserTicketState implements TicketStatusState {

    @Override
    public TicketStatus status() {
        return TicketStatus.WAITING_ON_USER;
    }

    @Override
    public Set<TicketStatus> allowedTransitions() {
        return Set.of(TicketStatus.IN_PROGRESS);
    }
}
