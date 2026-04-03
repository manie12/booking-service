package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.GuestType;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.OrderItemGuestEntity;
import io.booking.booking_service.repository.OrderItemGuestRepository;
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
public class OrderItemGuest {

    private final SharedUtils sharedUtils;
    private final OrderItemGuestRepository repository;

    /** Load order item guest by id, fail with NOT_FOUND if missing */
    public Mono<OrderItemGuestEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_GUEST_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_GUEST_NOT_FOUND)));
    }

    /** Validate guest name fields are present */
    public Mono<Void> validateNameRequired(String firstName, String lastName) {
        if (sharedUtils.isNullOrEmptyOrBlank(firstName) || sharedUtils.isNullOrEmptyOrBlank(lastName))
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_GUEST_NAME_REQUIRED));
        return Mono.empty();
    }

    /** Validate guest type is present */
    public Mono<Void> validateGuestTypeRequired(GuestType guestType) {
        if (guestType == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_GUEST_TYPE_REQUIRED));
        return Mono.empty();
    }

    /** Validate order item id is present */
    public Mono<Void> validateOrderItemRequired(UUID orderItemId) {
        if (orderItemId == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_NOT_FOUND));
        return Mono.empty();
    }

    /** Validate date of birth is not in the future */
    public Mono<Void> validateDateOfBirth(LocalDate dob) {
        if (dob != null && dob.isAfter(LocalDate.now()))
            return Mono.error(BookingException.of(BookingErrorType.GUEST_PROFILE_DOB_INVALID));
        return Mono.empty();
    }

    /** Assert guest belongs to the given order item */
    public Mono<OrderItemGuestEntity> assertBelongsToOrderItem(OrderItemGuestEntity guest, UUID orderItemId) {
        if (!orderItemId.equals(guest.getOrderItemId()))
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_GUEST_REQUEST_INVALID));
        return Mono.just(guest);
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
