package com.opsflow.ticket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketAuditEventRepository extends JpaRepository<TicketAuditEvent, Long> {

    List<TicketAuditEvent> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
