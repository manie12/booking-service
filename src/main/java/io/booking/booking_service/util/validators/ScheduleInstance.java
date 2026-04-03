package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.ScheduleInstanceStatus;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.ScheduleInstanceEntity;
import io.booking.booking_service.repository.ScheduleInstanceRepository;
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
public class ScheduleInstance {

    private final SharedUtils sharedUtils;
    private final ScheduleInstanceRepository repository;

    /** Load schedule instance by id, fail with NOT_FOUND if missing */
    public Mono<ScheduleInstanceEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_NOT_FOUND)));
    }

    /** Ensure instance code is unique; pass ignoreId=null on create */
    public Mono<Void> ensureUniqueCode(String instanceCode, UUID ignoreId) {
        if (sharedUtils.isNullOrEmptyOrBlank(instanceCode))
            return Mono.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_REQUEST_INVALID));
        return repository.findByInstanceCode(instanceCode).flatMap(e -> {
            if (ignoreId != null && ignoreId.equals(e.getId())) return Mono.empty();
            return Mono.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_ALREADY_EXISTS));
        }).then();
    }

    /** Validate product is present */
    public Mono<Void> validateProductRequired(UUID productId) {
        if (productId == null)
            return Mono.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_PRODUCT_REQUIRED));
        return Mono.empty();
    }

    /** Validate timezone is present */
    public Mono<Void> validateTimezoneRequired(String timezone) {
        if (sharedUtils.isNullOrEmptyOrBlank(timezone))
            return Mono.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_TIMEZONE_REQUIRED));
        return Mono.empty();
    }

    /** Validate time window: startAt must be before endAt */
    public Mono<Void> validateTimeWindow(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (startAt == null || endAt == null || !startAt.isBefore(endAt))
            return Mono.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_WINDOW_INVALID));
        return Mono.empty();
    }

    /** Assert schedule instance is not in DRAFT (inactive/not yet active) state */
    public Mono<ScheduleInstanceEntity> assertNotInactive(ScheduleInstanceEntity instance) {
        if (ScheduleInstanceStatus.DRAFT.equals(instance.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_INACTIVE));
        return Mono.just(instance);
    }

    /** Assert schedule instance is not closed */
    public Mono<ScheduleInstanceEntity> assertNotClosed(ScheduleInstanceEntity instance) {
        if (ScheduleInstanceStatus.CLOSED.equals(instance.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_CLOSED));
        return Mono.just(instance);
    }

    /** Assert schedule instance is not cancelled */
    public Mono<ScheduleInstanceEntity> assertNotCancelled(ScheduleInstanceEntity instance) {
        if (ScheduleInstanceStatus.CANCELLED.equals(instance.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_CANCELLED));
        return Mono.just(instance);
    }

    /** Load and assert schedule instance is bookable (not inactive, closed, or cancelled) */
    public Mono<ScheduleInstanceEntity> loadBookable(UUID id) {
        return load(id)
                .flatMap(this::assertNotInactive)
                .flatMap(this::assertNotClosed)
                .flatMap(this::assertNotCancelled);
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
