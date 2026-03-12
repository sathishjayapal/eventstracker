INSERT INTO domain (
    id,
    domain_name,
    status,
    comments,
    date_created,
    last_updated
) VALUES (
    1000,
    'RUNS_DOMAIN',
    'ACTIVE',
    'Seed domain for RUNS integration tests.',
    '2024-09-02 14:30:00',
    '2024-09-02 14:30:00'
) ON CONFLICT (id) DO NOTHING;

INSERT INTO domain_event (
    id,
    event_id,
    event_type,
    payload,
    created_by,
    updated_by,
    domain_id,
    date_created,
    last_updated
) VALUES (
    1100,
    'Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat.',
    'Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam.',
    'Vel eros donec ac odio tempor orci.',
    'Nec ullamcorper.',
    'Viverra suspendisse.',
    1000,
    '2024-09-02 14:30:00',
    '2024-09-02 14:30:00'
);

INSERT INTO domain_event (
    id,
    event_id,
    event_type,
    payload,
    created_by,
    updated_by,
    domain_id,
    date_created,
    last_updated
) VALUES (
    1101,
    'Nam liber tempor cum soluta nobis eleifend option congue nihil imperdiet doming id quod mazim placerat facer possim assum.',
    'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.',
    'Nibh ipsum consequat nisl vel pretium lectus quam id.',
    'Sed ut perspiciatis.',
    'Nec ullamcorper.',
    1000,
    '2024-09-03 14:30:00',
    '2024-09-03 14:30:00'
);