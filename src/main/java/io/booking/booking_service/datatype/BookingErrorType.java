package io.booking.booking_service.datatype;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Booking business error catalog.
 *
 * Conventions:
 * - code: stable external/business code
 * - statusMessage: internal/ops friendly
 * - customerMessage: safe for UI/customer
 *
 * Notes:
 * - Keep codes stable once released.
 * - Prefer category prefixes for easier analytics.
 */
@Slf4j
@ToString
@Getter
public enum BookingErrorType {
    SUCCESS("00", "Success", "Success"),

    // -------------------------------------------------
    // Generic
    // -------------------------------------------------
    INVALID_REQUEST("BKG_400_001", "Invalid request", "The request is invalid."),
    INTERNAL_ERROR("BKG_500_001", "Internal error", "An internal error occurred."),
    VALIDATION_ERROR("BKG_400_002", "Validation error", "Validation failed."),
    GENERIC_ERROR("BKG_500_002", "Generic error", "Something went wrong."),

    // -------------------------------------------------
    // Customer
    // -------------------------------------------------
    CUSTOMER_NOT_FOUND("BKG_404_010", "Customer not found", "The customer was not found."),
    CUSTOMER_INACTIVE("BKG_403_011", "Customer inactive", "The customer is inactive."),
    CUSTOMER_ALREADY_EXISTS("BKG_400_012", "Customer already exists", "A customer with the same number already exists."),
    CUSTOMER_NAME_REQUIRED("BKG_400_015", "Customer name required", "The customer name is required."),
    CUSTOMER_NUMBER_REQUIRED("BKG_400_016", "Customer number required", "The customer number is required."),
    CUSTOMER_EMAIL_INVALID("BKG_400_017", "Customer email invalid", "The customer email is invalid."),
    CUSTOMER_PHONE_INVALID("BKG_400_018", "Customer phone invalid", "The customer phone number is invalid."),

    // -------------------------------------------------
    // Guest Profile
    // -------------------------------------------------
    GUEST_PROFILE_NOT_FOUND("BKG_404_020", "Guest profile not found", "The guest profile was not found."),
    GUEST_PROFILE_ALREADY_EXISTS("BKG_400_021", "Guest profile already exists", "A guest profile with the same document already exists."),
    GUEST_PROFILE_REQUEST_REQUIRED("BKG_400_022", "Guest profile request required", "The guest profile request body is required."),
    GUEST_PROFILE_REQUEST_INVALID("BKG_400_023", "Guest profile request invalid", "The guest profile request is invalid."),
    GUEST_PROFILE_NAME_REQUIRED("BKG_400_024", "Guest profile name required", "The guest name is required."),
    GUEST_PROFILE_DOB_INVALID("BKG_400_025", "Guest profile date of birth invalid", "The guest date of birth is invalid."),
    GUEST_PROFILE_DOCUMENT_REQUIRED("BKG_400_026", "Guest profile document required", "The guest document is required."),

    // -------------------------------------------------
    // Cart
    // -------------------------------------------------
    CART_NOT_FOUND("BKG_404_100", "Cart not found", "The cart was not found."),
    CART_ALREADY_EXISTS("BKG_400_101", "Cart already exists", "A cart with the same number already exists."),
    CART_REQUEST_REQUIRED("BKG_400_102", "Cart request required", "The cart request body is required."),
    CART_REQUEST_INVALID("BKG_400_103", "Cart request invalid", "The cart request is invalid."),
    CART_EMPTY("BKG_400_104", "Cart is empty", "The cart has no items."),
    CART_EXPIRED("BKG_403_105", "Cart expired", "The cart has expired."),
    CART_INACTIVE("BKG_403_106", "Cart inactive", "The cart is not active."),
    CART_CHECKOUT_IN_PROGRESS("BKG_409_107", "Cart checkout in progress", "The cart is already being checked out."),
    CART_CUSTOMER_REQUIRED("BKG_400_108", "Cart customer required", "The customer is required."),
    CART_CURRENCY_REQUIRED("BKG_400_109", "Cart currency required", "The cart currency is required."),

    // -------------------------------------------------
    // Cart Item
    // -------------------------------------------------
    CART_ITEM_NOT_FOUND("BKG_404_120", "Cart item not found", "The cart item was not found."),
    CART_ITEM_ALREADY_EXISTS("BKG_400_121", "Cart item already exists", "The item already exists in the cart."),
    CART_ITEM_REQUEST_REQUIRED("BKG_400_122", "Cart item request required", "The cart item request body is required."),
    CART_ITEM_REQUEST_INVALID("BKG_400_123", "Cart item request invalid", "The cart item request is invalid."),
    CART_ITEM_PRODUCT_REQUIRED("BKG_400_124", "Cart item product required", "The product is required."),
    CART_ITEM_QUANTITY_INVALID("BKG_400_125", "Cart item quantity invalid", "The quantity must be greater than zero."),
    CART_ITEM_CURRENCY_MISMATCH("BKG_400_126", "Cart item currency mismatch", "The cart item currency does not match the cart currency."),
    CART_ITEM_SERVICE_DATE_REQUIRED("BKG_400_127", "Cart item service date required", "The service date is required."),
    CART_ITEM_DUPLICATE_GUEST("BKG_400_128", "Cart item duplicate guest", "The same guest cannot be added twice to the same cart item."),

    // -------------------------------------------------
    // Cart Adjustment
    // -------------------------------------------------
    CART_ADJUSTMENT_NOT_FOUND("BKG_404_140", "Cart adjustment not found", "The cart adjustment was not found."),
    CART_ADJUSTMENT_REQUEST_REQUIRED("BKG_400_141", "Cart adjustment request required", "The cart adjustment request body is required."),
    CART_ADJUSTMENT_REQUEST_INVALID("BKG_400_142", "Cart adjustment request invalid", "The cart adjustment request is invalid."),
    CART_ADJUSTMENT_TYPE_REQUIRED("BKG_400_143", "Cart adjustment type required", "The adjustment type is required."),
    CART_ADJUSTMENT_AMOUNT_INVALID("BKG_400_144", "Cart adjustment amount invalid", "The adjustment amount is invalid."),

    // -------------------------------------------------
    // Order
    // -------------------------------------------------
    ORDER_NOT_FOUND("BKG_404_200", "Order not found", "The order was not found."),
    ORDER_ALREADY_EXISTS("BKG_400_201", "Order already exists", "An order with the same number already exists."),
    ORDER_REQUEST_REQUIRED("BKG_400_202", "Order request required", "The order request body is required."),
    ORDER_REQUEST_INVALID("BKG_400_203", "Order request invalid", "The order request is invalid."),
    ORDER_CUSTOMER_REQUIRED("BKG_400_204", "Order customer required", "The customer is required for the order."),
    ORDER_CART_REQUIRED("BKG_400_205", "Order cart required", "The cart is required to place the order."),
    ORDER_ALREADY_PAID("BKG_409_206", "Order already paid", "The order has already been paid."),
    ORDER_PAYMENT_PENDING("BKG_409_207", "Order payment pending", "The order payment is still pending."),
    ORDER_CANCELLED("BKG_403_208", "Order cancelled", "The order has been cancelled."),
    ORDER_CLOSED("BKG_403_209", "Order closed", "The order is already closed."),
    ORDER_IDEMPOTENCY_KEY_REQUIRED("BKG_400_210", "Order idempotency key required", "The idempotency key is required."),
    ORDER_TOTAL_INVALID("BKG_400_211", "Order total invalid", "The order total is invalid."),

    // -------------------------------------------------
    // Order Item
    // -------------------------------------------------
    ORDER_ITEM_NOT_FOUND("BKG_404_220", "Order item not found", "The order item was not found."),
    ORDER_ITEM_REQUEST_REQUIRED("BKG_400_221", "Order item request required", "The order item request body is required."),
    ORDER_ITEM_REQUEST_INVALID("BKG_400_222", "Order item request invalid", "The order item request is invalid."),
    ORDER_ITEM_PRODUCT_REQUIRED("BKG_400_223", "Order item product required", "The product is required for the order item."),
    ORDER_ITEM_QUANTITY_INVALID("BKG_400_224", "Order item quantity invalid", "The order item quantity must be greater than zero."),
    ORDER_ITEM_AMOUNT_INVALID("BKG_400_225", "Order item amount invalid", "The order item amount is invalid."),
    ORDER_ITEM_STATUS_INVALID("BKG_400_226", "Order item status invalid", "The order item status is invalid."),
    ORDER_ITEM_FULFILLMENT_TYPE_REQUIRED("BKG_400_227", "Order item fulfillment type required", "The fulfillment type is required."),
    ORDER_ITEM_FULFILLMENT_TYPE_INVALID("BKG_400_228", "Order item fulfillment type invalid", "The fulfillment type is invalid."),

    // -------------------------------------------------
    // Order Item Guest
    // -------------------------------------------------
    ORDER_ITEM_GUEST_NOT_FOUND("BKG_404_230", "Order item guest not found", "The order item guest was not found."),
    ORDER_ITEM_GUEST_REQUEST_REQUIRED("BKG_400_231", "Order item guest request required", "The order item guest request body is required."),
    ORDER_ITEM_GUEST_REQUEST_INVALID("BKG_400_232", "Order item guest request invalid", "The order item guest request is invalid."),
    ORDER_ITEM_GUEST_NAME_REQUIRED("BKG_400_233", "Order item guest name required", "The guest name is required."),
    ORDER_ITEM_GUEST_TYPE_REQUIRED("BKG_400_234", "Order item guest type required", "The guest type is required."),

    // -------------------------------------------------
    // Order Price Component
    // -------------------------------------------------
    ORDER_PRICE_COMPONENT_NOT_FOUND("BKG_404_240", "Order price component not found", "The order price component was not found."),
    ORDER_PRICE_COMPONENT_REQUEST_REQUIRED("BKG_400_241", "Order price component request required", "The order price component request body is required."),
    ORDER_PRICE_COMPONENT_REQUEST_INVALID("BKG_400_242", "Order price component request invalid", "The order price component request is invalid."),
    ORDER_PRICE_COMPONENT_TYPE_REQUIRED("BKG_400_243", "Order price component type required", "The component type is required."),
    ORDER_PRICE_COMPONENT_AMOUNT_INVALID("BKG_400_244", "Order price component amount invalid", "The component amount is invalid."),

    // -------------------------------------------------
    // Payment
    // -------------------------------------------------
    PAYMENT_NOT_FOUND("BKG_404_300", "Payment not found", "The payment was not found."),
    PAYMENT_ALREADY_EXISTS("BKG_400_301", "Payment already exists", "A payment with the same reference already exists."),
    PAYMENT_REQUEST_REQUIRED("BKG_400_302", "Payment request required", "The payment request body is required."),
    PAYMENT_REQUEST_INVALID("BKG_400_303", "Payment request invalid", "The payment request is invalid."),
    PAYMENT_ORDER_REQUIRED("BKG_400_304", "Payment order required", "The order is required for the payment."),
    PAYMENT_REFERENCE_REQUIRED("BKG_400_305", "Payment reference required", "The payment reference is required."),
    PAYMENT_METHOD_REQUIRED("BKG_400_306", "Payment method required", "The payment method is required."),
    PAYMENT_AMOUNT_INVALID("BKG_400_307", "Payment amount invalid", "The payment amount is invalid."),
    PAYMENT_ALREADY_CAPTURED("BKG_409_308", "Payment already captured", "The payment has already been captured."),
    PAYMENT_ALREADY_REFUNDED("BKG_409_309", "Payment already refunded", "The payment has already been refunded."),
    PAYMENT_STATUS_INVALID("BKG_400_310", "Payment status invalid", "The payment status is invalid."),
    PAYMENT_PROVIDER_CALLBACK_INVALID("BKG_400_311", "Payment provider callback invalid", "The payment provider callback is invalid."),
    PAYMENT_AUTHORIZATION_FAILED("BKG_402_312", "Payment authorization failed", "The payment authorization failed."),
    PAYMENT_CAPTURE_FAILED("BKG_402_313", "Payment capture failed", "The payment capture failed."),

    // -------------------------------------------------
    // Payment Transaction
    // -------------------------------------------------
    PAYMENT_TRANSACTION_NOT_FOUND("BKG_404_320", "Payment transaction not found", "The payment transaction was not found."),
    PAYMENT_TRANSACTION_ALREADY_EXISTS("BKG_400_321", "Payment transaction already exists", "A payment transaction with the same reference already exists."),
    PAYMENT_TRANSACTION_REQUEST_REQUIRED("BKG_400_322", "Payment transaction request required", "The payment transaction request body is required."),
    PAYMENT_TRANSACTION_REQUEST_INVALID("BKG_400_323", "Payment transaction request invalid", "The payment transaction request is invalid."),
    PAYMENT_TRANSACTION_TYPE_REQUIRED("BKG_400_324", "Payment transaction type required", "The transaction type is required."),
    PAYMENT_TRANSACTION_AMOUNT_INVALID("BKG_400_325", "Payment transaction amount invalid", "The transaction amount is invalid."),

    // -------------------------------------------------
    // Schedule Instance
    // -------------------------------------------------
    SCHEDULE_INSTANCE_NOT_FOUND("BKG_404_400", "Schedule instance not found", "The schedule instance was not found."),
    SCHEDULE_INSTANCE_ALREADY_EXISTS("BKG_400_401", "Schedule instance already exists", "A schedule instance with the same code already exists."),
    SCHEDULE_INSTANCE_REQUEST_REQUIRED("BKG_400_402", "Schedule instance request required", "The schedule instance request body is required."),
    SCHEDULE_INSTANCE_REQUEST_INVALID("BKG_400_403", "Schedule instance request invalid", "The schedule instance request is invalid."),
    SCHEDULE_INSTANCE_PRODUCT_REQUIRED("BKG_400_404", "Schedule instance product required", "The product is required for the schedule instance."),
    SCHEDULE_INSTANCE_TIMEZONE_REQUIRED("BKG_400_405", "Schedule instance timezone required", "The timezone is required."),
    SCHEDULE_INSTANCE_WINDOW_INVALID("BKG_400_406", "Schedule instance window invalid", "The schedule time window is invalid."),
    SCHEDULE_INSTANCE_INACTIVE("BKG_403_407", "Schedule instance inactive", "The schedule instance is inactive."),
    SCHEDULE_INSTANCE_CLOSED("BKG_403_408", "Schedule instance closed", "The schedule instance is closed."),
    SCHEDULE_INSTANCE_CANCELLED("BKG_403_409", "Schedule instance cancelled", "The schedule instance has been cancelled."),

    // -------------------------------------------------
    // Capacity Pool
    // -------------------------------------------------
    CAPACITY_POOL_NOT_FOUND("BKG_404_420", "Capacity pool not found", "The capacity pool was not found."),
    CAPACITY_POOL_ALREADY_EXISTS("BKG_400_421", "Capacity pool already exists", "A capacity pool with the same code already exists."),
    CAPACITY_POOL_REQUEST_REQUIRED("BKG_400_422", "Capacity pool request required", "The capacity pool request body is required."),
    CAPACITY_POOL_REQUEST_INVALID("BKG_400_423", "Capacity pool request invalid", "The capacity pool request is invalid."),
    CAPACITY_POOL_TOTAL_INVALID("BKG_400_424", "Capacity pool total invalid", "The total capacity is invalid."),
    CAPACITY_POOL_INSUFFICIENT("BKG_409_425", "Capacity pool insufficient", "There is not enough capacity available."),
    CAPACITY_POOL_CLOSED("BKG_403_426", "Capacity pool closed", "The capacity pool is closed."),
    CAPACITY_POOL_STATUS_INVALID("BKG_400_427", "Capacity pool status invalid", "The capacity pool status is invalid."),

    // -------------------------------------------------
    // Reservation Hold
    // -------------------------------------------------
    RESERVATION_HOLD_NOT_FOUND("BKG_404_500", "Reservation hold not found", "The reservation hold was not found."),
    RESERVATION_HOLD_ALREADY_EXISTS("BKG_400_501", "Reservation hold already exists", "A reservation hold with the same reference already exists."),
    RESERVATION_HOLD_REQUEST_REQUIRED("BKG_400_502", "Reservation hold request required", "The reservation hold request body is required."),
    RESERVATION_HOLD_REQUEST_INVALID("BKG_400_503", "Reservation hold request invalid", "The reservation hold request is invalid."),
    RESERVATION_HOLD_QUANTITY_INVALID("BKG_400_504", "Reservation hold quantity invalid", "The hold quantity is invalid."),
    RESERVATION_HOLD_EXPIRED("BKG_403_505", "Reservation hold expired", "The reservation hold has expired."),
    RESERVATION_HOLD_RELEASED("BKG_403_506", "Reservation hold released", "The reservation hold has been released."),
    RESERVATION_HOLD_CONSUMED("BKG_409_507", "Reservation hold consumed", "The reservation hold has already been consumed."),
    RESERVATION_HOLD_CAPACITY_UNAVAILABLE("BKG_409_508", "Reservation hold capacity unavailable", "Capacity is not available for the hold."),

    // -------------------------------------------------
    // Booking
    // -------------------------------------------------
    BOOKING_NOT_FOUND("BKG_404_600", "Booking not found", "The booking was not found."),
    BOOKING_ALREADY_EXISTS("BKG_400_601", "Booking already exists", "A booking with the same number already exists."),
    BOOKING_REQUEST_REQUIRED("BKG_400_602", "Booking request required", "The booking request body is required."),
    BOOKING_REQUEST_INVALID("BKG_400_603", "Booking request invalid", "The booking request is invalid."),
    BOOKING_ORDER_REQUIRED("BKG_400_604", "Booking order required", "The order is required for the booking."),
    BOOKING_CUSTOMER_REQUIRED("BKG_400_605", "Booking customer required", "The customer is required for the booking."),
    BOOKING_DATE_REQUIRED("BKG_400_606", "Booking date required", "The booking date is required."),
    BOOKING_ALREADY_CANCELLED("BKG_403_607", "Booking already cancelled", "The booking has already been cancelled."),
    BOOKING_ALREADY_COMPLETED("BKG_403_608", "Booking already completed", "The booking has already been completed."),
    BOOKING_STATUS_INVALID("BKG_400_609", "Booking status invalid", "The booking status is invalid."),
    BOOKING_CAPACITY_UNAVAILABLE("BKG_409_610", "Booking capacity unavailable", "There is no capacity available for the booking."),
    BOOKING_RESCHEDULE_NOT_ALLOWED("BKG_403_611", "Booking reschedule not allowed", "This booking cannot be rescheduled."),
    BOOKING_CANCELLATION_NOT_ALLOWED("BKG_403_612", "Booking cancellation not allowed", "This booking cannot be cancelled."),

    // -------------------------------------------------
    // Booking Item
    // -------------------------------------------------
    BOOKING_ITEM_NOT_FOUND("BKG_404_620", "Booking item not found", "The booking item was not found."),
    BOOKING_ITEM_REQUEST_REQUIRED("BKG_400_621", "Booking item request required", "The booking item request body is required."),
    BOOKING_ITEM_REQUEST_INVALID("BKG_400_622", "Booking item request invalid", "The booking item request is invalid."),
    BOOKING_ITEM_SCHEDULE_REQUIRED("BKG_400_623", "Booking item schedule required", "The schedule instance is required."),
    BOOKING_ITEM_QUANTITY_INVALID("BKG_400_624", "Booking item quantity invalid", "The booking item quantity is invalid."),
    BOOKING_ITEM_WINDOW_INVALID("BKG_400_625", "Booking item window invalid", "The booking item time window is invalid."),
    BOOKING_ITEM_STATUS_INVALID("BKG_400_626", "Booking item status invalid", "The booking item status is invalid."),

    // -------------------------------------------------
    // Booking Guest
    // -------------------------------------------------
    BOOKING_GUEST_NOT_FOUND("BKG_404_630", "Booking guest not found", "The booking guest was not found."),
    BOOKING_GUEST_REQUEST_REQUIRED("BKG_400_631", "Booking guest request required", "The booking guest request body is required."),
    BOOKING_GUEST_REQUEST_INVALID("BKG_400_632", "Booking guest request invalid", "The booking guest request is invalid."),
    BOOKING_GUEST_NAME_REQUIRED("BKG_400_633", "Booking guest name required", "The guest name is required."),
    BOOKING_GUEST_TYPE_REQUIRED("BKG_400_634", "Booking guest type required", "The guest type is required."),

    // -------------------------------------------------
    // Entitlement
    // -------------------------------------------------
    ENTITLEMENT_NOT_FOUND("BKG_404_700", "Entitlement not found", "The entitlement was not found."),
    ENTITLEMENT_ALREADY_EXISTS("BKG_400_701", "Entitlement already exists", "An entitlement with the same number already exists."),
    ENTITLEMENT_REQUEST_REQUIRED("BKG_400_702", "Entitlement request required", "The entitlement request body is required."),
    ENTITLEMENT_REQUEST_INVALID("BKG_400_703", "Entitlement request invalid", "The entitlement request is invalid."),
    ENTITLEMENT_TYPE_REQUIRED("BKG_400_704", "Entitlement type required", "The entitlement type is required."),
    ENTITLEMENT_ALREADY_USED("BKG_409_705", "Entitlement already used", "The entitlement has already been used."),
    ENTITLEMENT_EXPIRED("BKG_403_706", "Entitlement expired", "The entitlement has expired."),
    ENTITLEMENT_CANCELLED("BKG_403_707", "Entitlement cancelled", "The entitlement has been cancelled."),
    ENTITLEMENT_REVOKED("BKG_403_708", "Entitlement revoked", "The entitlement has been revoked."),
    ENTITLEMENT_USAGE_LIMIT_EXCEEDED("BKG_403_709", "Entitlement usage limit exceeded", "The entitlement usage limit has been exceeded."),
    ENTITLEMENT_WINDOW_INVALID("BKG_400_710", "Entitlement valid window invalid", "The entitlement validity window is invalid."),

    // -------------------------------------------------
    // Access Pass
    // -------------------------------------------------
    ACCESS_PASS_NOT_FOUND("BKG_404_720", "Access pass not found", "The access pass was not found."),
    ACCESS_PASS_ALREADY_EXISTS("BKG_400_721", "Access pass already exists", "An access pass with the same number already exists."),
    ACCESS_PASS_REQUEST_REQUIRED("BKG_400_722", "Access pass request required", "The access pass request body is required."),
    ACCESS_PASS_REQUEST_INVALID("BKG_400_723", "Access pass request invalid", "The access pass request is invalid."),
    ACCESS_PASS_TYPE_REQUIRED("BKG_400_724", "Access pass type required", "The access pass type is required."),
    ACCESS_PASS_EXPIRED("BKG_403_725", "Access pass expired", "The access pass has expired."),
    ACCESS_PASS_CANCELLED("BKG_403_726", "Access pass cancelled", "The access pass has been cancelled."),
    ACCESS_PASS_REVOKED("BKG_403_727", "Access pass revoked", "The access pass has been revoked."),
    ACCESS_PASS_INVALID("BKG_400_728", "Access pass invalid", "The access pass is invalid."),

    // -------------------------------------------------
    // Check-in
    // -------------------------------------------------
    CHECK_IN_NOT_FOUND("BKG_404_740", "Check-in not found", "The check-in was not found."),
    CHECK_IN_REQUEST_REQUIRED("BKG_400_741", "Check-in request required", "The check-in request body is required."),
    CHECK_IN_REQUEST_INVALID("BKG_400_742", "Check-in request invalid", "The check-in request is invalid."),
    CHECK_IN_ENTITLEMENT_REQUIRED("BKG_400_743", "Check-in entitlement required", "The entitlement is required for check-in."),
    CHECK_IN_ALREADY_DONE("BKG_409_744", "Check-in already done", "The guest has already been checked in."),
    CHECK_IN_DENIED("BKG_403_745", "Check-in denied", "Check-in was denied."),
    CHECK_IN_LOCATION_REQUIRED("BKG_400_746", "Check-in location required", "The check-in location is required."),
    CHECK_IN_DEVICE_REQUIRED("BKG_400_747", "Check-in device required", "The check-in device is required."),

    // -------------------------------------------------
    // Entitlement Usage Event
    // -------------------------------------------------
    ENTITLEMENT_USAGE_EVENT_NOT_FOUND("BKG_404_760", "Entitlement usage event not found", "The entitlement usage event was not found."),
    ENTITLEMENT_USAGE_EVENT_REQUEST_REQUIRED("BKG_400_761", "Entitlement usage event request required", "The entitlement usage event request body is required."),
    ENTITLEMENT_USAGE_EVENT_REQUEST_INVALID("BKG_400_762", "Entitlement usage event request invalid", "The entitlement usage event request is invalid."),
    ENTITLEMENT_USAGE_EVENT_TYPE_REQUIRED("BKG_400_763", "Entitlement usage event type required", "The usage event type is required."),

    // -------------------------------------------------
    // Cancellation
    // -------------------------------------------------
    CANCELLATION_NOT_FOUND("BKG_404_800", "Cancellation not found", "The cancellation record was not found."),
    CANCELLATION_REQUEST_REQUIRED("BKG_400_801", "Cancellation request required", "The cancellation request body is required."),
    CANCELLATION_REQUEST_INVALID("BKG_400_802", "Cancellation request invalid", "The cancellation request is invalid."),
    CANCELLATION_REASON_REQUIRED("BKG_400_803", "Cancellation reason required", "The cancellation reason is required."),
    CANCELLATION_ALREADY_COMPLETED("BKG_409_804", "Cancellation already completed", "The cancellation has already been completed."),
    CANCELLATION_NOT_ALLOWED("BKG_403_805", "Cancellation not allowed", "Cancellation is not allowed."),
    CANCELLATION_REFUND_NOT_ELIGIBLE("BKG_403_806", "Cancellation refund not eligible", "This cancellation is not eligible for refund."),

    // -------------------------------------------------
    // Reschedule
    // -------------------------------------------------
    RESCHEDULE_NOT_FOUND("BKG_404_820", "Reschedule not found", "The reschedule record was not found."),
    RESCHEDULE_REQUEST_REQUIRED("BKG_400_821", "Reschedule request required", "The reschedule request body is required."),
    RESCHEDULE_REQUEST_INVALID("BKG_400_822", "Reschedule request invalid", "The reschedule request is invalid."),
    RESCHEDULE_OLD_SCHEDULE_REQUIRED("BKG_400_823", "Reschedule old schedule required", "The old schedule instance is required."),
    RESCHEDULE_NEW_SCHEDULE_REQUIRED("BKG_400_824", "Reschedule new schedule required", "The new schedule instance is required."),
    RESCHEDULE_SAME_SCHEDULE("BKG_400_825", "Reschedule same schedule", "The new schedule cannot be the same as the old schedule."),
    RESCHEDULE_WINDOW_INVALID("BKG_400_826", "Reschedule window invalid", "The new reschedule window is invalid."),
    RESCHEDULE_NOT_ALLOWED("BKG_403_827", "Reschedule not allowed", "Rescheduling is not allowed."),
    RESCHEDULE_CAPACITY_UNAVAILABLE("BKG_409_828", "Reschedule capacity unavailable", "Capacity is not available for the new schedule."),
    RESCHEDULE_ALREADY_COMPLETED("BKG_409_829", "Reschedule already completed", "The reschedule has already been completed."),

    // -------------------------------------------------
    // Refund
    // -------------------------------------------------
    REFUND_NOT_FOUND("BKG_404_900", "Refund not found", "The refund was not found."),
    REFUND_ALREADY_EXISTS("BKG_400_901", "Refund already exists", "A refund with the same reference already exists."),
    REFUND_REQUEST_REQUIRED("BKG_400_902", "Refund request required", "The refund request body is required."),
    REFUND_REQUEST_INVALID("BKG_400_903", "Refund request invalid", "The refund request is invalid."),
    REFUND_ORDER_REQUIRED("BKG_400_904", "Refund order required", "The order is required for the refund."),
    REFUND_AMOUNT_INVALID("BKG_400_905", "Refund amount invalid", "The refund amount is invalid."),
    REFUND_EXCEEDS_PAID_AMOUNT("BKG_400_906", "Refund exceeds paid amount", "The refund amount exceeds the paid amount."),
    REFUND_NOT_ELIGIBLE("BKG_403_907", "Refund not eligible", "The order is not eligible for refund."),
    REFUND_ALREADY_COMPLETED("BKG_409_908", "Refund already completed", "The refund has already been completed."),
    REFUND_FAILED("BKG_500_909", "Refund failed", "The refund failed to process."),
    REFUND_STATUS_INVALID("BKG_400_910", "Refund status invalid", "The refund status is invalid."),

    // -------------------------------------------------
    // Refund Item
    // -------------------------------------------------
    REFUND_ITEM_NOT_FOUND("BKG_404_920", "Refund item not found", "The refund item was not found."),
    REFUND_ITEM_REQUEST_REQUIRED("BKG_400_921", "Refund item request required", "The refund item request body is required."),
    REFUND_ITEM_REQUEST_INVALID("BKG_400_922", "Refund item request invalid", "The refund item request is invalid."),
    REFUND_ITEM_QUANTITY_INVALID("BKG_400_923", "Refund item quantity invalid", "The refund item quantity is invalid."),
    REFUND_ITEM_AMOUNT_INVALID("BKG_400_924", "Refund item amount invalid", "The refund item amount is invalid.");

    private final String code;
    private final String statusMessage;
    private final String customerMessage;

    BookingErrorType(String code, String statusMessage, String customerMessage) {
        this.code = code;
        this.statusMessage = statusMessage;
        this.customerMessage = customerMessage;
    }

    public String formatCustomerMessage(Object... args) {
        try {
            return String.format(this.customerMessage, args);
        } catch (Exception e) {
            log.warn("Failed to format customer message for code={} template={}", code, customerMessage, e);
            return this.customerMessage;
        }
    }

    public String formatStatusMessage(Object... args) {
        try {
            return String.format(this.statusMessage, args);
        } catch (Exception e) {
            log.warn("Failed to format status message for code={} template={}", code, statusMessage, e);
            return this.statusMessage;
        }
    }
}
