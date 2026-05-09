package com.opsflow.ticket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriorityCalculationServiceTest {

    private final PriorityCalculationService service = new PriorityCalculationService();

    @Test
    void highImpactHighUrgencyIsP1() {
        assertEquals(Priority.P1, service.calculate(Impact.HIGH, Urgency.HIGH));
    }

    @Test
    void highImpactMediumUrgencyIsP2() {
        assertEquals(Priority.P2, service.calculate(Impact.HIGH, Urgency.MEDIUM));
    }

    @Test
    void mediumImpactHighUrgencyIsP2() {
        assertEquals(Priority.P2, service.calculate(Impact.MEDIUM, Urgency.HIGH));
    }

    @Test
    void mediumImpactMediumUrgencyIsP3() {
        assertEquals(Priority.P3, service.calculate(Impact.MEDIUM, Urgency.MEDIUM));
    }

    @Test
    void lowImpactLowUrgencyIsP4() {
        assertEquals(Priority.P4, service.calculate(Impact.LOW, Urgency.LOW));
    }

    @Test
    void unspecifiedCombinationsDefaultToP3() {
        assertEquals(Priority.P3, service.calculate(Impact.HIGH, Urgency.LOW));
        assertEquals(Priority.P3, service.calculate(Impact.MEDIUM, Urgency.LOW));
        assertEquals(Priority.P3, service.calculate(Impact.LOW, Urgency.HIGH));
        assertEquals(Priority.P3, service.calculate(Impact.LOW, Urgency.MEDIUM));
    }

    @Test
    void nullImpactOrUrgencyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> service.calculate(null, Urgency.HIGH));
        assertThrows(IllegalArgumentException.class, () -> service.calculate(Impact.HIGH, null));
    }
}
