package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CancellationStatus;
import io.booking.booking_service.dto.pojo.cancellation.CancellationRequest;
import io.booking.booking_service.dto.pojo.cancellation.CancellationResponse;
import io.booking.booking_service.dto.pojo.cancellation.CancellationUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.CancellationEntity;
import io.booking.booking_service.repository.CancellationRepository;
import io.booking.booking_service.service.CancellationService;
import io.booking.booking_service.util.validators.Cancellation;
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
public class CancellationServiceImpl implements CancellationService {

    private final CancellationRepository repository;
    private final ReactiveTx reactiveTx;
    private final Cancellation validator;
    private final DatabaseClient db;

    public CancellationServiceImpl(CancellationRepository repository, ReactiveTx reactiveTx,
                                   Cancellation validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<CancellationResponse> create(String requestId, CancellationRequest request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.CANCELLATION_REQUEST_INVALID));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        CancellationStatus status = request.getStatus() != null ? request.getStatus() : CancellationStatus.REQUESTED;

        return validator.validateOrderRequired(request.getOrderId())
                .then(validator.validateReasonRequired(request.getReasonCode()))
                .then(Mono.defer(() -> reactiveTx.required(() -> insert(request, status, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createCancellation] requestId={} orderId={}", requestId, request.getOrderId()))
                .doOnSuccess(r -> log.info("[createCancellation] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createCancellation] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<CancellationEntity> insert(CancellationRequest r, CancellationStatus status, OffsetDateTime now) {
        var spec = db.sql("""
                INSERT INTO cancellations
                    (tenant_id, order_id, order_item_id, booking_id, booking_item_id, customer_id,
                     status, reason_code, reason_text, policy_reference,
                     refund_eligible, refund_amount_estimate,
                     actor_type, actor_id, requested_at, completed_at,
                     created_at, updated_at)
                VALUES
                    (:tenantId, :orderId, :orderItemId, :bookingId, :bookingItemId, :customerId,
                     CAST(:status AS cancellation_status), :reasonCode, :reasonText, :policyReference,
                     :refundEligible, :refundAmountEstimate,
                     CAST(:actorType AS actor_type), :actorId, :requestedAt, :completedAt,
                     :createdAt, :updatedAt)
                RETURNING *
                """)
                .bind("tenantId", r.getTenantId())
                .bind("orderId", r.getOrderId())
                .bind("customerId", r.getCustomerId())
                .bind("status", status.name())
                .bind("reasonCode", r.getReasonCode())
                .bind("reasonText", r.getReasonText() != null ? r.getReasonText() : "")
                .bind("policyReference", r.getPolicyReference() != null ? r.getPolicyReference() : "")
                .bind("refundEligible", r.getRefundEligible() != null ? r.getRefundEligible() : false)
                .bind("actorId", r.getActorId() != null ? r.getActorId() : "")
                .bind("createdAt", now)
                .bind("updatedAt", now);

        if (r.getOrderItemId() != null) spec = spec.bind("orderItemId", r.getOrderItemId());
        else spec = spec.bindNull("orderItemId", UUID.class);

        if (r.getBookingId() != null) spec = spec.bind("bookingId", r.getBookingId());
        else spec = spec.bindNull("bookingId", UUID.class);

        if (r.getBookingItemId() != null) spec = spec.bind("bookingItemId", r.getBookingItemId());
        else spec = spec.bindNull("bookingItemId", UUID.class);

        if (r.getRefundAmountEstimate() != null) spec = spec.bind("refundAmountEstimate", r.getRefundAmountEstimate());
        else spec = spec.bindNull("refundAmountEstimate", BigDecimal.class);

        if (r.getActorType() != null) spec = spec.bind("actorType", r.getActorType().name());
        else spec = spec.bindNull("actorType", String.class);

        if (r.getRequestedAt() != null) spec = spec.bind("requestedAt", r.getRequestedAt());
        else spec = spec.bind("requestedAt", now);

        if (r.getCompletedAt() != null) spec = spec.bind("completedAt", r.getCompletedAt());
        else spec = spec.bindNull("completedAt", OffsetDateTime.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<CancellationResponse> update(String requestId, UUID id, CancellationUpdate request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.CANCELLATION_REQUEST_INVALID));
        return validator.load(id)
                .flatMap(validator::assertNotAlreadyCompleted)
                .flatMap(validator::assertNotRejected)
                .flatMap(e -> Mono.defer(() -> reactiveTx.required(() -> updateCancellation(e, request))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateCancellation] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateCancellation] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<CancellationEntity> updateCancellation(CancellationEntity e, CancellationUpdate r) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        CancellationStatus status = r.getStatus() != null ? r.getStatus() : e.getStatus();
        String reasonCode = r.getReasonCode() != null ? r.getReasonCode() : e.getReasonCode();
        String reasonText = r.getReasonText() != null ? r.getReasonText() : e.getReasonText() != null ? e.getReasonText() : "";
        String policyRef = r.getPolicyReference() != null ? r.getPolicyReference() : e.getPolicyReference() != null ? e.getPolicyReference() : "";
        Boolean refundEligible = r.getRefundEligible() != null ? r.getRefundEligible() : e.getRefundEligible();
        String actorId = r.getActorId() != null ? r.getActorId() : e.getActorId() != null ? e.getActorId() : "";
        ActorType actorType = r.getActorType() != null ? r.getActorType() : e.getActorType();
        OffsetDateTime completedAt = r.getCompletedAt() != null ? r.getCompletedAt() : e.getCompletedAt();

        var spec = db.sql("""
                UPDATE cancellations
                SET status                 = CAST(:status AS cancellation_status),
                    reason_code            = :reasonCode,
                    reason_text            = :reasonText,
                    policy_reference       = :policyReference,
                    refund_eligible        = :refundEligible,
                    refund_amount_estimate = :refundAmountEstimate,
                    actor_type             = CAST(:actorType AS actor_type),
                    actor_id               = :actorId,
                    completed_at           = :completedAt,
                    updated_at             = :updatedAt
                WHERE id = :id
                RETURNING *
                """)
                .bind("status", status.name())
                .bind("reasonCode", reasonCode)
                .bind("reasonText", reasonText)
                .bind("policyReference", policyRef)
                .bind("refundEligible", refundEligible != null ? refundEligible : false)
                .bind("actorId", actorId)
                .bind("updatedAt", now)
                .bind("id", e.getId());

        BigDecimal estimate = r.getRefundAmountEstimate() != null ? r.getRefundAmountEstimate() : e.getRefundAmountEstimate();
        if (estimate != null) spec = spec.bind("refundAmountEstimate", estimate);
        else spec = spec.bindNull("refundAmountEstimate", BigDecimal.class);

        if (actorType != null) spec = spec.bind("actorType", actorType.name());
        else spec = spec.bindNull("actorType", String.class);

        if (completedAt != null) spec = spec.bind("completedAt", completedAt);
        else spec = spec.bindNull("completedAt", OffsetDateTime.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<CancellationResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCancellation] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getCancellation] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Flux<CancellationResponse> getByOrder(String requestId, UUID orderId) {
        if (orderId == null)
            return Flux.error(BookingException.of(BookingErrorType.ORDER_NOT_FOUND));
        return repository.findByOrderId(orderId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCancellationsByOrder] requestId={} orderId={}", requestId, orderId))
                .doOnError(e -> log.error("[getCancellationsByOrder] Failed requestId={} orderId={} error={}", requestId, orderId, e.getMessage(), e));
    }

    @Override
    public Flux<CancellationResponse> getByBooking(String requestId, UUID bookingId) {
        if (bookingId == null)
            return Flux.error(BookingException.of(BookingErrorType.BOOKING_NOT_FOUND));
        return repository.findByBookingId(bookingId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCancellationsByBooking] requestId={} bookingId={}", requestId, bookingId))
                .doOnError(e -> log.error("[getCancellationsByBooking] Failed requestId={} bookingId={} error={}", requestId, bookingId, e.getMessage(), e));
    }

    @Override
    public Flux<CancellationResponse> getByCustomer(String requestId, UUID customerId) {
        if (customerId == null)
            return Flux.error(BookingException.of(BookingErrorType.CUSTOMER_NOT_FOUND));
        return repository.findByCustomerId(customerId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCancellationsByCustomer] requestId={} customerId={}", requestId, customerId))
                .doOnError(e -> log.error("[getCancellationsByCustomer] Failed requestId={} customerId={} error={}", requestId, customerId, e.getMessage(), e));
    }

    private CancellationEntity mapRow(io.r2dbc.spi.Row row) {
        String statusRaw = row.get("status", String.class);
        String actorTypeRaw = row.get("actor_type", String.class);
        return CancellationEntity.builder()
                .id(row.get("id", UUID.class))
                .tenantId(row.get("tenant_id", UUID.class))
                .orderId(row.get("order_id", UUID.class))
                .orderItemId(row.get("order_item_id", UUID.class))
                .bookingId(row.get("booking_id", UUID.class))
                .bookingItemId(row.get("booking_item_id", UUID.class))
                .customerId(row.get("customer_id", UUID.class))
                .status(statusRaw != null ? CancellationStatus.valueOf(statusRaw) : null)
                .reasonCode(row.get("reason_code", String.class))
                .reasonText(row.get("reason_text", String.class))
                .policyReference(row.get("policy_reference", String.class))
                .refundEligible(row.get("refund_eligible", Boolean.class))
                .refundAmountEstimate(row.get("refund_amount_estimate", BigDecimal.class))
                .actorType(actorTypeRaw != null ? ActorType.valueOf(actorTypeRaw) : null)
                .actorId(row.get("actor_id", String.class))
                .requestedAt(row.get("requested_at", OffsetDateTime.class))
                .completedAt(row.get("completed_at", OffsetDateTime.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .updatedAt(row.get("updated_at", OffsetDateTime.class))
                .build();
    }

    private CancellationResponse toResponse(CancellationEntity e) {
        CancellationResponse r = new CancellationResponse();
        r.setId(e.getId());
        r.setTenantId(e.getTenantId());
        r.setOrderId(e.getOrderId());
        r.setOrderItemId(e.getOrderItemId());
        r.setBookingId(e.getBookingId());
        r.setBookingItemId(e.getBookingItemId());
        r.setCustomerId(e.getCustomerId());
        r.setStatus(e.getStatus());
        r.setReasonCode(e.getReasonCode());
        r.setReasonText(e.getReasonText());
        r.setPolicyReference(e.getPolicyReference());
        r.setRefundEligible(e.getRefundEligible());
        r.setRefundAmountEstimate(e.getRefundAmountEstimate());
        r.setActorType(e.getActorType());
        r.setActorId(e.getActorId());
        r.setRequestedAt(e.getRequestedAt());
        r.setCompletedAt(e.getCompletedAt());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}
