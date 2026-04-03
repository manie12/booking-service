package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.AdjustmentType;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.CartAdjustmentEntity;
import io.booking.booking_service.repository.CartAdjustmentRepository;
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
public class CartAdjustment {

    private final SharedUtils sharedUtils;
    private final CartAdjustmentRepository repository;

    /** Load cart adjustment by id, fail with NOT_FOUND if missing */
    public Mono<CartAdjustmentEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.CART_ADJUSTMENT_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.CART_ADJUSTMENT_NOT_FOUND)));
    }

    /** Validate adjustment type is present */
    public Mono<Void> validateTypeRequired(AdjustmentType adjustmentType) {
        if (adjustmentType == null)
            return Mono.error(BookingException.of(BookingErrorType.CART_ADJUSTMENT_TYPE_REQUIRED));
        return Mono.empty();
    }

    /** Validate cart id is present */
    public Mono<Void> validateCartRequired(UUID cartId) {
        if (cartId == null)
            return Mono.error(BookingException.of(BookingErrorType.CART_NOT_FOUND));
        return Mono.empty();
    }

    /** Validate amount is non-null (DISCOUNT must be <= 0, SURCHARGE/MARKUP must be >= 0) */
    public Mono<Void> validateAmount(BigDecimal amount, AdjustmentType adjustmentType) {
        if (amount == null)
            return Mono.error(BookingException.of(BookingErrorType.CART_ADJUSTMENT_AMOUNT_INVALID));
        if (AdjustmentType.DISCOUNT.equals(adjustmentType) && amount.compareTo(BigDecimal.ZERO) > 0)
            return Mono.error(BookingException.of(BookingErrorType.CART_ADJUSTMENT_AMOUNT_INVALID));
        if ((AdjustmentType.SURCHARGE.equals(adjustmentType) || AdjustmentType.MARKUP.equals(adjustmentType))
                && amount.compareTo(BigDecimal.ZERO) < 0)
            return Mono.error(BookingException.of(BookingErrorType.CART_ADJUSTMENT_AMOUNT_INVALID));
        return Mono.empty();
    }

    /** Assert adjustment belongs to the given cart */
    public Mono<CartAdjustmentEntity> assertBelongsToCart(CartAdjustmentEntity adjustment, UUID cartId) {
        if (!cartId.equals(adjustment.getCartId()))
            return Mono.error(BookingException.of(BookingErrorType.CART_ADJUSTMENT_REQUEST_INVALID));
        return Mono.just(adjustment);
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
