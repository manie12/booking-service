package io.booking.booking_service.datatype.booking;

public enum OrderStatus {
    DRAFT,
    PENDING_PAYMENT,
    PAID,
    PARTIALLY_PAID,
    PAYMENT_FAILED,
    CONFIRMED,
    PARTIALLY_CANCELLED,
    CANCELLED,
    FULFILLED,
    CLOSED
}