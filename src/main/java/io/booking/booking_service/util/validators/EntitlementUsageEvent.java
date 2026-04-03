package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.datatype.booking.UsageEventType;
import io.booking.booking_service.model.EntitlementUsageEventEntity;
import io.booking.booking_service.repository.EntitlementUsageEventRepository;
import io.booking.booking_service.util.SharedUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntitlementUsageEvent {

    private final SharedUtils sharedUtils;
    private final EntitlementUsageEventRepository repository;

    /** Load usage event by id, fail with NOT_FOUND if missing */
    public Mono<EntitlementUsageEventEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_USAGE_EVENT_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_USAGE_EVENT_NOT_FOUND)));
    }

    /** Validate entitlement id is present */
    public Mono<Void> validateEntitlementRequired(UUID entitlementId) {
        if (entitlementId == null)
            return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_NOT_FOUND));
        return Mono.empty();
    }

    /** Validate event type is present */
    public Mono<Void> validateEventTypeRequired(UsageEventType eventType) {
        if (eventType == null)
            return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_USAGE_EVENT_TYPE_REQUIRED));
        return Mono.empty();
    }

    /** Validate usage delta is non-null and non-zero */
    public Mono<Void> validateUsageDelta(Integer usageDelta) {
        if (usageDelta == null || usageDelta == 0)
            return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_USAGE_EVENT_REQUEST_INVALID));
        return Mono.empty();
    }

    /** Validate location code is present */
    public Mono<Void> validateLocationCode(String locationCode) {
        if (sharedUtils.isNullOrEmptyOrBlank(locationCode))
            return Mono.error(BookingException.of(BookingErrorType.CHECK_IN_LOCATION_REQUIRED));
        return Mono.empty();
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
