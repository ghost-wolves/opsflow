package com.opsflow.ticket;

import java.time.OffsetDateTime;

public record TicketAuditEventResponse(
        Long id,
        Long ticketId,
        Long actorId,
        String actorEmail,
        String actorDisplayName,
        TicketAuditEventType eventType,
        String oldValue,
        String newValue,
        String message,
        OffsetDateTime createdAt
) {

    public static TicketAuditEventResponse from(TicketAuditEvent event) {
        var actor = event.getActor();

        return new TicketAuditEventResponse(
                event.getId(),
                event.getTicket().getId(),
                actor.getId(),
                actor.getEmail(),
                actor.getDisplayName(),
                event.getEventType(),
                event.getOldValue(),
                event.getNewValue(),
                event.getMessage(),
                event.getCreatedAt()
        );
    }
}
