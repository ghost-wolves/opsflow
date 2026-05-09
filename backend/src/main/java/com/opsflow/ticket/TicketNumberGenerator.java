package com.opsflow.ticket;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.ZonedDateTime;

@Component
public class TicketNumberGenerator {

    private final Clock clock;

    public TicketNumberGenerator() {
        this(Clock.systemUTC());
    }

    TicketNumberGenerator(Clock clock) {
        this.clock = clock;
    }

    public String generate(long sequenceNumber) {
        if (sequenceNumber < 1) {
            throw new IllegalArgumentException("Sequence number must be greater than zero.");
        }

        int year = ZonedDateTime.now(clock).getYear();

        return String.format("OPS-%d-%04d", year, sequenceNumber);
    }
}
