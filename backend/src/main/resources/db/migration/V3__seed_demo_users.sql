INSERT INTO roles (name)
VALUES
    ('REQUESTER'),
    ('ANALYST'),
    ('MANAGER')
ON CONFLICT (name) DO NOTHING;

INSERT INTO users (email, password_hash, display_name, enabled)
VALUES
    ('requester@opsflow.demo', '{noop}password123', 'Riley Requester', TRUE),
    ('analyst@opsflow.demo', '{noop}password123', 'Alex Analyst', TRUE),
    ('manager@opsflow.demo', '{noop}password123', 'Morgan Manager', TRUE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'REQUESTER'
WHERE u.email = 'requester@opsflow.demo'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'ANALYST'
WHERE u.email = 'analyst@opsflow.demo'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
JOIN roles r ON r.name = 'MANAGER'
WHERE u.email = 'manager@opsflow.demo'
ON CONFLICT DO NOTHING;
