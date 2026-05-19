package com.opsflow.ticket;

public record TriageSuggestionResponse(
        Impact suggestedImpact,
        Urgency suggestedUrgency,
        Priority suggestedPriority,
        String explanation
) {
}
