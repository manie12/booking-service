package io.booking.booking_service.datatype.booking;

public enum PaymentStatus {
    INITIATED,
    AUTHORIZED,
    CAPTURED,
    FAILED,
    VOIDED,
    REFUNDED,
    PARTIALLY_REFUNDED
}