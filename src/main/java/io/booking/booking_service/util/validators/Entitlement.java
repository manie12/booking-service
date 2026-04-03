package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.EntitlementStatus;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.EntitlementEntity;
import io.booking.booking_service.repository.EntitlementRepository;
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
public class Entitlement {

    private final SharedUtils sharedUtils;
    private final EntitlementRepository repository;

    /** Load entitlement by id, fail with NOT_FOUND if missing */
    public Mono<EntitlementEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_NOT_FOUND)));
    }

    /** Ensure entitlement number is unique; pass ignoreId=null on create */
    public Mono<Void> ensureUniqueNumber(String entitlementNumber, UUID ignoreId) {
        if (sharedUtils.isNullOrEmptyOrBlank(entitlementNumber))
            return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_REQUEST_INVALID));
        return repository.findByEntitlementNumber(entitlementNumber).flatMap(e -> {
            if (ignoreId != null && ignoreId.equals(e.getId())) return Mono.empty();
            return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_ALREADY_EXISTS));
        }).then();
    }

    /** Validate entitlement type is present */
    public Mono<Void> validateTypeRequired(String entitlementType) {
        if (sharedUtils.isNullOrEmptyOrBlank(entitlementType))
            return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_TYPE_REQUIRED));
        return Mono.empty();
    }

    /** Assert entitlement is not expired */
    public Mono<EntitlementEntity> assertNotExpired(EntitlementEntity entitlement) {
        if (EntitlementStatus.EXPIRED.equals(entitlement.getStatus()) ||
                (entitlement.getValidTo() != null && entitlement.getValidTo().isBefore(OffsetDateTime.now())))
            return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_EXPIRED));
        return Mono.just(entitlement);
    }

    /** Assert entitlement is not cancelled */
    public Mono<EntitlementEntity> assertNotCancelled(EntitlementEntity entitlement) {
        if (EntitlementStatus.CANCELLED.equals(entitlement.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_CANCELLED));
        return Mono.just(entitlement);
    }

    /** Assert entitlement is not revoked */
    public Mono<EntitlementEntity> assertNotRevoked(EntitlementEntity entitlement) {
        if (EntitlementStatus.REVOKED.equals(entitlement.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_REVOKED));
        return Mono.just(entitlement);
    }

    /** Assert entitlement has not exceeded usage limit */
    public Mono<EntitlementEntity> assertUsageLimitNotExceeded(EntitlementEntity entitlement) {
        if (entitlement.getUsageLimit() != null && entitlement.getUsageCount() != null
                && entitlement.getUsageCount() >= entitlement.getUsageLimit())
            return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_USAGE_LIMIT_EXCEEDED));
        return Mono.just(entitlement);
    }

    /** Validate validity window: validFrom must be before validTo */
    public Mono<Void> validateValidityWindow(OffsetDateTime validFrom, OffsetDateTime validTo) {
        if (validFrom != null && validTo != null && !validFrom.isBefore(validTo))
            return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_WINDOW_INVALID));
        return Mono.empty();
    }

    /** Validate order is linked */
    public Mono<Void> validateOrderRequired(UUID orderId) {
        if (orderId == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_NOT_FOUND));
        return Mono.empty();
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
