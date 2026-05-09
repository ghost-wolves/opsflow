package com.opsflow.ticket;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateTicketRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validCreateTicketRequestHasNoValidationErrors() {
        CreateTicketRequest request = new CreateTicketRequest(
                "Billing export failed",
                "Customers cannot download invoices because the nightly billing export failed.",
                "Billing Portal",
                Impact.HIGH,
                Urgency.HIGH
        );

        var violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void invalidCreateTicketRequestReturnsValidationErrors() {
        CreateTicketRequest request = new CreateTicketRequest(
                "",
                "",
                "",
                null,
                null
        );

        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title"))
        );
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description"))
        );
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("affectedSystem"))
        );
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("impact"))
        );
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("urgency"))
        );
    }

    @Test
    void overMaxLengthFieldsReturnValidationErrors() {
        CreateTicketRequest request = new CreateTicketRequest(
                "A".repeat(161),
                "B".repeat(5001),
                "C".repeat(121),
                Impact.LOW,
                Urgency.LOW
        );

        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title"))
        );
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description"))
        );
        assertTrue(
                violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("affectedSystem"))
        );
    }
}
