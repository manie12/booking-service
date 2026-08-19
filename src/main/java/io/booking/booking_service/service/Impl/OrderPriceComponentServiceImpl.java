package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.PriceComponentType;
import io.booking.booking_service.dto.pojo.orderpricecomponent.OrderPriceComponentRequest;
import io.booking.booking_service.dto.pojo.orderpricecomponent.OrderPriceComponentResponse;
import io.booking.booking_service.dto.pojo.orderpricecomponent.OrderPriceComponentUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.OrderPriceComponentEntity;
import io.booking.booking_service.repository.OrderPriceComponentRepository;
import io.booking.booking_service.service.OrderPriceComponentService;
import io.booking.booking_service.util.validators.OrderPriceComponent;
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
public class OrderPriceComponentServiceImpl implements OrderPriceComponentService {

    private final OrderPriceComponentRepository repository;
    private final ReactiveTx reactiveTx;
    private final OrderPriceComponent validator;
    private final DatabaseClient db;

    public OrderPriceComponentServiceImpl(OrderPriceComponentRepository repository, ReactiveTx reactiveTx,
                                          OrderPriceComponent validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<OrderPriceComponentResponse> create(String requestId, OrderPriceComponentRequest request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_PRICE_COMPONENT_REQUEST_INVALID));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        return validator.validateOrderItemRequired(request.getOrderItemId())
                .then(validator.validateComponentTypeRequired(request.getComponentType()))
                .then(validator.validateAmount(request.getAmount()))
                .then(Mono.defer(() -> reactiveTx.required(() -> insert(request, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createOrderPriceComponent] requestId={} orderItemId={}", requestId, request.getOrderItemId()))
                .doOnSuccess(r -> log.info("[createOrderPriceComponent] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createOrderPriceComponent] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<OrderPriceComponentEntity> insert(OrderPriceComponentRequest r, OffsetDateTime now) {
        return db.sql("""
                INSERT INTO order_price_components
                    (order_item_id, component_type, component_name,
                     source_reference, amount, currency_code, sort_order, created_at)
                VALUES
                    (:orderItemId, CAST(:componentType AS price_component_type), :componentName,
                     :sourceReference, :amount, :currencyCode, :sortOrder, :createdAt)
                RETURNING *
                """)
                .bind("orderItemId", r.getOrderItemId())
                .bind("componentType", r.getComponentType().name())
                .bind("componentName", r.getComponentName() != null ? r.getComponentName() : "")
                .bind("sourceReference", r.getSourceReference() != null ? r.getSourceReference() : "")
                .bind("amount", r.getAmount())
                .bind("currencyCode", r.getCurrencyCode() != null ? r.getCurrencyCode() : "")
                .bind("sortOrder", r.getSortOrder() != null ? r.getSortOrder() : 0)
                .bind("createdAt", now)
                .map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<OrderPriceComponentResponse> update(String requestId, UUID id, OrderPriceComponentUpdate request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_PRICE_COMPONENT_REQUEST_INVALID));
        return validator.load(id)
                .flatMap(e -> {
                    BigDecimal amount = request.getAmount() != null ? request.getAmount() : e.getAmount();
                    return validator.validateAmount(amount).thenReturn(e);
                })
                .flatMap(e -> Mono.defer(() -> reactiveTx.required(() -> updateComponent(e, request))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateOrderPriceComponent] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateOrderPriceComponent] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<OrderPriceComponentEntity> updateComponent(OrderPriceComponentEntity e, OrderPriceComponentUpdate r) {
        PriceComponentType componentType = r.getComponentType() != null ? r.getComponentType() : e.getComponentType();
        String componentName = r.getComponentName() != null ? r.getComponentName() : e.getComponentName() != null ? e.getComponentName() : "";
        String sourceRef = r.getSourceReference() != null ? r.getSourceReference() : e.getSourceReference() != null ? e.getSourceReference() : "";
        BigDecimal amount = r.getAmount() != null ? r.getAmount() : e.getAmount();
        String currencyCode = r.getCurrencyCode() != null ? r.getCurrencyCode() : e.getCurrencyCode() != null ? e.getCurrencyCode() : "";
        Integer sortOrder = r.getSortOrder() != null ? r.getSortOrder() : e.getSortOrder() != null ? e.getSortOrder() : 0;

        return db.sql("""
                UPDATE order_price_components
                SET component_type   = CAST(:componentType AS price_component_type),
                    component_name   = :componentName,
                    source_reference = :sourceReference,
                    amount           = :amount,
                    currency_code    = :currencyCode,
                    sort_order       = :sortOrder
                WHERE id = :id
                RETURNING *
                """)
                .bind("componentType", componentType.name())
                .bind("componentName", componentName)
                .bind("sourceReference", sourceRef)
                .bind("amount", amount)
                .bind("currencyCode", currencyCode)
                .bind("sortOrder", sortOrder)
                .bind("id", e.getId())
                .map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<OrderPriceComponentResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getOrderPriceComponent] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getOrderPriceComponent] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Flux<OrderPriceComponentResponse> getByOrderItem(String requestId, UUID orderItemId) {
        if (orderItemId == null)
            return Flux.error(BookingException.of(BookingErrorType.ORDER_ITEM_NOT_FOUND));
        return repository.findByOrderItemId(orderItemId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getOrderPriceComponentsByOrderItem] requestId={} orderItemId={}", requestId, orderItemId))
                .doOnError(e -> log.error("[getOrderPriceComponentsByOrderItem] Failed requestId={} orderItemId={} error={}", requestId, orderItemId, e.getMessage(), e));
    }

    @Override
    public Mono<Void> delete(String requestId, UUID id) {
        return validator.load(id)
                .flatMap(e -> reactiveTx.required(() -> db.sql("""
                        DELETE FROM order_price_components
                        WHERE id = :id
                        """)
                        .bind("id", e.getId())
                        .fetch().rowsUpdated()))
                .then()
                .doOnSubscribe(s -> log.info("[deleteOrderPriceComponent] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[deleteOrderPriceComponent] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private OrderPriceComponentEntity mapRow(io.r2dbc.spi.Row row) {
        String typeRaw = row.get("component_type", String.class);
        return OrderPriceComponentEntity.builder()
                .id(row.get("id", UUID.class))
                .orderItemId(row.get("order_item_id", UUID.class))
                .componentType(typeRaw != null ? PriceComponentType.valueOf(typeRaw) : null)
                .componentName(row.get("component_name", String.class))
                .sourceReference(row.get("source_reference", String.class))
                .amount(row.get("amount", BigDecimal.class))
                .currencyCode(row.get("currency_code", String.class))
                .sortOrder(row.get("sort_order", Integer.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .build();
    }

    private OrderPriceComponentResponse toResponse(OrderPriceComponentEntity e) {
        OrderPriceComponentResponse r = new OrderPriceComponentResponse();
        r.setId(e.getId());
        r.setOrderItemId(e.getOrderItemId());
        r.setComponentType(e.getComponentType());
        r.setComponentName(e.getComponentName());
        r.setSourceReference(e.getSourceReference());
        r.setAmount(e.getAmount());
        r.setCurrencyCode(e.getCurrencyCode());
        r.setSortOrder(e.getSortOrder());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}
