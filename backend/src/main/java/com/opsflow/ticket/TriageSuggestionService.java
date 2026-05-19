package com.opsflow.ticket;

import org.springframework.stereotype.Service;

@Service
public class TriageSuggestionService {

    private final PriorityCalculationService priorityCalculationService;

    public TriageSuggestionService(PriorityCalculationService priorityCalculationService) {
        this.priorityCalculationService = priorityCalculationService;
    }

    public TriageSuggestionResponse suggest(String title, String description, String affectedSystem) {
        String text = normalize(title, description, affectedSystem);

        Impact impact = suggestImpact(text);
        Urgency urgency = suggestUrgency(text);
        Priority priority = priorityCalculationService.calculate(impact, urgency);

        return new TriageSuggestionResponse(
                impact,
                urgency,
                priority,
                buildExplanation(impact, urgency, priority)
        );
    }

    private Impact suggestImpact(String text) {
        if (containsAny(
                text,
                "outage",
                "down",
                "unavailable",
                "all users",
                "everyone",
                "company-wide",
                "production",
                "prod",
                "payment",
                "billing",
                "data loss",
                "breach",
                "security incident"
        )) {
            return Impact.HIGH;
        }

        if (containsAny(
                text,
                "multiple users",
                "customers",
                "customer",
                "team",
                "department",
                "cannot access",
                "login",
                "password",
                "vpn",
                "export failed"
        )) {
            return Impact.MEDIUM;
        }

        return Impact.LOW;
    }

    private Urgency suggestUrgency(String text) {
        if (containsAny(
                text,
                "urgent",
                "immediately",
                "cannot work",
                "blocked",
                "deadline",
                "production",
                "prod",
                "security",
                "breach",
                "outage",
                "down"
        )) {
            return Urgency.HIGH;
        }

        if (containsAny(
                text,
                "cannot access",
                "failed",
                "error",
                "not working",
                "login",
                "password",
                "vpn",
                "slow"
        )) {
            return Urgency.MEDIUM;
        }

        return Urgency.LOW;
    }

    private String buildExplanation(Impact impact, Urgency urgency, Priority priority) {
        return "Suggested " + priority + " because the ticket appears to have "
                + impact + " impact and " + urgency + " urgency.";
    }

    private String normalize(String title, String description, String affectedSystem) {
        return String.join(
                " ",
                nullToEmpty(title),
                nullToEmpty(description),
                nullToEmpty(affectedSystem)
        ).toLowerCase();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }

        return false;
    }
}
