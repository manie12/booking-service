package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CancellationStatus;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.RescheduleEntity;
import io.booking.booking_service.repository.RescheduleRepository;
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
public class Reschedule {

    private final SharedUtils sharedUtils;
    private final RescheduleRepository repository;

    /** Load reschedule by id, fail with NOT_FOUND if missing */
    public Mono<RescheduleEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.RESCHEDULE_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.RESCHEDULE_NOT_FOUND)));
    }

    /** Validate booking is linked */
    public Mono<Void> validateBookingRequired(UUID bookingId) {
        if (bookingId == null)
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_NOT_FOUND));
        return Mono.empty();
    }

    /** Validate old schedule instance is present */
    public Mono<Void> validateOldScheduleRequired(UUID oldScheduleInstanceId) {
        if (oldScheduleInstanceId == null)
            return Mono.error(BookingException.of(BookingErrorType.RESCHEDULE_OLD_SCHEDULE_REQUIRED));
        return Mono.empty();
    }

    /** Validate new schedule instance is present */
    public Mono<Void> validateNewScheduleRequired(UUID newScheduleInstanceId) {
        if (newScheduleInstanceId == null)
            return Mono.error(BookingException.of(BookingErrorType.RESCHEDULE_NEW_SCHEDULE_REQUIRED));
        return Mono.empty();
    }

    /** Assert old and new schedule instances differ */
    public Mono<Void> assertSchedulesDiffer(UUID oldScheduleId, UUID newScheduleId) {
        if (oldScheduleId != null && oldScheduleId.equals(newScheduleId))
            return Mono.error(BookingException.of(BookingErrorType.RESCHEDULE_SAME_SCHEDULE));
        return Mono.empty();
    }

    /** Validate new start time is in the future */
    public Mono<Void> validateNewStartInFuture(OffsetDateTime newStartAt) {
        if (newStartAt != null && !newStartAt.isAfter(OffsetDateTime.now()))
            return Mono.error(BookingException.of(BookingErrorType.RESCHEDULE_WINDOW_INVALID));
        return Mono.empty();
    }

    /** Assert reschedule has not already been completed */
    public Mono<RescheduleEntity> assertNotAlreadyCompleted(RescheduleEntity reschedule) {
        if (CancellationStatus.COMPLETED.equals(reschedule.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.RESCHEDULE_ALREADY_COMPLETED));
        return Mono.just(reschedule);
    }

    /** Validate actor type is present */
    public Mono<Void> validateActorTypeRequired(ActorType actorType) {
        if (actorType == null)
            return Mono.error(BookingException.of(BookingErrorType.RESCHEDULE_REQUEST_INVALID));
        return Mono.empty();
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
