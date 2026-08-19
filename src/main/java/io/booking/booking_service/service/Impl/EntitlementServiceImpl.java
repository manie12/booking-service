package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.EntitlementStatus;
import io.booking.booking_service.dto.pojo.entitlement.EntitlementRequest;
import io.booking.booking_service.dto.pojo.entitlement.EntitlementResponse;
import io.booking.booking_service.dto.pojo.entitlement.EntitlementUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.EntitlementEntity;
import io.booking.booking_service.repository.EntitlementRepository;
import io.booking.booking_service.service.EntitlementService;
import io.booking.booking_service.util.validators.Entitlement;
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
public class EntitlementServiceImpl implements EntitlementService {

    private final EntitlementRepository repository;
    private final ReactiveTx reactiveTx;
    private final Entitlement validator;
    private final DatabaseClient db;

    public EntitlementServiceImpl(EntitlementRepository repository, ReactiveTx reactiveTx,
                                  Entitlement validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<EntitlementResponse> create(String requestId, EntitlementRequest request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_REQUEST_INVALID));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        EntitlementStatus status = request.getStatus() != null ? request.getStatus() : EntitlementStatus.ISSUED;
        OffsetDateTime issuedAt = request.getIssuedAt() != null ? request.getIssuedAt() : now;

        return validator.validateOrderRequired(request.getOrderId())
                .then(validator.validateTypeRequired(request.getEntitlementType()))
                .then(validator.ensureUniqueNumber(request.getEntitlementNumber(), null))
                .then(validator.validateValidityWindow(request.getValidFrom(), request.getValidTo()))
                .then(Mono.defer(() -> reactiveTx.required(() -> insert(request, status, issuedAt, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createEntitlement] requestId={} entitlementNumber={}", requestId, request.getEntitlementNumber()))
                .doOnSuccess(r -> log.info("[createEntitlement] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createEntitlement] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<EntitlementEntity> insert(EntitlementRequest r, EntitlementStatus status,
                                           OffsetDateTime issuedAt, OffsetDateTime now) {
        var spec = db.sql("""
                INSERT INTO entitlements
                    (tenant_id, entitlement_number, order_id, order_item_id,
                     booking_id, booking_item_id, guest_profile_id,
                     entitlement_type, status, issued_at,
                     valid_from, valid_to, usage_limit, usage_count,
                     notes, created_at, updated_at)
                VALUES
                    (:tenantId, :entitlementNumber, :orderId, :orderItemId,
                     :bookingId, :bookingItemId, :guestProfileId,
                     :entitlementType, CAST(:status AS entitlement_status), :issuedAt,
                     :validFrom, :validTo, :usageLimit, :usageCount,
                     :notes, :createdAt, :updatedAt)
                RETURNING *
                """)
                .bind("tenantId", r.getTenantId())
                .bind("entitlementNumber", r.getEntitlementNumber())
                .bind("orderId", r.getOrderId())
                .bind("orderItemId", r.getOrderItemId())
                .bind("entitlementType", r.getEntitlementType())
                .bind("status", status.name())
                .bind("issuedAt", issuedAt)
                .bind("usageLimit", r.getUsageLimit() != null ? r.getUsageLimit() : 1)
                .bind("usageCount", r.getUsageCount() != null ? r.getUsageCount() : 0)
                .bind("notes", r.getNotes() != null ? r.getNotes() : "")
                .bind("createdAt", now)
                .bind("updatedAt", now);

        if (r.getBookingId() != null) spec = spec.bind("bookingId", r.getBookingId());
        else spec = spec.bindNull("bookingId", UUID.class);

        if (r.getBookingItemId() != null) spec = spec.bind("bookingItemId", r.getBookingItemId());
        else spec = spec.bindNull("bookingItemId", UUID.class);

        if (r.getGuestProfileId() != null) spec = spec.bind("guestProfileId", r.getGuestProfileId());
        else spec = spec.bindNull("guestProfileId", UUID.class);

        if (r.getValidFrom() != null) spec = spec.bind("validFrom", r.getValidFrom());
        else spec = spec.bindNull("validFrom", OffsetDateTime.class);

        if (r.getValidTo() != null) spec = spec.bind("validTo", r.getValidTo());
        else spec = spec.bindNull("validTo", OffsetDateTime.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<EntitlementResponse> update(String requestId, UUID id, EntitlementUpdate request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_REQUEST_INVALID));
        return validator.load(id)
                .flatMap(validator::assertNotRevoked)
                .flatMap(validator::assertNotCancelled)
                .flatMap(e -> {
                    OffsetDateTime from = request.getValidFrom() != null ? request.getValidFrom() : e.getValidFrom();
                    OffsetDateTime to = request.getValidTo() != null ? request.getValidTo() : e.getValidTo();
                    return validator.validateValidityWindow(from, to).thenReturn(e);
                })
                .flatMap(e -> Mono.defer(() -> reactiveTx.required(() -> updateEntitlement(e, request))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateEntitlement] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateEntitlement] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<EntitlementEntity> updateEntitlement(EntitlementEntity e, EntitlementUpdate r) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        EntitlementStatus status = r.getStatus() != null ? r.getStatus() : e.getStatus();
        Integer usageLimit = r.getUsageLimit() != null ? r.getUsageLimit() : e.getUsageLimit();
        Integer usageCount = r.getUsageCount() != null ? r.getUsageCount() : e.getUsageCount();
        String notes = r.getNotes() != null ? r.getNotes() : e.getNotes() != null ? e.getNotes() : "";
        OffsetDateTime validFrom = r.getValidFrom() != null ? r.getValidFrom() : e.getValidFrom();
        OffsetDateTime validTo = r.getValidTo() != null ? r.getValidTo() : e.getValidTo();

        var spec = db.sql("""
                UPDATE entitlements
                SET status      = CAST(:status AS entitlement_status),
                    valid_from  = :validFrom,
                    valid_to    = :validTo,
                    usage_limit = :usageLimit,
                    usage_count = :usageCount,
                    notes       = :notes,
                    updated_at  = :updatedAt
                WHERE id = :id
                RETURNING *
                """)
                .bind("status", status.name())
                .bind("usageLimit", usageLimit != null ? usageLimit : 1)
                .bind("usageCount", usageCount != null ? usageCount : 0)
                .bind("notes", notes)
                .bind("updatedAt", now)
                .bind("id", e.getId());

        if (validFrom != null) spec = spec.bind("validFrom", validFrom);
        else spec = spec.bindNull("validFrom", OffsetDateTime.class);

        if (validTo != null) spec = spec.bind("validTo", validTo);
        else spec = spec.bindNull("validTo", OffsetDateTime.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<EntitlementResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getEntitlement] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getEntitlement] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Mono<EntitlementResponse> getByEntitlementNumber(String requestId, String entitlementNumber) {
        if (entitlementNumber == null || entitlementNumber.isBlank())
            return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_REQUEST_INVALID));
        return repository.findByEntitlementNumber(entitlementNumber)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_NOT_FOUND)))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getEntitlementByNumber] requestId={} entitlementNumber={}", requestId, entitlementNumber))
                .doOnError(e -> log.error("[getEntitlementByNumber] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    @Override
    public Flux<EntitlementResponse> getByOrder(String requestId, UUID orderId) {
        if (orderId == null)
            return Flux.error(BookingException.of(BookingErrorType.ORDER_NOT_FOUND));
        return repository.findByOrderId(orderId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getEntitlementsByOrder] requestId={} orderId={}", requestId, orderId))
                .doOnError(e -> log.error("[getEntitlementsByOrder] Failed requestId={} orderId={} error={}", requestId, orderId, e.getMessage(), e));
    }

    @Override
    public Flux<EntitlementResponse> getByBooking(String requestId, UUID bookingId) {
        if (bookingId == null)
            return Flux.error(BookingException.of(BookingErrorType.BOOKING_NOT_FOUND));
        return repository.findByBookingId(bookingId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getEntitlementsByBooking] requestId={} bookingId={}", requestId, bookingId))
                .doOnError(e -> log.error("[getEntitlementsByBooking] Failed requestId={} bookingId={} error={}", requestId, bookingId, e.getMessage(), e));
    }

    @Override
    public Flux<EntitlementResponse> getByGuestProfile(String requestId, UUID guestProfileId) {
        if (guestProfileId == null)
            return Flux.error(BookingException.of(BookingErrorType.ENTITLEMENT_NOT_FOUND));
        return repository.findByGuestProfileId(guestProfileId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getEntitlementsByGuestProfile] requestId={} guestProfileId={}", requestId, guestProfileId))
                .doOnError(e -> log.error("[getEntitlementsByGuestProfile] Failed requestId={} guestProfileId={} error={}", requestId, guestProfileId, e.getMessage(), e));
    }

    @Override
    public Mono<Void> revoke(String requestId, UUID id) {
        return validator.load(id)
                .flatMap(validator::assertNotRevoked)
                .flatMap(validator::assertNotCancelled)
                .flatMap(e -> reactiveTx.required(() -> db.sql("""
                        UPDATE entitlements
                        SET status = CAST(:status AS entitlement_status), updated_at = :updatedAt
                        WHERE id = :id
                        RETURNING id
                        """)
                        .bind("status", EntitlementStatus.REVOKED.name())
                        .bind("updatedAt", OffsetDateTime.now(ZoneOffset.UTC))
                        .bind("id", e.getId())
                        .fetch().rowsUpdated()))
                .then()
                .doOnSubscribe(s -> log.info("[revokeEntitlement] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[revokeEntitlement] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Mono<Void> cancel(String requestId, UUID id) {
        return validator.load(id)
                .flatMap(validator::assertNotRevoked)
                .flatMap(validator::assertNotCancelled)
                .flatMap(e -> reactiveTx.required(() -> db.sql("""
                        UPDATE entitlements
                        SET status = CAST(:status AS entitlement_status), updated_at = :updatedAt
                        WHERE id = :id
                        RETURNING id
                        """)
                        .bind("status", EntitlementStatus.CANCELLED.name())
                        .bind("updatedAt", OffsetDateTime.now(ZoneOffset.UTC))
                        .bind("id", e.getId())
                        .fetch().rowsUpdated()))
                .then()
                .doOnSubscribe(s -> log.info("[cancelEntitlement] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[cancelEntitlement] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private EntitlementEntity mapRow(io.r2dbc.spi.Row row) {
        String statusRaw = row.get("status", String.class);
        return EntitlementEntity.builder()
                .id(row.get("id", UUID.class))
                .tenantId(row.get("tenant_id", UUID.class))
                .entitlementNumber(row.get("entitlement_number", String.class))
                .orderId(row.get("order_id", UUID.class))
                .orderItemId(row.get("order_item_id", UUID.class))
                .bookingId(row.get("booking_id", UUID.class))
                .bookingItemId(row.get("booking_item_id", UUID.class))
                .guestProfileId(row.get("guest_profile_id", UUID.class))
                .entitlementType(row.get("entitlement_type", String.class))
                .status(statusRaw != null ? EntitlementStatus.valueOf(statusRaw) : null)
                .issuedAt(row.get("issued_at", OffsetDateTime.class))
                .validFrom(row.get("valid_from", OffsetDateTime.class))
                .validTo(row.get("valid_to", OffsetDateTime.class))
                .usageLimit(row.get("usage_limit", Integer.class))
                .usageCount(row.get("usage_count", Integer.class))
                .notes(row.get("notes", String.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .updatedAt(row.get("updated_at", OffsetDateTime.class))
                .build();
    }

    private EntitlementResponse toResponse(EntitlementEntity e) {
        EntitlementResponse r = new EntitlementResponse();
        r.setId(e.getId());
        r.setTenantId(e.getTenantId());
        r.setEntitlementNumber(e.getEntitlementNumber());
        r.setOrderId(e.getOrderId());
        r.setOrderItemId(e.getOrderItemId());
        r.setBookingId(e.getBookingId());
        r.setBookingItemId(e.getBookingItemId());
        r.setGuestProfileId(e.getGuestProfileId());
        r.setEntitlementType(e.getEntitlementType());
        r.setStatus(e.getStatus());
        r.setIssuedAt(e.getIssuedAt());
        r.setValidFrom(e.getValidFrom());
        r.setValidTo(e.getValidTo());
        r.setUsageLimit(e.getUsageLimit());
        r.setUsageCount(e.getUsageCount());
        r.setNotes(e.getNotes());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}
