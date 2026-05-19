package com.opsflow.ticket;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;

@Service
public class SlaRiskCalculationService {

    private static final long DUE_SOON_HOURS = 4;

    private final Clock clock;

    public SlaRiskCalculationService() {
        this(Clock.systemDefaultZone());
    }

    SlaRiskCalculationService(Clock clock) {
        this.clock = clock;
    }

    public SlaRisk calculate(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket is required.");
        }

        if (ticket.isSlaBreached()) {
            return SlaRisk.BREACHED;
        }

        if (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED) {
            return SlaRisk.COMPLETED;
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        OffsetDateTime dueAt = ticket.getSlaDueAt();

        if (dueAt.isBefore(now)) {
            return SlaRisk.OVERDUE;
        }

        if (!dueAt.isAfter(now.plusHours(DUE_SOON_HOURS))) {
            return SlaRisk.DUE_SOON;
        }

        return SlaRisk.ON_TRACK;
    }
}
