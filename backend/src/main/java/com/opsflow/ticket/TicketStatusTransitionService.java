package com.opsflow.ticket;

import com.opsflow.ticket.state.TicketStatusState;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketStatusTransitionService {

    private final Map<TicketStatus, TicketStatusState> statesByStatus;

    public TicketStatusTransitionService(List<TicketStatusState> states) {
        this.statesByStatus = new EnumMap<>(TicketStatus.class);

        for (TicketStatusState state : states) {
            this.statesByStatus.put(state.status(), state);
        }
    }

    public boolean canTransition(TicketStatus from, TicketStatus to) {
        if (from == null || to == null) {
            return false;
        }

        TicketStatusState state = statesByStatus.get(from);

        return state != null && state.canTransitionTo(to);
    }

    public void validateTransition(TicketStatus from, TicketStatus to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateException("Invalid ticket status transition from " + from + " to " + to + ".");
        }
    }
}
