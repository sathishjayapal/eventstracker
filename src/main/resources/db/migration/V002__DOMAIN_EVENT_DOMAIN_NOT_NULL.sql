-- Remove orphaned domain_event rows that have no associated domain
DELETE FROM domain_event WHERE domain_id IS NULL;

-- Enforce that every domain_event must have an associated domain
ALTER TABLE domain_event ALTER COLUMN domain_id SET NOT NULL;