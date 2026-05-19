package com.opsflow.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findAllByOrderByCreatedAtDesc();

    List<Ticket> findByRequesterEmailIgnoreCaseOrderByCreatedAtDesc(String requesterEmail);

    @Query("""
            select t
            from Ticket t
            left join t.assignedTo assignedTo
            where assignedTo is null
               or lower(assignedTo.email) = lower(:analystEmail)
            order by t.createdAt desc
            """)
    List<Ticket> findVisibleToAnalyst(@Param("analystEmail") String analystEmail);

    @Query("""
            select t
            from Ticket t
            where t.slaBreached = false
              and t.status <> :resolvedStatus
              and t.status <> :closedStatus
              and t.slaDueAt < :now
            """)
    List<Ticket> findActiveOverdueTickets(
            @Param("now") OffsetDateTime now,
            @Param("resolvedStatus") TicketStatus resolvedStatus,
            @Param("closedStatus") TicketStatus closedStatus
    );
}
