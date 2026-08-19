package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.PassStatus;
import io.booking.booking_service.dto.pojo.accesspass.AccessPassRequest;
import io.booking.booking_service.dto.pojo.accesspass.AccessPassResponse;
import io.booking.booking_service.dto.pojo.accesspass.AccessPassUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.AccessPassEntity;
import io.booking.booking_service.repository.AccessPassRepository;
import io.booking.booking_service.service.AccessPassService;
import io.booking.booking_service.util.validators.AccessPass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
public class AccessPassServiceImpl implements AccessPassService {

    private final AccessPassRepository repository;
    private final ReactiveTx reactiveTx;
    private final AccessPass validator;
    private final DatabaseClient db;

    public AccessPassServiceImpl(AccessPassRepository repository,
                                 ReactiveTx reactiveTx, AccessPass validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<AccessPassResponse> create(String requestId, AccessPassRequest request) {
        if (request == null) return Mono.error(BookingException.of(BookingErrorType.ACCESS_PASS_REQUEST_REQUIRED));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        PassStatus status = request.getStatus() != null ? request.getStatus() : PassStatus.ISSUED;

        return validator.validateEntitlementRequired(request.getEntitlementId())
                .then(validator.validatePassTypeRequired(request.getPassType()))
                .then(validator.ensureUniquePassNumber(request.getPassNumber(), request.getIgnoredId()))
                .then(Mono.defer(() -> reactiveTx.required(() -> insertAccessPass(request, status, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createAccessPass] requestId={} passNumber={}", requestId, request.getPassNumber()))
                .doOnSuccess(r -> log.info("[createAccessPass] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createAccessPass] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<AccessPassEntity> insertAccessPass(AccessPassRequest r, PassStatus status, OffsetDateTime now) {
        OffsetDateTime issuedAt = r.getIssuedAt() != null ? r.getIssuedAt() : now;
        return db.sql("""
                        INSERT INTO access_passes
                            (entitlement_id, pass_number, pass_type, barcode_value, qr_token, external_token,
                             status, issued_at, expires_at, created_at)
                        VALUES
                            (:entitlementId, :passNumber, :passType, :barcodeValue, :qrToken, :externalToken,
                             CAST(:status AS pass_status), :issuedAt, :expiresAt, :createdAt)
                        RETURNING *
                        """)
                .bind("entitlementId", r.getEntitlementId())
                .bind("passNumber", r.getPassNumber().trim())
                .bind("passType", r.getPassType().trim())
                .bind("barcodeValue", r.getBarcodeValue() != null ? r.getBarcodeValue() : "")
                .bind("qrToken", r.getQrToken() != null ? r.getQrToken() : "")
                .bind("externalToken", r.getExternalToken() != null ? r.getExternalToken() : "")
                .bind("status", status.name())
                .bind("issuedAt", issuedAt)
                .bind("expiresAt", r.getExpiresAt() != null ? r.getExpiresAt() : OffsetDateTime.now(ZoneOffset.UTC).plusYears(100))
                .bind("createdAt", now)
                .map((row, meta) -> mapRow(row))
                .one();
    }

    @Override
    public Mono<AccessPassResponse> update(String requestId, UUID id, AccessPassUpdate request) {
        if (request == null) return Mono.error(BookingException.of(BookingErrorType.ACCESS_PASS_REQUEST_INVALID));
        return validator.load(id)
                .flatMap(e -> Mono.defer(() -> reactiveTx.required(() -> updateAccessPass(e, request))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateAccessPass] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateAccessPass] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<AccessPassEntity> updateAccessPass(AccessPassEntity e, AccessPassUpdate r) {
        PassStatus status = r.getStatus() != null ? r.getStatus() : e.getStatus();
        OffsetDateTime expiresAt = r.getExpiresAt() != null ? r.getExpiresAt() : e.getExpiresAt();
        return db.sql("""
                        UPDATE access_passes
                        SET barcode_value = :barcodeValue,
                            qr_token      = :qrToken,
                            external_token = :externalToken,
                            status        = CAST(:status AS pass_status),
                            expires_at    = :expiresAt
                        WHERE id = :id
                        RETURNING *
                        """)
                .bind("barcodeValue", r.getBarcodeValue() != null ? r.getBarcodeValue() : e.getBarcodeValue() != null ? e.getBarcodeValue() : "")
                .bind("qrToken", r.getQrToken() != null ? r.getQrToken() : e.getQrToken() != null ? e.getQrToken() : "")
                .bind("externalToken", r.getExternalToken() != null ? r.getExternalToken() : e.getExternalToken() != null ? e.getExternalToken() : "")
                .bind("status", status.name())
                .bind("expiresAt", expiresAt != null ? expiresAt : OffsetDateTime.now(ZoneOffset.UTC).plusYears(100))
                .bind("id", e.getId())
                .map((row, meta) -> mapRow(row))
                .one();
    }

    @Override
    public Mono<AccessPassResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getAccessPass] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getAccessPass] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Flux<AccessPassResponse> getByEntitlement(String requestId, UUID entitlementId) {
        if (entitlementId == null) return Flux.error(BookingException.of(BookingErrorType.ACCESS_PASS_REQUEST_INVALID));
        return repository.findByEntitlementId(entitlementId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getAccessPassByEntitlement] requestId={} entitlementId={}", requestId, entitlementId))
                .doOnError(e -> log.error("[getAccessPassByEntitlement] Failed requestId={} entitlementId={} error={}", requestId, entitlementId, e.getMessage(), e));
    }

    @Override
    public Mono<AccessPassResponse> revoke(String requestId, UUID id) {
        return validator.load(id)
                .flatMap(e -> Mono.defer(() -> reactiveTx.required(() -> revokeAccessPass(e))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[revokeAccessPass] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[revokeAccessPass] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<AccessPassEntity> revokeAccessPass(AccessPassEntity e) {
        return db.sql("""
                        UPDATE access_passes
                        SET status = CAST(:status AS pass_status)
                        WHERE id = :id
                        RETURNING *
                        """)
                .bind("status", PassStatus.REVOKED.name())
                .bind("id", e.getId())
                .map((row, meta) -> mapRow(row))
                .one();
    }

    private AccessPassEntity mapRow(io.r2dbc.spi.Row row) {
        return AccessPassEntity.builder()
                .id(row.get("id", UUID.class))
                .entitlementId(row.get("entitlement_id", UUID.class))
                .passNumber(row.get("pass_number", String.class))
                .passType(row.get("pass_type", String.class))
                .barcodeValue(row.get("barcode_value", String.class))
                .qrToken(row.get("qr_token", String.class))
                .externalToken(row.get("external_token", String.class))
                .status(PassStatus.valueOf(row.get("status", String.class)))
                .issuedAt(row.get("issued_at", OffsetDateTime.class))
                .expiresAt(row.get("expires_at", OffsetDateTime.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .build();
    }

    private AccessPassResponse toResponse(AccessPassEntity e) {
        AccessPassResponse r = new AccessPassResponse();
        r.setId(e.getId());
        r.setEntitlementId(e.getEntitlementId());
        r.setPassNumber(e.getPassNumber());
        r.setPassType(e.getPassType());
        r.setBarcodeValue(e.getBarcodeValue());
        r.setQrToken(e.getQrToken());
        r.setExternalToken(e.getExternalToken());
        r.setStatus(e.getStatus());
        r.setIssuedAt(e.getIssuedAt());
        r.setExpiresAt(e.getExpiresAt());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}
