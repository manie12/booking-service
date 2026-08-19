package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.AdjustmentType;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.CartAdjustmentEntity;
import io.booking.booking_service.repository.CartAdjustmentRepository;
import io.booking.booking_service.repository.CartItemRepository;
import io.booking.booking_service.repository.CartRepository;
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
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    // ============================================================
    // LOAD
    // ============================================================

    /**
     * Load cart adjustment by ID.
     */
    public Mono<CartAdjustmentEntity> load(UUID id) {

        if (id == null) {
            return Mono.error(
                    BookingException.of(
                            BookingErrorType.CART_ADJUSTMENT_NOT_FOUND
                    )
            );
        }

        return repository
                .findById(id)
                .switchIfEmpty(
                        Mono.error(
                                BookingException.of(
                                        BookingErrorType.CART_ADJUSTMENT_NOT_FOUND
                                )
                        )
                );
    }

    // ============================================================
    // CART VALIDATION
    // ============================================================

    /**
     * Validate cart ID is supplied.
     */
    public Mono<Void> validateCartRequired(UUID cartId) {

        if (cartId == null) {
            return Mono.error(
                    BookingException.of(
                            BookingErrorType.CART_NOT_FOUND
                    )
            );
        }

        return Mono.empty();
    }

    /**
     * Validate that the supplied cart actually exists.
     *
     * This prevents PostgreSQL FK errors such as:
     * fk_cart_adjustments_cart
     */
    public Mono<Void> validateCartExists(UUID cartId) {

        if (cartId == null) {
            return Mono.error(
                    BookingException.of(
                            BookingErrorType.CART_NOT_FOUND
                    )
            );
        }

        return cartRepository
                .existsById(cartId)
                .flatMap(exists -> {

                    if (!exists) {

                        log.warn(
                                "[validateCartExists] Cart not found cartId={}",
                                cartId
                        );

                        return Mono.error(
                                BookingException.of(
                                        BookingErrorType.CART_NOT_FOUND
                                )
                        );
                    }

                    return Mono.empty();
                });
    }

    // ============================================================
    // CART ITEM VALIDATION
    // ============================================================

    /**
     * Validate cart item exists when cartItemId is supplied.
     *
     * cartItemId is optional because an adjustment may apply to
     * the whole cart instead of a particular item.
     */
    public Mono<Void> validateCartItemExists(UUID cartItemId) {

        if (cartItemId == null) {
            return Mono.empty();
        }

        return cartItemRepository
                .existsById(cartItemId)
                .flatMap(exists -> {

                    if (!exists) {

                        log.warn(
                                "[validateCartItemExists] Cart item not found cartItemId={}",
                                cartItemId
                        );

                        return Mono.error(
                                BookingException.of(
                                        BookingErrorType.CART_ITEM_NOT_FOUND
                                )
                        );
                    }

                    return Mono.empty();
                });
    }

    // ============================================================
    // ADJUSTMENT TYPE
    // ============================================================

    /**
     * Validate adjustment type is supplied.
     */
    public Mono<Void> validateTypeRequired(
            AdjustmentType adjustmentType
    ) {

        if (adjustmentType == null) {
            return Mono.error(
                    BookingException.of(
                            BookingErrorType.CART_ADJUSTMENT_REQUEST_INVALID
                    )
            );
        }

        return Mono.empty();
    }

    // ============================================================
    // AMOUNT
    // ============================================================

    /**
     * Validate adjustment amount.
     */
    public Mono<Void> validateAmount(
            BigDecimal amount,
            AdjustmentType adjustmentType
    ) {

        if (amount == null) {
            return Mono.error(
                    BookingException.of(
                            BookingErrorType.CART_ADJUSTMENT_REQUEST_INVALID
                    )
            );
        }

        if (adjustmentType == null) {
            return Mono.error(
                    BookingException.of(
                            BookingErrorType.CART_ADJUSTMENT_REQUEST_INVALID
                    )
            );
        }

        /*
         * Do not allow zero-valued adjustments.
         */
        if (amount.compareTo(BigDecimal.ZERO) == 0) {

            log.warn(
                    "[validateAmount] Adjustment amount cannot be zero adjustmentType={} amount={}",
                    adjustmentType,
                    amount
            );

            return Mono.error(
                    BookingException.of(
                            BookingErrorType.CART_ADJUSTMENT_REQUEST_INVALID
                    )
            );
        }

        return Mono.empty();
    }

    // ============================================================
    // REQUEST ID
    // ============================================================

    /**
     * Resolve request ID using:
     *
     * 1. payload request ID
     * 2. header request ID
     * 3. generated UUID
     */
    public String resolveRequestId(
            String header,
            String payload
    ) {

        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) {
            return payload;
        }

        if (!sharedUtils.isNullOrEmptyOrBlank(header)) {
            return header;
        }

        return UUID.randomUUID().toString();
    }
}