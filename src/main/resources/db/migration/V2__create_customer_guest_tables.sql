-- ============================================================
-- V2: Customers & Guest Profiles
-- ============================================================
SET search_path TO booking;

CREATE TABLE customers (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    customer_number     VARCHAR(100) NOT NULL,
    external_reference  VARCHAR(200),
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    full_name           VARCHAR(200),
    email               VARCHAR(200),
    phone_number        VARCHAR(50),
    country_code        VARCHAR(10),
    status              VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_customers_tenant_number UNIQUE (tenant_id, customer_number),
    CONSTRAINT uq_customers_tenant_email UNIQUE (tenant_id, email)
);

CREATE TABLE guest_profiles (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             UUID NOT NULL,
    customer_id           varchar(100),
    first_name            VARCHAR(100) NOT NULL,
    last_name             VARCHAR(100) NOT NULL,
    full_name             VARCHAR(200),
    date_of_birth         DATE,
    gender                VARCHAR(20),
    nationality_code      VARCHAR(10),
    document_type         VARCHAR(50),
    document_number       VARCHAR(100),
    email                 VARCHAR(200),
    phone_number          VARCHAR(50),
    special_requirements  TEXT,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_guest_profiles_customer_id ON guest_profiles (customer_id);
CREATE INDEX idx_guest_profiles_tenant_id ON guest_profiles (tenant_id);
