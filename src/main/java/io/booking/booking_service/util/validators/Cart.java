package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.booking.CartStatus;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.CartEntity;
import io.booking.booking_service.repository.CartRepository;
import io.booking.booking_service.util.SharedUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class Cart {

    private final SharedUtils sharedUtils;
    private final CartRepository repository;

    /** Ensure cart number is unique; ignoreId=null on create */
    public Mono<Void> ensureUniqueNumber(String cartNumber, UUID ignoreId) {
        if (sharedUtils.isNullOrEmptyOrBlank(cartNumber))
            return Mono.error(BookingException.of(BookingErrorType.CART_NOT_FOUND));
        return repository.findByCartNumber(cartNumber).flatMap(e -> {
            if (ignoreId != null && ignoreId.equals(e.getId())) return Mono.empty();
            return Mono.error(BookingException.of(BookingErrorType.CART_ALREADY_EXISTS));
        }).then();
    }

    /** Load cart by id */
    public Mono<CartEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.CART_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.CART_NOT_FOUND)));
    }

    /** Load cart and assert it is ACTIVE */
    public Mono<CartEntity> loadActive(UUID id) {
        return load(id).flatMap(c -> {
            if (c.getStatus() == CartStatus.EXPIRED || (c.getExpiresAt() != null && c.getExpiresAt().isBefore(OffsetDateTime.now())))
                return Mono.error(BookingException.of(BookingErrorType.CART_EXPIRED));
            if (c.getStatus() != CartStatus.ACTIVE)
                return Mono.error(BookingException.of(BookingErrorType.CART_INACTIVE));
            return Mono.just(c);
        });
    }

    /** Assert cart is not currently in checkout */
    public Mono<CartEntity> assertNotInCheckout(CartEntity cart) {
        if (cart.getStatus() == CartStatus.CHECKOUT_IN_PROGRESS)
            return Mono.error(BookingException.of(BookingErrorType.CART_CHECKOUT_IN_PROGRESS));
        return Mono.just(cart);
    }

    /** Validate currency code is present */
    public Mono<Void> validateCurrency(String currencyCode) {
        if (sharedUtils.isNullOrEmptyOrBlank(currencyCode))
            return Mono.error(BookingException.of(BookingErrorType.CART_CURRENCY_REQUIRED));
        return Mono.empty();
    }

    /** Validate customer id is present */
    public Mono<Void> validateCustomer(UUID customerId) {
        if (customerId == null)
            return Mono.error(BookingException.of(BookingErrorType.CART_CUSTOMER_REQUIRED));
        return Mono.empty();
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
