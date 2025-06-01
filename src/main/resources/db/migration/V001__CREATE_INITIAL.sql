CREATE SEQUENCE  IF NOT EXISTS primary_sequence START WITH 10000 INCREMENT BY 1;

CREATE TABLE domain (
    id BIGINT NOT NULL,
    domain_name TEXT NOT NULL,
    status TEXT NOT NULL,
    comments TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    date_created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_updated TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT domain_pkey PRIMARY KEY (id)
);

CREATE TABLE domain_event (
    id BIGINT NOT NULL,
    event_id TEXT NOT NULL,
    event_type TEXT NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_by TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    domain_id BIGINT,
    date_created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_updated TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT domain_event_pkey PRIMARY KEY (id)
);

CREATE TABLE event_domain_user (
    id BIGINT NOT NULL,
    username VARCHAR(255) NOT NULL,
    hash VARCHAR(255) NOT NULL,
    date_created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_updated TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT event_domain_user_pkey PRIMARY KEY (id)
);

ALTER TABLE domain_event ADD CONSTRAINT fk_domain_event_domain_id FOREIGN KEY (domain_id) REFERENCES domain (id) ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE event_domain_user ADD CONSTRAINT unique_event_domain_user_username UNIQUE (username);
