package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.OrderStatus;
import io.booking.booking_service.datatype.booking.PaymentStatus;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.OrderEntity;
import io.booking.booking_service.repository.OrderRepository;
import io.booking.booking_service.util.SharedUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class Order {

    private final SharedUtils sharedUtils;
    private final OrderRepository repository;

    /** Load order by id, fail with NOT_FOUND if missing */
    public Mono<OrderEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.ORDER_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.ORDER_NOT_FOUND)));
    }

    /** Ensure order number is unique; pass ignoreId=null on create */
    public Mono<Void> ensureUniqueOrderNumber(String orderNumber, UUID ignoreId) {
        if (sharedUtils.isNullOrEmptyOrBlank(orderNumber))
            return Mono.error(BookingException.of(BookingErrorType.ORDER_REQUEST_INVALID));
        return repository.findByOrderNumber(orderNumber).flatMap(e -> {
            if (ignoreId != null && ignoreId.equals(e.getId())) return Mono.empty();
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ALREADY_EXISTS));
        }).then();
    }

    /** Assert order is not already cancelled */
    public Mono<OrderEntity> assertNotCancelled(OrderEntity order) {
        if (OrderStatus.CANCELLED.equals(order.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.ORDER_CANCELLED));
        return Mono.just(order);
    }

    /** Assert order is not already closed */
    public Mono<OrderEntity> assertNotClosed(OrderEntity order) {
        if (OrderStatus.CLOSED.equals(order.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.ORDER_CLOSED));
        return Mono.just(order);
    }

    /** Assert order has not already been paid (payment captured) */
    public Mono<OrderEntity> assertNotAlreadyPaid(OrderEntity order) {
        if (PaymentStatus.CAPTURED.equals(order.getPaymentStatus()))
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ALREADY_PAID));
        return Mono.just(order);
    }

    /** Assert order payment is not still pending (initiated) */
    public Mono<OrderEntity> assertPaymentNotPending(OrderEntity order) {
        if (PaymentStatus.INITIATED.equals(order.getPaymentStatus()))
            return Mono.error(BookingException.of(BookingErrorType.ORDER_PAYMENT_PENDING));
        return Mono.just(order);
    }

    /** Validate required customer */
    public Mono<Void> validateCustomerRequired(UUID customerId) {
        if (customerId == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_CUSTOMER_REQUIRED));
        return Mono.empty();
    }

    /** Validate required cart */
    public Mono<Void> validateCartRequired(UUID cartId) {
        if (cartId == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_CART_REQUIRED));
        return Mono.empty();
    }

    /** Validate idempotency key is present */
    public Mono<Void> validateIdempotencyKey(String key) {
        if (sharedUtils.isNullOrEmptyOrBlank(key))
            return Mono.error(BookingException.of(BookingErrorType.ORDER_IDEMPOTENCY_KEY_REQUIRED));
        return Mono.empty();
    }

    /** Validate total amount is non-null and >= 0 */
    public Mono<Void> validateTotal(BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) < 0)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_TOTAL_INVALID));
        return Mono.empty();
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
