package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.OrderStatus;
import io.booking.booking_service.dto.pojo.orderstatushistory.OrderStatusHistoryRequest;
import io.booking.booking_service.dto.pojo.orderstatushistory.OrderStatusHistoryResponse;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.OrderStatusHistoryEntity;
import io.booking.booking_service.repository.OrderStatusHistoryRepository;
import io.booking.booking_service.service.OrderStatusHistoryService;
import io.booking.booking_service.util.validators.OrderStatusHistory;
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
public class OrderStatusHistoryServiceImpl implements OrderStatusHistoryService {

    private final OrderStatusHistoryRepository repository;
    private final ReactiveTx reactiveTx;
    private final OrderStatusHistory validator;
    private final DatabaseClient db;

    public OrderStatusHistoryServiceImpl(
            OrderStatusHistoryRepository repository,
            ReactiveTx reactiveTx,
            OrderStatusHistory validator,
            DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<OrderStatusHistoryResponse> create(String requestId, OrderStatusHistoryRequest request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_REQUEST_INVALID));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        return validator.validateOrderRequired(request.getOrderId())
                .then(validator.validateNewStatusRequired(request.getNewStatus()))
                .then(validator.assertStatusTransition(request.getOldStatus(), request.getNewStatus()))
                .then(Mono.defer(() -> reactiveTx.required(() -> insert(request, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createOrderStatusHistory] requestId={} orderId={}", requestId, request.getOrderId()))
                .doOnSuccess(r -> log.info("[createOrderStatusHistory] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createOrderStatusHistory] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<OrderStatusHistoryEntity> insert(OrderStatusHistoryRequest r, OffsetDateTime now) {
        var spec = db.sql("""
                INSERT INTO order_status_history
                    (order_id, old_status, new_status, actor_type, actor_id, reason_code, notes, created_at)
                VALUES
                    (:orderId,
                     CAST(:oldStatus AS order_status), CAST(:newStatus AS order_status),
                     CAST(:actorType AS actor_type),
                     :actorId, :reasonCode, :notes, :createdAt)
                RETURNING *
                """)
                .bind("orderId", r.getOrderId())
                .bind("actorId", r.getActorId() != null ? r.getActorId() : "")
                .bind("reasonCode", r.getReasonCode() != null ? r.getReasonCode() : "")
                .bind("notes", r.getNotes() != null ? r.getNotes() : "")
                .bind("createdAt", now);

        if (r.getOldStatus() != null) spec = spec.bind("oldStatus", r.getOldStatus().name());
        else spec = spec.bindNull("oldStatus", String.class);

        spec = spec.bind("newStatus", r.getNewStatus().name());

        if (r.getActorType() != null) spec = spec.bind("actorType", r.getActorType().name());
        else spec = spec.bindNull("actorType", String.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<OrderStatusHistoryResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getOrderStatusHistory] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getOrderStatusHistory] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Flux<OrderStatusHistoryResponse> getByOrder(String requestId, UUID orderId) {
        if (orderId == null)
            return Flux.error(BookingException.of(BookingErrorType.ORDER_NOT_FOUND));
        return repository.findByOrderId(orderId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getOrderStatusHistoryByOrder] requestId={} orderId={}", requestId, orderId))
                .doOnError(e -> log.error("[getOrderStatusHistoryByOrder] Failed requestId={} orderId={} error={}", requestId, orderId, e.getMessage(), e));
    }

    private OrderStatusHistoryEntity mapRow(io.r2dbc.spi.Row row) {
        String oldStatusRaw = row.get("old_status", String.class);
        String actorTypeRaw = row.get("actor_type", String.class);
        return OrderStatusHistoryEntity.builder()
                .id(row.get("id", UUID.class))
                .orderId(row.get("order_id", UUID.class))
                .oldStatus(oldStatusRaw != null ? OrderStatus.valueOf(oldStatusRaw) : null)
                .newStatus(OrderStatus.valueOf(row.get("new_status", String.class)))
                .actorType(actorTypeRaw != null ? ActorType.valueOf(actorTypeRaw) : null)
                .actorId(row.get("actor_id", String.class))
                .reasonCode(row.get("reason_code", String.class))
                .notes(row.get("notes", String.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .build();
    }

    private OrderStatusHistoryResponse toResponse(OrderStatusHistoryEntity e) {
        OrderStatusHistoryResponse r = new OrderStatusHistoryResponse();
        r.setId(e.getId());
        r.setOrderId(e.getOrderId());
        r.setOldStatus(e.getOldStatus());
        r.setNewStatus(e.getNewStatus());
        r.setActorType(e.getActorType());
        r.setActorId(e.getActorId());
        r.setReasonCode(e.getReasonCode());
        r.setNotes(e.getNotes());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}
