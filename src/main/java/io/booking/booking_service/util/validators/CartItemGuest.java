package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.GuestType;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.CartItemGuestEntity;
import io.booking.booking_service.repository.CartItemGuestRepository;
import io.booking.booking_service.util.SharedUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartItemGuest {

    private final SharedUtils sharedUtils;
    private final CartItemGuestRepository repository;

    /** Load cart item guest by id, fail with NOT_FOUND if missing */
    public Mono<CartItemGuestEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.CART_ITEM_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.CART_ITEM_NOT_FOUND)));
    }

    /** Validate cart item id is present */
    public Mono<Void> validateCartItemRequired(UUID cartItemId) {
        if (cartItemId == null)
            return Mono.error(BookingException.of(BookingErrorType.CART_ITEM_NOT_FOUND));
        return Mono.empty();
    }

    /** Validate guest first and last name are present */
    public Mono<Void> validateNameRequired(String firstName, String lastName) {
        if (sharedUtils.isNullOrEmptyOrBlank(firstName) || sharedUtils.isNullOrEmptyOrBlank(lastName))
            return Mono.error(BookingException.of(BookingErrorType.CART_ITEM_REQUEST_INVALID));
        return Mono.empty();
    }

    /** Validate guest type is present */
    public Mono<Void> validateGuestTypeRequired(GuestType guestType) {
        if (guestType == null)
            return Mono.error(BookingException.of(BookingErrorType.CART_ITEM_REQUEST_INVALID));
        return Mono.empty();
    }

    /** Validate date of birth is not in the future */
    public Mono<Void> validateDateOfBirth(LocalDate dob) {
        if (dob != null && dob.isAfter(LocalDate.now()))
            return Mono.error(BookingException.of(BookingErrorType.GUEST_PROFILE_DOB_INVALID));
        return Mono.empty();
    }

    /** Assert guest is not duplicated within the same cart item */
    public Mono<Void> assertNoDuplicateGuest(UUID cartItemId, UUID guestProfileId) {
        if (cartItemId == null || guestProfileId == null) return Mono.empty();
        return repository.findByCartItemId(cartItemId)
                .filter(g -> guestProfileId.equals(g.getGuestProfileId()))
                .hasElements()
                .flatMap(exists -> exists
                        ? Mono.error(BookingException.of(BookingErrorType.CART_ITEM_DUPLICATE_GUEST))
                        : Mono.empty());
    }

    /** Assert guest belongs to the given cart item */
    public Mono<CartItemGuestEntity> assertBelongsToCartItem(CartItemGuestEntity guest, UUID cartItemId) {
        if (!cartItemId.equals(guest.getCartItemId()))
            return Mono.error(BookingException.of(BookingErrorType.CART_ITEM_REQUEST_INVALID));
        return Mono.just(guest);
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
