CREATE TABLE ticket_audit_events (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES tickets(id) ON DELETE CASCADE,
    actor_id BIGINT NOT NULL REFERENCES users(id),
    event_type VARCHAR(80) NOT NULL,
    old_value TEXT,
    new_value TEXT,
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ticket_audit_events_ticket_id ON ticket_audit_events(ticket_id);
CREATE INDEX idx_ticket_audit_events_actor_id ON ticket_audit_events(actor_id);
CREATE INDEX idx_ticket_audit_events_event_type ON ticket_audit_events(event_type);
CREATE INDEX idx_ticket_audit_events_created_at ON ticket_audit_events(created_at);
