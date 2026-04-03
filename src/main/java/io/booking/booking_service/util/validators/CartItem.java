package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.FulfillmentType;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.CartItemEntity;
import io.booking.booking_service.repository.CartItemRepository;
import io.booking.booking_service.util.SharedUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartItem {

    private final SharedUtils sharedUtils;
    private final CartItemRepository repository;

    /** Load cart item by id, fail with NOT_FOUND if missing */
    public Mono<CartItemEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.CART_ITEM_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.CART_ITEM_NOT_FOUND)));
    }

    /** Validate cart id is present */
    public Mono<Void> validateCartRequired(UUID cartId) {
        if (cartId == null)
            return Mono.error(BookingException.of(BookingErrorType.CART_NOT_FOUND));
        return Mono.empty();
    }

    /** Validate product id is present */
    public Mono<Void> validateProductRequired(UUID productId) {
        if (productId == null)
            return Mono.error(BookingException.of(BookingErrorType.CART_ITEM_PRODUCT_REQUIRED));
        return Mono.empty();
    }

    /** Validate quantity is >= 1 */
    public Mono<Void> validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1)
            return Mono.error(BookingException.of(BookingErrorType.CART_ITEM_QUANTITY_INVALID));
        return Mono.empty();
    }

    /** Validate service date is present */
    public Mono<Void> validateServiceDateRequired(LocalDate serviceDate) {
        if (serviceDate == null)
            return Mono.error(BookingException.of(BookingErrorType.CART_ITEM_SERVICE_DATE_REQUIRED));
        return Mono.empty();
    }

    /** Validate currency matches the cart currency */
    public Mono<Void> validateCurrencyMatch(String itemCurrency, String cartCurrency) {
        if (itemCurrency != null && !itemCurrency.equalsIgnoreCase(cartCurrency))
            return Mono.error(BookingException.of(BookingErrorType.CART_ITEM_CURRENCY_MISMATCH));
        return Mono.empty();
    }

    /** Validate unit price and total amount are >= 0 */
    public Mono<Void> validateAmounts(BigDecimal unitPrice, BigDecimal totalAmount) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0
                || totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) < 0)
            return Mono.error(BookingException.of(BookingErrorType.CART_ITEM_REQUEST_INVALID));
        return Mono.empty();
    }

    /** Validate fulfillment type is present */
    public Mono<Void> validateFulfillmentTypeRequired(FulfillmentType fulfillmentType) {
        if (fulfillmentType == null)
            return Mono.error(BookingException.of(BookingErrorType.CART_ITEM_REQUEST_INVALID));
        return Mono.empty();
    }

    /** Assert cart item belongs to the given cart */
    public Mono<CartItemEntity> assertBelongsToCart(CartItemEntity item, UUID cartId) {
        if (!cartId.equals(item.getCartId()))
            return Mono.error(BookingException.of(BookingErrorType.CART_ITEM_REQUEST_INVALID));
        return Mono.just(item);
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
