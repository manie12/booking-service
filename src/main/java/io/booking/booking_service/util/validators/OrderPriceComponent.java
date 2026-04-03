package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.PriceComponentType;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.OrderPriceComponentEntity;
import io.booking.booking_service.repository.OrderPriceComponentRepository;
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
public class OrderPriceComponent {

    private final SharedUtils sharedUtils;
    private final OrderPriceComponentRepository repository;

    /** Load order price component by id, fail with NOT_FOUND if missing */
    public Mono<OrderPriceComponentEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.ORDER_PRICE_COMPONENT_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.ORDER_PRICE_COMPONENT_NOT_FOUND)));
    }

    /** Validate component type is present */
    public Mono<Void> validateComponentTypeRequired(PriceComponentType componentType) {
        if (componentType == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_PRICE_COMPONENT_TYPE_REQUIRED));
        return Mono.empty();
    }

    /** Validate amount is non-null and >= 0 */
    public Mono<Void> validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_PRICE_COMPONENT_AMOUNT_INVALID));
        return Mono.empty();
    }

    /** Validate order item id is present */
    public Mono<Void> validateOrderItemRequired(UUID orderItemId) {
        if (orderItemId == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_NOT_FOUND));
        return Mono.empty();
    }

    /** Validate currency code is present */
    public Mono<Void> validateCurrencyRequired(String currencyCode) {
        if (sharedUtils.isNullOrEmptyOrBlank(currencyCode))
            return Mono.error(BookingException.of(BookingErrorType.ORDER_PRICE_COMPONENT_REQUEST_INVALID));
        return Mono.empty();
    }

    /** Assert component belongs to the given order item */
    public Mono<OrderPriceComponentEntity> assertBelongsToOrderItem(OrderPriceComponentEntity component, UUID orderItemId) {
        if (!orderItemId.equals(component.getOrderItemId()))
            return Mono.error(BookingException.of(BookingErrorType.ORDER_PRICE_COMPONENT_REQUEST_INVALID));
        return Mono.just(component);
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
