package io.booking.booking_service.datatype;

public enum AvailabilityMode {
    ALWAYS_AVAILABLE,
    DATE_WINDOW_ONLY,
    SCHEDULE_DRIVEN,
    CAPACITY_DRIVEN,
    INVENTORY_DRIVEN,
    POLICY_DRIVEN,
    EXTERNAL_DECISION
}