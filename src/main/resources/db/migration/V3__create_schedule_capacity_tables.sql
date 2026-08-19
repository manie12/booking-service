-- ============================================================
-- V3: Schedule Instances & Capacity Pools
-- ============================================================
SET search_path TO booking;

CREATE TABLE schedule_instances (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    product_id          UUID NOT NULL,
    product_variant_id  UUID,
    instance_code       VARCHAR(100) NOT NULL,
    instance_name       VARCHAR(200),
    start_at            TIMESTAMPTZ NOT NULL,
    end_at              TIMESTAMPTZ NOT NULL,
    timezone             VARCHAR(60) NOT NULL,
    venue_code          VARCHAR(100),
    resource_reference  VARCHAR(200),
    status              schedule_instance_status NOT NULL DEFAULT 'DRAFT',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_schedule_instances_tenant_code UNIQUE (tenant_id, instance_code),
    CONSTRAINT chk_schedule_instances_time CHECK (end_at > start_at)
);

CREATE INDEX idx_schedule_instances_product_id ON schedule_instances (product_id);
CREATE INDEX idx_schedule_instances_tenant_id ON schedule_instances (tenant_id);
CREATE INDEX idx_schedule_instances_start_at ON schedule_instances (start_at);

CREATE TABLE capacity_pools (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    schedule_instance_id UUID NOT NULL REFERENCES schedule_instances (id) ON DELETE CASCADE,
    pool_code            VARCHAR(100) NOT NULL,
    pool_name            VARCHAR(200),
    capacity_total       INTEGER NOT NULL,
    capacity_held        INTEGER NOT NULL DEFAULT 0,
    capacity_booked      INTEGER NOT NULL DEFAULT 0,
    capacity_available   INTEGER NOT NULL,
    status               capacity_pool_status NOT NULL DEFAULT 'ACTIVE',
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_capacity_pools_instance_code UNIQUE (schedule_instance_id, pool_code),
    CONSTRAINT chk_capacity_pools_totals CHECK (capacity_total >= 0 AND capacity_held >= 0 AND capacity_booked >= 0)
);

CREATE INDEX idx_capacity_pools_schedule_instance_id ON capacity_pools (schedule_instance_id);
