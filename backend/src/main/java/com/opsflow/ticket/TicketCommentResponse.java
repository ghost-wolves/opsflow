package com.opsflow.ticket;

import java.time.OffsetDateTime;

public record TicketCommentResponse(
        Long id,
        Long ticketId,
        Long authorId,
        String authorEmail,
        String authorDisplayName,
        String body,
        boolean internal,
        OffsetDateTime createdAt
) {

    public static TicketCommentResponse from(TicketComment comment) {
        var author = comment.getAuthor();

        return new TicketCommentResponse(
                comment.getId(),
                comment.getTicket().getId(),
                author.getId(),
                author.getEmail(),
                author.getDisplayName(),
                comment.getBody(),
                comment.isInternal(),
                comment.getCreatedAt()
        );
    }
}
