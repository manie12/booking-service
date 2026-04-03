package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.BookingStatus;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.BookingStatusHistoryEntity;
import io.booking.booking_service.util.SharedUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingStatusHistory {

    private final SharedUtils sharedUtils;
    private final io.booking.booking_service.repository.BookingStatusHistory repository;

    /** Load booking status history entry by id, fail with NOT_FOUND if missing */
    public Mono<BookingStatusHistoryEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.BOOKING_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.BOOKING_NOT_FOUND)));
    }

    /** Validate booking id is present */
    public Mono<Void> validateBookingRequired(UUID bookingId) {
        if (bookingId == null)
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_NOT_FOUND));
        return Mono.empty();
    }

    /** Validate new status is present */
    public Mono<Void> validateNewStatusRequired(BookingStatus newStatus) {
        if (newStatus == null)
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_STATUS_INVALID));
        return Mono.empty();
    }

    /** Validate actor type is present */
    public Mono<Void> validateActorTypeRequired(ActorType actorType) {
        if (actorType == null)
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_REQUEST_INVALID));
        return Mono.empty();
    }

    /** Validate actor id is present */
    public Mono<Void> validateActorIdRequired(String actorId) {
        if (sharedUtils.isNullOrEmptyOrBlank(actorId))
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_REQUEST_INVALID));
        return Mono.empty();
    }

    /** Assert new status differs from old status */
    public Mono<Void> assertStatusTransition(BookingStatus oldStatus, BookingStatus newStatus) {
        if (oldStatus != null && oldStatus.equals(newStatus))
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_STATUS_INVALID));
        return Mono.empty();
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
