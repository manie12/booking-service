-- ============================================================
-- V9: Payments, Payment Transactions, Refunds, Refund Items
-- ============================================================
SET search_path TO booking;

CREATE TABLE payments (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id          UUID NOT NULL,
    order_id           UUID NOT NULL REFERENCES orders (id),
    payment_reference  VARCHAR(200) NOT NULL,
    payment_method     VARCHAR(50) NOT NULL,
    provider_name      VARCHAR(100) NOT NULL,
    currency_code      VARCHAR(10) NOT NULL,
    requested_amount   NUMERIC(14, 2) NOT NULL,
    authorized_amount  NUMERIC(14, 2) NOT NULL DEFAULT 0,
    captured_amount    NUMERIC(14, 2) NOT NULL DEFAULT 0,
    refunded_amount    NUMERIC(14, 2) NOT NULL DEFAULT 0,
    status             payment_status NOT NULL DEFAULT 'INITIATED',
    initiated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at       TIMESTAMPTZ,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_payments_payment_reference UNIQUE (payment_reference)
);

CREATE INDEX idx_payments_order_id ON payments (order_id);
CREATE INDEX idx_payments_tenant_id ON payments (tenant_id);
CREATE INDEX idx_payments_status ON payments (status);

CREATE TABLE payment_transactions (
    id                         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id                 UUID NOT NULL REFERENCES payments (id) ON DELETE CASCADE,
    transaction_reference      VARCHAR(200) NOT NULL,
    provider_transaction_id    VARCHAR(200),
    transaction_type           payment_transaction_type NOT NULL,
    status                     payment_status NOT NULL,
    amount                     NUMERIC(14, 2) NOT NULL,
    currency_code              VARCHAR(10) NOT NULL,
    provider_response_code    VARCHAR(100),
    provider_response_message TEXT,
    payload_json               TEXT,
    processed_at               TIMESTAMPTZ,
    created_at                 TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_payment_transactions_reference UNIQUE (transaction_reference)
);

CREATE INDEX idx_payment_transactions_payment_id ON payment_transactions (payment_id);

CREATE TABLE refunds (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID NOT NULL,
    order_id          UUID NOT NULL REFERENCES orders (id),
    payment_id        UUID REFERENCES payments (id),
    refund_reference  VARCHAR(200) NOT NULL,
    status            refund_status NOT NULL DEFAULT 'REQUESTED',
    currency_code     VARCHAR(10) NOT NULL,
    requested_amount  NUMERIC(14, 2) NOT NULL,
    approved_amount   NUMERIC(14, 2),
    refunded_amount   NUMERIC(14, 2) NOT NULL DEFAULT 0,
    reason_code       VARCHAR(100),
    reason_text       TEXT,
    actor_type        actor_type NOT NULL DEFAULT 'CUSTOMER',
    actor_id          VARCHAR(200),
    requested_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_refunds_refund_reference UNIQUE (refund_reference)
);

CREATE INDEX idx_refunds_order_id ON refunds (order_id);
CREATE INDEX idx_refunds_payment_id ON refunds (payment_id);
CREATE INDEX idx_refunds_tenant_id ON refunds (tenant_id);
CREATE INDEX idx_refunds_status ON refunds (status);

CREATE TABLE refund_items (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    refund_id              UUID NOT NULL REFERENCES refunds (id) ON DELETE CASCADE,
    order_item_id          UUID NOT NULL REFERENCES order_items (id),
    quantity_refunded      INTEGER NOT NULL DEFAULT 1,
    refund_amount          NUMERIC(14, 2) NOT NULL,
    tax_refund_amount      NUMERIC(14, 2) NOT NULL DEFAULT 0,
    fee_deduction_amount   NUMERIC(14, 2) NOT NULL DEFAULT 0,
    notes                  TEXT,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_refund_items_quantity CHECK (quantity_refunded > 0)
);

CREATE INDEX idx_refund_items_refund_id ON refund_items (refund_id);
CREATE INDEX idx_refund_items_order_item_id ON refund_items (order_item_id);
