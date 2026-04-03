package io.booking.booking_service.datatype.booking;

public enum FulfillmentType {
    NO_FULFILLMENT,
    BOOKING_REQUIRED,
    ENTITLEMENT_REQUIRED,
    BOOKING_AND_ENTITLEMENT,
    SHIPMENT_REQUIRED,
    EXTERNAL_FULFILLMENT
}