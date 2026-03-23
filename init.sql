-- EventTracker Database Initialization
-- Flyway migrations will run automatically when the app starts
-- This file just ensures the database exists and pgvector is available

-- Enable pgvector extension for AI embeddings
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS btree_gin;
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Comment: Actual schema creation is handled by Flyway migrations
-- See: src/main/resources/db/migration/
