package com.opsflow.ticket;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SlaCalculationServiceTest {

    private final Clock fixedClock = Clock.fixed(
            Instant.parse("2026-05-04T12:00:00Z"),
            ZoneId.of("UTC")
    );

    private final SlaCalculationService service = new SlaCalculationService(fixedClock);

    @Test
    void p1DueDateIsTwoHoursFromNow() {
        OffsetDateTime dueAt = service.calculateDueAt(Priority.P1);

        assertEquals(OffsetDateTime.parse("2026-05-04T14:00:00Z"), dueAt);
    }

    @Test
    void p2DueDateIsFourHoursFromNow() {
        OffsetDateTime dueAt = service.calculateDueAt(Priority.P2);

        assertEquals(OffsetDateTime.parse("2026-05-04T16:00:00Z"), dueAt);
    }

    @Test
    void p3DueDateIsOneBusinessDayFromNow() {
        OffsetDateTime dueAt = service.calculateDueAt(Priority.P3);

        assertEquals(OffsetDateTime.parse("2026-05-05T12:00:00Z"), dueAt);
    }

    @Test
    void p4DueDateIsThreeBusinessDaysFromNow() {
        OffsetDateTime dueAt = service.calculateDueAt(Priority.P4);

        assertEquals(OffsetDateTime.parse("2026-05-07T12:00:00Z"), dueAt);
    }

    @Test
    void businessDayCalculationSkipsWeekend() {
        Clock fridayClock = Clock.fixed(
                Instant.parse("2026-05-08T12:00:00Z"),
                ZoneId.of("UTC")
        );

        SlaCalculationService fridayService = new SlaCalculationService(fridayClock);

        assertEquals(
                OffsetDateTime.parse("2026-05-11T12:00:00Z"),
                fridayService.calculateDueAt(Priority.P3)
        );

        assertEquals(
                OffsetDateTime.parse("2026-05-13T12:00:00Z"),
                fridayService.calculateDueAt(Priority.P4)
        );
    }

    @Test
    void nullPriorityThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> service.calculateDueAt(null));
    }
}
