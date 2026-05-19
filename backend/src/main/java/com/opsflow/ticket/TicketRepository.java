package com.opsflow.ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
