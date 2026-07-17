-- Demo-only tables backing the intentionally-vulnerable /demo/** endpoints (Phase 8).
-- Isolated from the core detection/triage/report schema on purpose - never referenced by
-- SecurityEvent or IncidentReport.
CREATE TABLE demo_users (
    id       SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(50) NOT NULL
);

INSERT INTO demo_users (username, password) VALUES
    ('alice', 'password123'),
    ('bob', 'letmein');

CREATE TABLE demo_comments (
    id         SERIAL PRIMARY KEY,
    body       TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
