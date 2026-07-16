CREATE TABLE security_events (
    id            UUID PRIMARY KEY,
    event_type    VARCHAR(32)  NOT NULL,
    source_ip     VARCHAR(45)  NOT NULL,
    request_uri   VARCHAR(2048) NOT NULL,
    http_method   VARCHAR(10)  NOT NULL,
    score         INTEGER      NOT NULL,
    severity      VARCHAR(16)  NOT NULL,
    raw_details   TEXT,
    detected_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_security_events_detected_at ON security_events (detected_at);
CREATE INDEX idx_security_events_severity ON security_events (severity);
