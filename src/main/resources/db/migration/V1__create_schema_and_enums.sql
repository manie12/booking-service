-- ============================================================
-- V1: Schema + extensions + enum types for booking-service
-- ============================================================

CREATE SCHEMA IF NOT EXISTS booking;
SET search_path TO booking;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Generic
CREATE TYPE record_status AS ENUM ('ACTIVE', 'INACTIVE', 'ARCHIVED');
CREATE TYPE actor_type AS ENUM ('CUSTOMER', 'AGENT', 'OPS_USER', 'SYSTEM');
CREATE TYPE adjustment_type AS ENUM ('DISCOUNT', 'SURCHARGE', 'OVERRIDE', 'MARKUP');

-- Guest / booking domain
CREATE TYPE guest_type AS ENUM ('ADULT', 'CHILD', 'INFANT', 'SENIOR', 'STUDENT', 'VIP');
CREATE TYPE fulfillment_type AS ENUM ('NO_FULFILLMENT', 'BOOKING_REQUIRED', 'ENTITLEMENT_REQUIRED', 'BOOKING_AND_ENTITLEMENT', 'SHIPMENT_REQUIRED', 'EXTERNAL_FULFILLMENT');

-- Cart
CREATE TYPE cart_status AS ENUM ('ACTIVE', 'CHECKOUT_IN_PROGRESS', 'CONVERTED', 'ABANDONED', 'EXPIRED');

-- Order
CREATE TYPE order_status AS ENUM ('DRAFT', 'PENDING_PAYMENT', 'PAID', 'PARTIALLY_PAID', 'PAYMENT_FAILED', 'CONFIRMED', 'PARTIALLY_CANCELLED', 'CANCELLED', 'FULFILLED', 'CLOSED');
CREATE TYPE price_component_type AS ENUM ('BASE_PRICE', 'DISCOUNT', 'TAX', 'SERVICE_FEE', 'SURCHARGE', 'MARKUP');

-- Booking
CREATE TYPE booking_status AS ENUM ('PENDING', 'HELD', 'BOOKED', 'CONFIRMED', 'CANCELLED', 'RESCHEDULED', 'CHECKED_IN', 'NO_SHOW', 'COMPLETED');

-- Schedule / capacity
CREATE TYPE schedule_instance_status AS ENUM ('DRAFT', 'ACTIVE', 'CLOSED', 'CANCELLED', 'COMPLETED');
CREATE TYPE capacity_pool_status AS ENUM ('ACTIVE', 'INACTIVE', 'CLOSED');
CREATE TYPE hold_status AS ENUM ('ACTIVE', 'CONSUMED', 'EXPIRED', 'RELEASED', 'FAILED');

-- Entitlement / pass / check-in
CREATE TYPE entitlement_status AS ENUM ('ISSUED', 'ACTIVE', 'PARTIALLY_USED', 'USED', 'EXPIRED', 'CANCELLED', 'REVOKED');
CREATE TYPE usage_event_type AS ENUM ('ISSUED', 'ACTIVATED', 'CHECKED_IN', 'REDEEMED', 'PARTIAL_USE', 'VOIDED', 'EXPIRED');
CREATE TYPE pass_status AS ENUM ('ISSUED', 'ACTIVE', 'USED', 'CANCELLED', 'EXPIRED', 'REVOKED');
CREATE TYPE check_in_result_status AS ENUM ('GRANTED', 'DENIED', 'ALREADY_USED', 'INVALID', 'EXPIRED');

-- Cancellation / reschedule
CREATE TYPE cancellation_status AS ENUM ('REQUESTED', 'APPROVED', 'REJECTED', 'COMPLETED');

-- Payment / refund
CREATE TYPE payment_status AS ENUM ('INITIATED', 'AUTHORIZED', 'CAPTURED', 'FAILED', 'VOIDED', 'REFUNDED', 'PARTIALLY_REFUNDED');
CREATE TYPE payment_transaction_type AS ENUM ('AUTHORIZE', 'CAPTURE', 'VOID', 'REFUND');
CREATE TYPE refund_status AS ENUM ('REQUESTED', 'APPROVED', 'PROCESSING', 'COMPLETED', 'FAILED', 'REJECTED');
