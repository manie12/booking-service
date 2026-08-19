-- ============================================================
-- V10: Processed Event (Kafka / outbox idempotency tracking)
-- ============================================================
SET search_path TO booking;

CREATE TABLE processed_event (
    id            VARCHAR(200) PRIMARY KEY,
    processed_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
