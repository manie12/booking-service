package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.PassStatus;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.AccessPassEntity;
import io.booking.booking_service.repository.AccessPassRepository;
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
public class AccessPass {

    private final SharedUtils sharedUtils;
    private final AccessPassRepository repository;

    /** Load access pass by id, fail with NOT_FOUND if missing */
    public Mono<AccessPassEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.ACCESS_PASS_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.ACCESS_PASS_NOT_FOUND)));
    }

    /** Load access pass by barcode value */
    public Mono<AccessPassEntity> loadByBarcode(String barcodeValue) {
        if (sharedUtils.isNullOrEmptyOrBlank(barcodeValue))
            return Mono.error(BookingException.of(BookingErrorType.ACCESS_PASS_INVALID));
        return repository.findByBarcodeValue(barcodeValue)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.ACCESS_PASS_NOT_FOUND)));
    }

    /** Load access pass by QR token */
    public Mono<AccessPassEntity> loadByQrToken(String qrToken) {
        if (sharedUtils.isNullOrEmptyOrBlank(qrToken))
            return Mono.error(BookingException.of(BookingErrorType.ACCESS_PASS_INVALID));
        return repository.findByQrToken(qrToken)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.ACCESS_PASS_NOT_FOUND)));
    }

    /** Ensure pass number is unique; pass ignoreId=null on create */
    public Mono<Void> ensureUniquePassNumber(String passNumber, UUID ignoreId) {
        if (sharedUtils.isNullOrEmptyOrBlank(passNumber))
            return Mono.error(BookingException.of(BookingErrorType.ACCESS_PASS_REQUEST_INVALID));
        return repository.findByPassNumber(passNumber).flatMap(e -> {
            if (ignoreId != null && ignoreId.equals(e.getId())) return Mono.empty();
            return Mono.error(BookingException.of(BookingErrorType.ACCESS_PASS_ALREADY_EXISTS));
        }).then();
    }

    /** Validate pass type is present */
    public Mono<Void> validatePassTypeRequired(String passType) {
        if (sharedUtils.isNullOrEmptyOrBlank(passType))
            return Mono.error(BookingException.of(BookingErrorType.ACCESS_PASS_TYPE_REQUIRED));
        return Mono.empty();
    }

    /** Validate entitlement is linked */
    public Mono<Void> validateEntitlementRequired(UUID entitlementId) {
        if (entitlementId == null)
            return Mono.error(BookingException.of(BookingErrorType.ACCESS_PASS_REQUEST_INVALID));
        return Mono.empty();
    }

    /** Assert pass is not expired */
    public Mono<AccessPassEntity> assertNotExpired(AccessPassEntity pass) {
        if (PassStatus.EXPIRED.equals(pass.getStatus()) ||
                (pass.getExpiresAt() != null && pass.getExpiresAt().isBefore(OffsetDateTime.now())))
            return Mono.error(BookingException.of(BookingErrorType.ACCESS_PASS_EXPIRED));
        return Mono.just(pass);
    }

    /** Assert pass is not cancelled */
    public Mono<AccessPassEntity> assertNotCancelled(AccessPassEntity pass) {
        if (PassStatus.CANCELLED.equals(pass.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.ACCESS_PASS_CANCELLED));
        return Mono.just(pass);
    }

    /** Assert pass is not revoked */
    public Mono<AccessPassEntity> assertNotRevoked(AccessPassEntity pass) {
        if (PassStatus.REVOKED.equals(pass.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.ACCESS_PASS_REVOKED));
        return Mono.just(pass);
    }

    /** Assert pass has not already been used */
    public Mono<AccessPassEntity> assertNotUsed(AccessPassEntity pass) {
        if (PassStatus.USED.equals(pass.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.ACCESS_PASS_INVALID));
        return Mono.just(pass);
    }

    /** Load and assert pass is usable (not expired, cancelled, revoked, or used) */
    public Mono<AccessPassEntity> loadUsable(UUID id) {
        return load(id)
                .flatMap(this::assertNotExpired)
                .flatMap(this::assertNotCancelled)
                .flatMap(this::assertNotRevoked)
                .flatMap(this::assertNotUsed);
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
