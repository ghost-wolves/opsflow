package com.opsflow.ticket;

import org.springframework.stereotype.Service;

@Service
public class PriorityCalculationService {

    public Priority calculate(Impact impact, Urgency urgency) {
        if (impact == null || urgency == null) {
            throw new IllegalArgumentException("Impact and urgency are required.");
        }

        if (impact == Impact.HIGH && urgency == Urgency.HIGH) {
            return Priority.P1;
        }

        if (impact == Impact.HIGH && urgency == Urgency.MEDIUM) {
            return Priority.P2;
        }

        if (impact == Impact.MEDIUM && urgency == Urgency.HIGH) {
            return Priority.P2;
        }

        if (impact == Impact.MEDIUM && urgency == Urgency.MEDIUM) {
            return Priority.P3;
        }

        if (impact == Impact.LOW && urgency == Urgency.LOW) {
            return Priority.P4;
        }

        return Priority.P3;
    }
}
