package com.opsflow.ticket;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketNumberGeneratorTest {

    @Test
    void generatesTicketNumberWithYearAndPaddedSequence() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-05-09T12:00:00Z"),
                ZoneOffset.UTC
        );

        TicketNumberGenerator generator = new TicketNumberGenerator(fixedClock);

        assertEquals("OPS-2026-0001", generator.generate(1));
        assertEquals("OPS-2026-0042", generator.generate(42));
        assertEquals("OPS-2026-1234", generator.generate(1234));
    }

    @Test
    void rejectsInvalidSequenceNumber() {
        Clock fixedClock = Clock.fixed(
                Instant.parse("2026-05-09T12:00:00Z"),
                ZoneOffset.UTC
        );

        TicketNumberGenerator generator = new TicketNumberGenerator(fixedClock);

        assertThrows(IllegalArgumentException.class, () -> generator.generate(0));
    }
}
