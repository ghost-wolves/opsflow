CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    ticket_number VARCHAR(40) NOT NULL UNIQUE,
    title VARCHAR(160) NOT NULL,
    description TEXT NOT NULL,
    affected_system VARCHAR(120) NOT NULL,
    impact VARCHAR(20) NOT NULL,
    urgency VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    status VARCHAR(40) NOT NULL,
    requester_id BIGINT NOT NULL REFERENCES users(id),
    assigned_to_id BIGINT REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sla_due_at TIMESTAMPTZ NOT NULL,
    resolved_at TIMESTAMPTZ,
    closed_at TIMESTAMPTZ,
    sla_breached BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_tickets_ticket_number ON tickets(ticket_number);
CREATE INDEX idx_tickets_requester_id ON tickets(requester_id);
CREATE INDEX idx_tickets_assigned_to_id ON tickets(assigned_to_id);
CREATE INDEX idx_tickets_priority ON tickets(priority);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_sla_due_at ON tickets(sla_due_at);
CREATE INDEX idx_tickets_sla_breached ON tickets(sla_breached);
