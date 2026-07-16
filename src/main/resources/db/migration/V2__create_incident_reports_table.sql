CREATE TABLE incident_reports (
    id                 UUID PRIMARY KEY,
    security_event_id  UUID NOT NULL UNIQUE REFERENCES security_events (id),
    classification     VARCHAR(128) NOT NULL,
    severity           VARCHAR(16)  NOT NULL,
    recommendation     TEXT         NOT NULL,
    ai_generated       BOOLEAN      NOT NULL,
    created_at         TIMESTAMP WITH TIME ZONE NOT NULL
);
