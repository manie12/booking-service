package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.FulfillmentType;
import io.booking.booking_service.datatype.booking.OrderStatus;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.OrderItemEntity;
import io.booking.booking_service.repository.OrderItemRepository;
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
public class OrderItem {

    private final SharedUtils sharedUtils;
    private final OrderItemRepository repository;

    /** Load order item by id, fail with NOT_FOUND if missing */
    public Mono<OrderItemEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_NOT_FOUND)));
    }

    /** Validate product is present */
    public Mono<Void> validateProductRequired(UUID productId) {
        if (productId == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_PRODUCT_REQUIRED));
        return Mono.empty();
    }

    /** Validate quantity is >= 1 */
    public Mono<Void> validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_QUANTITY_INVALID));
        return Mono.empty();
    }

    /** Validate amounts are non-null and >= 0 */
    public Mono<Void> validateAmounts(BigDecimal unitPrice, BigDecimal totalAmount) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0
                || totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_AMOUNT_INVALID));
        return Mono.empty();
    }

    /** Validate fulfillment type is present */
    public Mono<Void> validateFulfillmentTypeRequired(FulfillmentType fulfillmentType) {
        if (fulfillmentType == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_FULFILLMENT_TYPE_REQUIRED));
        return Mono.empty();
    }

    /** Validate status is not null */
    public Mono<Void> validateStatusRequired(OrderStatus status) {
        if (status == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_STATUS_INVALID));
        return Mono.empty();
    }

    /** Assert order item belongs to the given order */
    public Mono<OrderItemEntity> assertBelongsToOrder(OrderItemEntity item, UUID orderId) {
        if (!orderId.equals(item.getOrderId()))
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_REQUEST_INVALID));
        return Mono.just(item);
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
