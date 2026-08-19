package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.HoldStatus;
import io.booking.booking_service.dto.pojo.reservationhold.ReservationHoldRequest;
import io.booking.booking_service.dto.pojo.reservationhold.ReservationHoldResponse;
import io.booking.booking_service.dto.pojo.reservationhold.ReservationHoldUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.ReservationHoldEntity;
import io.booking.booking_service.repository.ReservationHoldRepository;
import io.booking.booking_service.service.ReservationHoldService;
import io.booking.booking_service.util.validators.ReservationHold;
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
public class ReservationHoldServiceImpl implements ReservationHoldService {

    private final ReservationHoldRepository repository;
    private final ReactiveTx reactiveTx;
    private final ReservationHold validator;
    private final DatabaseClient db;

    public ReservationHoldServiceImpl(ReservationHoldRepository repository, ReactiveTx reactiveTx,
                                      ReservationHold validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<ReservationHoldResponse> create(String requestId, ReservationHoldRequest request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.RESERVATION_HOLD_REQUEST_INVALID));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        return validator.validateScheduleInstanceRequired(request.getScheduleInstanceId())
                .then(validator.validateCapacityPoolRequired(request.getCapacityPoolId()))
                .then(validator.validateQuantityHeld(request.getQuantityHeld()))
                .then(validator.ensureUniqueReference(request.getHoldReference(), null))
                .then(Mono.defer(() -> reactiveTx.required(() -> insert(request, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createReservationHold] requestId={} cartId={}", requestId, request.getCartId()))
                .doOnSuccess(r -> log.info("[createReservationHold] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createReservationHold] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<ReservationHoldEntity> insert(ReservationHoldRequest r, OffsetDateTime now) {
        HoldStatus status = r.getStatus() != null ? r.getStatus() : HoldStatus.ACTIVE;

        var spec = db.sql("""
                INSERT INTO reservation_holds
                    (tenant_id, cart_id, order_id, schedule_instance_id, capacity_pool_id,
                     hold_reference, quantity_held, status, expires_at,
                     consumed_at, released_at, created_at, updated_at)
                VALUES
                    (:tenantId, :cartId, :orderId, :scheduleInstanceId, :capacityPoolId,
                     :holdReference, :quantityHeld, CAST(:status AS hold_status), :expiresAt,
                     :consumedAt, :releasedAt, :createdAt, :updatedAt)
                RETURNING *
                """)
                .bind("tenantId", r.getTenantId())
                .bind("cartId", r.getCartId())
                .bind("scheduleInstanceId", r.getScheduleInstanceId())
                .bind("capacityPoolId", r.getCapacityPoolId())
                .bind("holdReference", r.getHoldReference() != null ? r.getHoldReference() : UUID.randomUUID().toString())
                .bind("quantityHeld", r.getQuantityHeld())
                .bind("status", status.name())
                .bind("expiresAt", r.getExpiresAt())
                .bind("createdAt", now)
                .bind("updatedAt", now);

        if (r.getOrderId() != null) spec = spec.bind("orderId", r.getOrderId());
        else spec = spec.bindNull("orderId", UUID.class);

        spec = spec.bindNull("consumedAt", OffsetDateTime.class);
        spec = spec.bindNull("releasedAt", OffsetDateTime.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<ReservationHoldResponse> update(String requestId, UUID id, ReservationHoldUpdate request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.RESERVATION_HOLD_REQUEST_INVALID));
        return validator.load(id)
                .flatMap(validator::assertNotExpired)
                .flatMap(e -> reactiveTx.required(() -> updateHold(e, request)))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateReservationHold] requestId={} id={}", requestId, id))
                .doOnSuccess(r -> log.info("[updateReservationHold] Success requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateReservationHold] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<ReservationHoldEntity> updateHold(ReservationHoldEntity e, ReservationHoldUpdate r) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        HoldStatus status = r.getStatus() != null ? r.getStatus() : e.getStatus();

        var spec = db.sql("""
                UPDATE reservation_holds
                SET status       = CAST(:status AS hold_status),
                    expires_at   = :expiresAt,
                    consumed_at  = :consumedAt,
                    released_at  = :releasedAt,
                    updated_at   = :updatedAt
                WHERE id = :id
                RETURNING *
                """)
                .bind("status", status.name())
                .bind("updatedAt", now)
                .bind("id", e.getId());

        if (r.getExpiresAt() != null) spec = spec.bind("expiresAt", r.getExpiresAt());
        else spec = spec.bind("expiresAt", e.getExpiresAt());

        // consumed_at: set automatically when status flips to CONSUMED
        if (r.getConsumedAt() != null) spec = spec.bind("consumedAt", r.getConsumedAt());
        else if (HoldStatus.CONSUMED.equals(status) && e.getConsumedAt() == null) spec = spec.bind("consumedAt", now);
        else if (e.getConsumedAt() != null) spec = spec.bind("consumedAt", e.getConsumedAt());
        else spec = spec.bindNull("consumedAt", OffsetDateTime.class);

        // released_at: set automatically when status flips to RELEASED
        if (r.getReleasedAt() != null) spec = spec.bind("releasedAt", r.getReleasedAt());
        else if (HoldStatus.RELEASED.equals(status) && e.getReleasedAt() == null) spec = spec.bind("releasedAt", now);
        else if (e.getReleasedAt() != null) spec = spec.bind("releasedAt", e.getReleasedAt());
        else spec = spec.bindNull("releasedAt", OffsetDateTime.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<ReservationHoldResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getReservationHold] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getReservationHold] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Flux<ReservationHoldResponse> getByCart(String requestId, UUID cartId) {
        if (cartId == null)
            return Flux.error(BookingException.of(BookingErrorType.CART_NOT_FOUND));
        return repository.findByCartId(cartId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getReservationHoldByCart] requestId={} cartId={}", requestId, cartId))
                .doOnError(e -> log.error("[getReservationHoldByCart] Failed requestId={} cartId={} error={}", requestId, cartId, e.getMessage(), e));
    }

    @Override
    public Flux<ReservationHoldResponse> getByScheduleInstance(String requestId, UUID scheduleInstanceId) {
        if (scheduleInstanceId == null)
            return Flux.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_NOT_FOUND));
        return repository.findByScheduleInstanceId(scheduleInstanceId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getReservationHoldByScheduleInstance] requestId={} scheduleInstanceId={}", requestId, scheduleInstanceId))
                .doOnError(e -> log.error("[getReservationHoldByScheduleInstance] Failed requestId={} scheduleInstanceId={} error={}", requestId, scheduleInstanceId, e.getMessage(), e));
    }

    private ReservationHoldEntity mapRow(io.r2dbc.spi.Row row) {
        String statusRaw = row.get("status", String.class);
        return ReservationHoldEntity.builder()
                .id(row.get("id", UUID.class))
                .tenantId(row.get("tenant_id", UUID.class))
                .cartId(row.get("cart_id", UUID.class))
                .orderId(row.get("order_id", UUID.class))
                .scheduleInstanceId(row.get("schedule_instance_id", UUID.class))
                .capacityPoolId(row.get("capacity_pool_id", UUID.class))
                .holdReference(row.get("hold_reference", String.class))
                .quantityHeld(row.get("quantity_held", Integer.class))
                .status(statusRaw != null ? HoldStatus.valueOf(statusRaw) : null)
                .expiresAt(row.get("expires_at", OffsetDateTime.class))
                .consumedAt(row.get("consumed_at", OffsetDateTime.class))
                .releasedAt(row.get("released_at", OffsetDateTime.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .updatedAt(row.get("updated_at", OffsetDateTime.class))
                .build();
    }

    private ReservationHoldResponse toResponse(ReservationHoldEntity e) {
        ReservationHoldResponse r = new ReservationHoldResponse();
        r.setId(e.getId());
        r.setTenantId(e.getTenantId());
        r.setCartId(e.getCartId());
        r.setOrderId(e.getOrderId());
        r.setScheduleInstanceId(e.getScheduleInstanceId());
        r.setCapacityPoolId(e.getCapacityPoolId());
        r.setHoldReference(e.getHoldReference());
        r.setQuantityHeld(e.getQuantityHeld());
        r.setStatus(e.getStatus());
        r.setExpiresAt(e.getExpiresAt());
        r.setConsumedAt(e.getConsumedAt());
        r.setReleasedAt(e.getReleasedAt());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}
