package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CancellationStatus;
import io.booking.booking_service.dto.pojo.reschedule.RescheduleRequest;
import io.booking.booking_service.dto.pojo.reschedule.RescheduleResponse;
import io.booking.booking_service.dto.pojo.reschedule.RescheduleUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.RescheduleEntity;
import io.booking.booking_service.repository.RescheduleRepository;
import io.booking.booking_service.service.RescheduleService;
import io.booking.booking_service.util.validators.Reschedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
public class RescheduleServiceImpl implements RescheduleService {

    private final RescheduleRepository repository;
    private final ReactiveTx reactiveTx;
    private final Reschedule validator;
    private final DatabaseClient db;

    public RescheduleServiceImpl(RescheduleRepository repository, ReactiveTx reactiveTx,
                                 Reschedule validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<RescheduleResponse> create(String requestId, RescheduleRequest request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.RESCHEDULE_REQUEST_INVALID));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        return validator.validateBookingRequired(request.getBookingId())
                .then(validator.validateOldScheduleRequired(request.getOldScheduleInstanceId()))
                .then(validator.validateNewScheduleRequired(request.getNewScheduleInstanceId()))
                .then(validator.assertSchedulesDiffer(request.getOldScheduleInstanceId(), request.getNewScheduleInstanceId()))
                .then(validator.validateNewStartInFuture(request.getNewStartAt()))
                .then(Mono.defer(() -> reactiveTx.required(() -> insert(request, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createReschedule] requestId={} bookingId={}", requestId, request.getBookingId()))
                .doOnSuccess(r -> log.info("[createReschedule] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createReschedule] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<RescheduleEntity> insert(RescheduleRequest r, OffsetDateTime now) {
        var spec = db.sql("""
                INSERT INTO reschedules
                    (tenant_id, booking_id, booking_item_id,
                     old_schedule_instance_id, new_schedule_instance_id,
                     old_start_at, new_start_at,
                     reschedule_fee_amount, currency_code,
                     reason_code, reason_text,
                     actor_type, actor_id,
                     status, requested_at, completed_at, created_at)
                VALUES
                    (:tenantId, :bookingId, :bookingItemId,
                     :oldScheduleInstanceId, :newScheduleInstanceId,
                     :oldStartAt, :newStartAt,
                     :rescheduleFeeAmount, :currencyCode,
                     :reasonCode, :reasonText,
                     CAST(:actorType AS actor_type), :actorId,
                     CAST(:status AS cancellation_status),
                     :requestedAt, :completedAt, :createdAt)
                RETURNING *
                """)
                .bind("tenantId", r.getTenantId())
                .bind("bookingId", r.getBookingId())
                .bind("oldScheduleInstanceId", r.getOldScheduleInstanceId())
                .bind("newScheduleInstanceId", r.getNewScheduleInstanceId())
                .bind("currencyCode", r.getCurrencyCode() != null ? r.getCurrencyCode() : "")
                .bind("reasonCode", r.getReasonCode() != null ? r.getReasonCode() : "")
                .bind("reasonText", r.getReasonText() != null ? r.getReasonText() : "")
                .bind("actorId", r.getActorId() != null ? r.getActorId() : "")
                .bind("createdAt", now);

        if (r.getBookingItemId() != null) spec = spec.bind("bookingItemId", r.getBookingItemId());
        else spec = spec.bindNull("bookingItemId", UUID.class);

        if (r.getOldStartAt() != null) spec = spec.bind("oldStartAt", r.getOldStartAt());
        else spec = spec.bindNull("oldStartAt", OffsetDateTime.class);

        if (r.getNewStartAt() != null) spec = spec.bind("newStartAt", r.getNewStartAt());
        else spec = spec.bindNull("newStartAt", OffsetDateTime.class);

        if (r.getRescheduleFeeAmount() != null) spec = spec.bind("rescheduleFeeAmount", r.getRescheduleFeeAmount());
        else spec = spec.bindNull("rescheduleFeeAmount", BigDecimal.class);

        if (r.getActorType() != null) spec = spec.bind("actorType", r.getActorType().name());
        else spec = spec.bindNull("actorType", String.class);

        CancellationStatus status = r.getStatus() != null ? r.getStatus() : CancellationStatus.REQUESTED;
        spec = spec.bind("status", status.name());

        if (r.getRequestedAt() != null) spec = spec.bind("requestedAt", r.getRequestedAt());
        else spec = spec.bind("requestedAt", now);

        if (r.getCompletedAt() != null) spec = spec.bind("completedAt", r.getCompletedAt());
        else spec = spec.bindNull("completedAt", OffsetDateTime.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<RescheduleResponse> update(String requestId, UUID id, RescheduleUpdate request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.RESCHEDULE_REQUEST_INVALID));
        return validator.load(id)
                .flatMap(validator::assertNotAlreadyCompleted)
                .flatMap(e -> reactiveTx.required(() -> updateReschedule(e, request)))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateReschedule] requestId={} id={}", requestId, id))
                .doOnSuccess(r -> log.info("[updateReschedule] Success requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateReschedule] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<RescheduleEntity> updateReschedule(RescheduleEntity e, RescheduleUpdate r) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        CancellationStatus status = r.getStatus() != null ? r.getStatus() : e.getStatus();

        var spec = db.sql("""
                UPDATE reschedules
                SET new_schedule_instance_id = :newScheduleInstanceId,
                    new_start_at             = :newStartAt,
                    reschedule_fee_amount    = :rescheduleFeeAmount,
                    reason_code              = :reasonCode,
                    reason_text              = :reasonText,
                    actor_type               = CAST(:actorType AS actor_type),
                    actor_id                 = :actorId,
                    status                   = CAST(:status AS cancellation_status),
                    completed_at             = :completedAt
                WHERE id = :id
                RETURNING *
                """)
                .bind("newScheduleInstanceId", r.getNewScheduleInstanceId() != null ? r.getNewScheduleInstanceId() : e.getNewScheduleInstanceId())
                .bind("reasonCode", r.getReasonCode() != null ? r.getReasonCode() : (e.getReasonCode() != null ? e.getReasonCode() : ""))
                .bind("reasonText", r.getReasonText() != null ? r.getReasonText() : (e.getReasonText() != null ? e.getReasonText() : ""))
                .bind("actorId", r.getActorId() != null ? r.getActorId() : (e.getActorId() != null ? e.getActorId() : ""))
                .bind("status", status.name())
                .bind("id", e.getId());

        if (r.getNewStartAt() != null) spec = spec.bind("newStartAt", r.getNewStartAt());
        else if (e.getNewStartAt() != null) spec = spec.bind("newStartAt", e.getNewStartAt());
        else spec = spec.bindNull("newStartAt", OffsetDateTime.class);

        if (r.getRescheduleFeeAmount() != null) spec = spec.bind("rescheduleFeeAmount", r.getRescheduleFeeAmount());
        else if (e.getRescheduleFeeAmount() != null) spec = spec.bind("rescheduleFeeAmount", e.getRescheduleFeeAmount());
        else spec = spec.bindNull("rescheduleFeeAmount", BigDecimal.class);

        ActorType actorType = r.getActorType() != null ? r.getActorType() : e.getActorType();
        if (actorType != null) spec = spec.bind("actorType", actorType.name());
        else spec = spec.bindNull("actorType", String.class);

        if (r.getCompletedAt() != null) spec = spec.bind("completedAt", r.getCompletedAt());
        else if (CancellationStatus.COMPLETED.equals(status)) spec = spec.bind("completedAt", now);
        else spec = spec.bindNull("completedAt", OffsetDateTime.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<RescheduleResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getReschedule] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getReschedule] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Flux<RescheduleResponse> getByBooking(String requestId, UUID bookingId) {
        if (bookingId == null)
            return Flux.error(BookingException.of(BookingErrorType.BOOKING_NOT_FOUND));
        return repository.findByBookingId(bookingId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getRescheduleByBooking] requestId={} bookingId={}", requestId, bookingId))
                .doOnError(e -> log.error("[getRescheduleByBooking] Failed requestId={} bookingId={} error={}", requestId, bookingId, e.getMessage(), e));
    }

    @Override
    public Flux<RescheduleResponse> getByBookingItem(String requestId, UUID bookingItemId) {
        if (bookingItemId == null)
            return Flux.error(BookingException.of(BookingErrorType.BOOKING_NOT_FOUND));
        return repository.findByBookingItemId(bookingItemId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getRescheduleByBookingItem] requestId={} bookingItemId={}", requestId, bookingItemId))
                .doOnError(e -> log.error("[getRescheduleByBookingItem] Failed requestId={} bookingItemId={} error={}", requestId, bookingItemId, e.getMessage(), e));
    }

    private RescheduleEntity mapRow(io.r2dbc.spi.Row row) {
        String actorTypeRaw = row.get("actor_type", String.class);
        String statusRaw = row.get("status", String.class);
        return RescheduleEntity.builder()
                .id(row.get("id", UUID.class))
                .tenantId(row.get("tenant_id", UUID.class))
                .bookingId(row.get("booking_id", UUID.class))
                .bookingItemId(row.get("booking_item_id", UUID.class))
                .oldScheduleInstanceId(row.get("old_schedule_instance_id", UUID.class))
                .newScheduleInstanceId(row.get("new_schedule_instance_id", UUID.class))
                .oldStartAt(row.get("old_start_at", OffsetDateTime.class))
                .newStartAt(row.get("new_start_at", OffsetDateTime.class))
                .rescheduleFeeAmount(row.get("reschedule_fee_amount", BigDecimal.class))
                .currencyCode(row.get("currency_code", String.class))
                .reasonCode(row.get("reason_code", String.class))
                .reasonText(row.get("reason_text", String.class))
                .actorType(actorTypeRaw != null ? ActorType.valueOf(actorTypeRaw) : null)
                .actorId(row.get("actor_id", String.class))
                .status(statusRaw != null ? CancellationStatus.valueOf(statusRaw) : null)
                .requestedAt(row.get("requested_at", OffsetDateTime.class))
                .completedAt(row.get("completed_at", OffsetDateTime.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .build();
    }

    private RescheduleResponse toResponse(RescheduleEntity e) {
        RescheduleResponse r = new RescheduleResponse();
        r.setId(e.getId());
        r.setTenantId(e.getTenantId());
        r.setBookingId(e.getBookingId());
        r.setBookingItemId(e.getBookingItemId());
        r.setOldScheduleInstanceId(e.getOldScheduleInstanceId());
        r.setNewScheduleInstanceId(e.getNewScheduleInstanceId());
        r.setOldStartAt(e.getOldStartAt());
        r.setNewStartAt(e.getNewStartAt());
        r.setRescheduleFeeAmount(e.getRescheduleFeeAmount());
        r.setCurrencyCode(e.getCurrencyCode());
        r.setReasonCode(e.getReasonCode());
        r.setReasonText(e.getReasonText());
        r.setActorType(e.getActorType());
        r.setActorId(e.getActorId());
        r.setStatus(e.getStatus());
        r.setRequestedAt(e.getRequestedAt());
        r.setCompletedAt(e.getCompletedAt());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}
