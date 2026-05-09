package com.opsflow.ticket;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;

@Service
public class SlaCalculationService {

    private final Clock clock;

    public SlaCalculationService() {
        this(Clock.systemDefaultZone());
    }

    SlaCalculationService(Clock clock) {
        this.clock = clock;
    }

    public OffsetDateTime calculateDueAt(Priority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Priority is required.");
        }

        OffsetDateTime now = OffsetDateTime.now(clock);

        return switch (priority) {
            case P1 -> now.plusHours(2);
            case P2 -> now.plusHours(4);
            case P3 -> addBusinessDays(now, 1);
            case P4 -> addBusinessDays(now, 3);
        };
    }

    private OffsetDateTime addBusinessDays(OffsetDateTime start, int businessDaysToAdd) {
        OffsetDateTime result = start;
        int addedDays = 0;

        while (addedDays < businessDaysToAdd) {
            result = result.plusDays(1);

            if (isBusinessDay(result)) {
                addedDays++;
            }
        }

        return result;
    }

    private boolean isBusinessDay(OffsetDateTime dateTime) {
        DayOfWeek day = dateTime.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }
}
