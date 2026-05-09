package com.opsflow.ticket;

import com.opsflow.user.AppUser;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 40)
    private String ticketNumber;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "affected_system", nullable = false, length = 120)
    private String affectedSystem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Impact impact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Urgency urgency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TicketStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private AppUser requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private AppUser assignedTo;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "sla_due_at", nullable = false)
    private OffsetDateTime slaDueAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @Column(name = "sla_breached", nullable = false)
    private boolean slaBreached;

    protected Ticket() {
    }

    public Ticket(
            String ticketNumber,
            String title,
            String description,
            String affectedSystem,
            Impact impact,
            Urgency urgency,
            Priority priority,
            TicketStatus status,
            AppUser requester,
            OffsetDateTime slaDueAt
    ) {
        this.ticketNumber = ticketNumber;
        this.title = title;
        this.description = description;
        this.affectedSystem = affectedSystem;
        this.impact = impact;
        this.urgency = urgency;
        this.priority = priority;
        this.status = status;
        this.requester = requester;
        this.slaDueAt = slaDueAt;
        this.slaBreached = false;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getAffectedSystem() {
        return affectedSystem;
    }

    public Impact getImpact() {
        return impact;
    }

    public Urgency getUrgency() {
        return urgency;
    }

    public Priority getPriority() {
        return priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public AppUser getRequester() {
        return requester;
    }

    public AppUser getAssignedTo() {
        return assignedTo;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getSlaDueAt() {
        return slaDueAt;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public boolean isSlaBreached() {
        return slaBreached;
    }

    public void assignTo(AppUser analyst) {
        this.assignedTo = analyst;
    }

    public void changeStatus(TicketStatus status) {
        this.status = status;
    }

    public void markResolved(OffsetDateTime resolvedAt) {
        this.status = TicketStatus.RESOLVED;
        this.resolvedAt = resolvedAt;
    }

    public void markClosed(OffsetDateTime closedAt) {
        this.status = TicketStatus.CLOSED;
        this.closedAt = closedAt;
    }

    public void markSlaBreached() {
        this.slaBreached = true;
    }
}
