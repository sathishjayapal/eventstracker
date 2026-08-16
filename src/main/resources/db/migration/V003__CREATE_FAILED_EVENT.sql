CREATE TABLE failed_event (
    id BIGINT NOT NULL,
    source_queue TEXT NOT NULL,
    event_type TEXT,
    payload TEXT NOT NULL,
    failure_reason TEXT,
    correlation_id TEXT,
    death_count BIGINT NOT NULL,
    date_created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT failed_event_pkey PRIMARY KEY (id)
);
