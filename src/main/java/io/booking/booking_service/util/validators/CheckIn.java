package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CheckInResultStatus;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.CheckInEntity;
import io.booking.booking_service.repository.CheckInRepository;
import io.booking.booking_service.util.SharedUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckIn {

    private final SharedUtils sharedUtils;
    private final CheckInRepository repository;

    /** Load check-in by id, fail with NOT_FOUND if missing */
    public Mono<CheckInEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.CHECK_IN_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.CHECK_IN_NOT_FOUND)));
    }

    /** Validate entitlement is linked */
    public Mono<Void> validateEntitlementRequired(UUID entitlementId) {
        if (entitlementId == null)
            return Mono.error(BookingException.of(BookingErrorType.CHECK_IN_ENTITLEMENT_REQUIRED));
        return Mono.empty();
    }

    /** Validate location code is present */
    public Mono<Void> validateLocationRequired(String locationCode) {
        if (sharedUtils.isNullOrEmptyOrBlank(locationCode))
            return Mono.error(BookingException.of(BookingErrorType.CHECK_IN_LOCATION_REQUIRED));
        return Mono.empty();
    }

    /** Validate device code is present */
    public Mono<Void> validateDeviceRequired(String deviceCode) {
        if (sharedUtils.isNullOrEmptyOrBlank(deviceCode))
            return Mono.error(BookingException.of(BookingErrorType.CHECK_IN_DEVICE_REQUIRED));
        return Mono.empty();
    }

    /** Validate actor type is present */
    public Mono<Void> validateActorTypeRequired(ActorType actorType) {
        if (actorType == null)
            return Mono.error(BookingException.of(BookingErrorType.CHECK_IN_REQUEST_INVALID));
        return Mono.empty();
    }

    /** Assert guest has not already been checked in for this entitlement */
    public Mono<Void> assertNotAlreadyCheckedIn(UUID entitlementId) {
        if (entitlementId == null) return Mono.empty();
        return repository.findByEntitlementId(entitlementId)
                .filter(c -> CheckInResultStatus.GRANTED.equals(c.getResultStatus()))
                .hasElements()
                .flatMap(exists -> exists
                        ? Mono.error(BookingException.of(BookingErrorType.CHECK_IN_ALREADY_DONE))
                        : Mono.empty());
    }

    /** Assert check-in result was not denied */
    public Mono<CheckInEntity> assertNotDenied(CheckInEntity checkIn) {
        if (CheckInResultStatus.DENIED.equals(checkIn.getResultStatus()))
            return Mono.error(BookingException.of(BookingErrorType.CHECK_IN_DENIED));
        return Mono.just(checkIn);
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return java.util.UUID.randomUUID().toString();
    }
}
