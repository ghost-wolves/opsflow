package com.opsflow.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank
        @Size(max = 160)
        String title,

        @NotBlank
        @Size(max = 5000)
        String description,

        @NotBlank
        @Size(max = 120)
        String affectedSystem,

        @NotNull
        Impact impact,

        @NotNull
        Urgency urgency
) {
}
