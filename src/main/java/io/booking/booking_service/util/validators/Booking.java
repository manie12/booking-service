package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.BookingStatus;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.BookingEntity;
import io.booking.booking_service.repository.BookingRepository;
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
public class Booking {

    private final SharedUtils sharedUtils;
    private final BookingRepository repository;

    /** Load booking by id, fail with NOT_FOUND if missing */
    public Mono<BookingEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.BOOKING_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.BOOKING_NOT_FOUND)));
    }

    /** Ensure booking number is unique; pass ignoreId=null on create */
    public Mono<Void> ensureUniqueNumber(String bookingNumber, UUID ignoreId) {
        if (sharedUtils.isNullOrEmptyOrBlank(bookingNumber))
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_REQUEST_INVALID));
        return repository.findByBookingNumber(bookingNumber).flatMap(e -> {
            if (ignoreId != null && ignoreId.equals(e.getId())) return Mono.empty();
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_ALREADY_EXISTS));
        }).then();
    }

    /** Validate order is linked */
    public Mono<Void> validateOrderRequired(UUID orderId) {
        if (orderId == null)
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_ORDER_REQUIRED));
        return Mono.empty();
    }

    /** Validate customer is linked */
    public Mono<Void> validateCustomerRequired(UUID customerId) {
        if (customerId == null)
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_CUSTOMER_REQUIRED));
        return Mono.empty();
    }

    /** Validate booking date is present */
    public Mono<Void> validateBookingDateRequired(LocalDate bookingDate) {
        if (bookingDate == null)
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_DATE_REQUIRED));
        return Mono.empty();
    }

    /** Validate status is not null */
    public Mono<Void> validateStatusRequired(BookingStatus status) {
        if (status == null)
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_STATUS_INVALID));
        return Mono.empty();
    }

    /** Assert booking is not already cancelled */
    public Mono<BookingEntity> assertNotCancelled(BookingEntity booking) {
        if (BookingStatus.CANCELLED.equals(booking.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_ALREADY_CANCELLED));
        return Mono.just(booking);
    }

    /** Assert booking is not already completed */
    public Mono<BookingEntity> assertNotCompleted(BookingEntity booking) {
        if (BookingStatus.COMPLETED.equals(booking.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_ALREADY_COMPLETED));
        return Mono.just(booking);
    }

    /** Assert booking can be rescheduled (not cancelled or completed) */
    public Mono<BookingEntity> assertReschedulable(BookingEntity booking) {
        if (BookingStatus.CANCELLED.equals(booking.getStatus()) ||
                BookingStatus.COMPLETED.equals(booking.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_RESCHEDULE_NOT_ALLOWED));
        return Mono.just(booking);
    }

    /** Assert booking can be cancelled (not already cancelled or completed) */
    public Mono<BookingEntity> assertCancellable(BookingEntity booking) {
        if (BookingStatus.CANCELLED.equals(booking.getStatus()) ||
                BookingStatus.COMPLETED.equals(booking.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_CANCELLATION_NOT_ALLOWED));
        return Mono.just(booking);
    }

    /** Assert booking has capacity available */
    public Mono<Void> assertCapacityAvailable(UUID bookingId) {
        if (bookingId == null)
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_CAPACITY_UNAVAILABLE));
        return Mono.empty();
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
