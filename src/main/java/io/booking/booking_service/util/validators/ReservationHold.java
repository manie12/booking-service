package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.HoldStatus;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.ReservationHoldEntity;
import io.booking.booking_service.repository.ReservationHoldRepository;
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
public class ReservationHold {

    private final SharedUtils sharedUtils;
    private final ReservationHoldRepository repository;

    /** Load reservation hold by id, fail with NOT_FOUND if missing */
    public Mono<ReservationHoldEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.RESERVATION_HOLD_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.RESERVATION_HOLD_NOT_FOUND)));
    }

    /** Ensure hold reference is unique; pass ignoreId=null on create */
    public Mono<Void> ensureUniqueReference(String holdReference, UUID ignoreId) {
        if (sharedUtils.isNullOrEmptyOrBlank(holdReference))
            return Mono.error(BookingException.of(BookingErrorType.RESERVATION_HOLD_REQUEST_INVALID));
        return repository.findByHoldReference(holdReference).flatMap(e -> {
            if (ignoreId != null && ignoreId.equals(e.getId())) return Mono.empty();
            return Mono.error(BookingException.of(BookingErrorType.RESERVATION_HOLD_ALREADY_EXISTS));
        }).then();
    }

    /** Validate quantity is >= 1 */
    public Mono<Void> validateQuantityHeld(Integer quantityHeld) {
        if (quantityHeld == null || quantityHeld < 1)
            return Mono.error(BookingException.of(BookingErrorType.RESERVATION_HOLD_QUANTITY_INVALID));
        return Mono.empty();
    }

    /** Assert hold has not expired */
    public Mono<ReservationHoldEntity> assertNotExpired(ReservationHoldEntity hold) {
        if (HoldStatus.EXPIRED.equals(hold.getStatus()) ||
                (hold.getExpiresAt() != null && hold.getExpiresAt().isBefore(OffsetDateTime.now())))
            return Mono.error(BookingException.of(BookingErrorType.RESERVATION_HOLD_EXPIRED));
        return Mono.just(hold);
    }

    /** Assert hold has not been released */
    public Mono<ReservationHoldEntity> assertNotReleased(ReservationHoldEntity hold) {
        if (HoldStatus.RELEASED.equals(hold.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.RESERVATION_HOLD_RELEASED));
        return Mono.just(hold);
    }

    /** Assert hold has not been consumed */
    public Mono<ReservationHoldEntity> assertNotConsumed(ReservationHoldEntity hold) {
        if (HoldStatus.CONSUMED.equals(hold.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.RESERVATION_HOLD_CONSUMED));
        return Mono.just(hold);
    }

    /** Validate schedule instance is linked */
    public Mono<Void> validateScheduleInstanceRequired(UUID scheduleInstanceId) {
        if (scheduleInstanceId == null)
            return Mono.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_NOT_FOUND));
        return Mono.empty();
    }

    /** Validate capacity pool is linked */
    public Mono<Void> validateCapacityPoolRequired(UUID capacityPoolId) {
        if (capacityPoolId == null)
            return Mono.error(BookingException.of(BookingErrorType.CAPACITY_POOL_NOT_FOUND));
        return Mono.empty();
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
