package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.BookingStatus;
import io.booking.booking_service.datatype.booking.GuestType;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.BookingGuestEntity;
import io.booking.booking_service.repository.BookingGuestRepository;
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
public class BookingGuest {

    private final SharedUtils sharedUtils;
    private final BookingGuestRepository repository;

    /** Load booking guest by id, fail with NOT_FOUND if missing */
    public Mono<BookingGuestEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.BOOKING_GUEST_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.BOOKING_GUEST_NOT_FOUND)));
    }

    /** Validate guest first and last name are present */
    public Mono<Void> validateNameRequired(String firstName, String lastName) {
        if (sharedUtils.isNullOrEmptyOrBlank(firstName) || sharedUtils.isNullOrEmptyOrBlank(lastName))
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_GUEST_NAME_REQUIRED));
        return Mono.empty();
    }

    /** Validate guest type is present */
    public Mono<Void> validateGuestTypeRequired(GuestType guestType) {
        if (guestType == null)
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_GUEST_TYPE_REQUIRED));
        return Mono.empty();
    }

    /** Validate booking item id is present */
    public Mono<Void> validateBookingItemRequired(UUID bookingItemId) {
        if (bookingItemId == null)
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_ITEM_NOT_FOUND));
        return Mono.empty();
    }

    /** Validate date of birth is not in the future */
    public Mono<Void> validateDateOfBirth(LocalDate dob) {
        if (dob != null && dob.isAfter(LocalDate.now()))
            return Mono.error(BookingException.of(BookingErrorType.GUEST_PROFILE_DOB_INVALID));
        return Mono.empty();
    }

    /** Assert guest is not cancelled */
    public Mono<BookingGuestEntity> assertNotCancelled(BookingGuestEntity guest) {
        if (BookingStatus.CANCELLED.equals(guest.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_ALREADY_CANCELLED));
        return Mono.just(guest);
    }

    /** Assert guest belongs to the given booking item */
    public Mono<BookingGuestEntity> assertBelongsToBookingItem(BookingGuestEntity guest, UUID bookingItemId) {
        if (!bookingItemId.equals(guest.getBookingItemId()))
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_GUEST_REQUEST_INVALID));
        return Mono.just(guest);
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
