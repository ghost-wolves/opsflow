package com.opsflow.ticket;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TriageSuggestionServiceTest {

    private final TriageSuggestionService service = new TriageSuggestionService(
            new PriorityCalculationService()
    );

    @Test
    void outageSuggestsHighImpactHighUrgencyP1() {
        TriageSuggestionResponse response = service.suggest(
                "Production outage",
                "The billing portal is down for all users and customers cannot pay invoices.",
                "Billing Portal"
        );

        assertEquals(Impact.HIGH, response.suggestedImpact());
        assertEquals(Urgency.HIGH, response.suggestedUrgency());
        assertEquals(Priority.P1, response.suggestedPriority());
        assertTrue(response.explanation().contains("P1"));
    }

    @Test
    void accessIssueSuggestsMediumImpactMediumUrgencyP3() {
        TriageSuggestionResponse response = service.suggest(
                "Password reset not working",
                "I cannot access my account because the reset email failed.",
                "Identity Portal"
        );

        assertEquals(Impact.MEDIUM, response.suggestedImpact());
        assertEquals(Urgency.MEDIUM, response.suggestedUrgency());
        assertEquals(Priority.P3, response.suggestedPriority());
        assertTrue(response.explanation().contains("P3"));
    }

    @Test
    void routineRequestSuggestsLowImpactLowUrgencyP4() {
        TriageSuggestionResponse response = service.suggest(
                "Update display name",
                "Please update my display name when convenient.",
                "User Profile"
        );

        assertEquals(Impact.LOW, response.suggestedImpact());
        assertEquals(Urgency.LOW, response.suggestedUrgency());
        assertEquals(Priority.P4, response.suggestedPriority());
        assertTrue(response.explanation().contains("P4"));
    }

    @Test
    void handlesNullInputAsLowRiskSuggestion() {
        TriageSuggestionResponse response = service.suggest(null, null, null);

        assertEquals(Impact.LOW, response.suggestedImpact());
        assertEquals(Urgency.LOW, response.suggestedUrgency());
        assertEquals(Priority.P4, response.suggestedPriority());
    }
}
