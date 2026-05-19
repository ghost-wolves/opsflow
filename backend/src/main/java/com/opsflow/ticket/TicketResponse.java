package com.opsflow.ticket;

import java.time.OffsetDateTime;

public record TicketResponse(
        Long id,
        String ticketNumber,
        String title,
        String description,
        String affectedSystem,
        Impact impact,
        Urgency urgency,
        Priority priority,
        TicketStatus status,
        Long requesterId,
        String requesterEmail,
        String requesterDisplayName,
        Long assignedToId,
        String assignedToEmail,
        String assignedToDisplayName,
        SlaRisk slaRisk,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime slaDueAt,
        OffsetDateTime resolvedAt,
        OffsetDateTime closedAt,
        boolean slaBreached
) {

    public static TicketResponse from(Ticket ticket) {
        SlaRisk slaRisk = new SlaRiskCalculationService().calculate(ticket);
        return from(ticket, slaRisk);
    }

    static TicketResponse from(Ticket ticket, SlaRisk slaRisk) {
        var requester = ticket.getRequester();
        var assignedTo = ticket.getAssignedTo();

        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getAffectedSystem(),
                ticket.getImpact(),
                ticket.getUrgency(),
                ticket.getPriority(),
                ticket.getStatus(),
                requester.getId(),
                requester.getEmail(),
                requester.getDisplayName(),
                assignedTo == null ? null : assignedTo.getId(),
                assignedTo == null ? null : assignedTo.getEmail(),
                assignedTo == null ? null : assignedTo.getDisplayName(),
                slaRisk,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getSlaDueAt(),
                ticket.getResolvedAt(),
                ticket.getClosedAt(),
                ticket.isSlaBreached()
        );
    }
}
