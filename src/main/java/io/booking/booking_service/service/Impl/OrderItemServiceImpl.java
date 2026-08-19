package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.FulfillmentType;
import io.booking.booking_service.datatype.booking.OrderStatus;
import io.booking.booking_service.dto.pojo.orderitem.OrderItemRequest;
import io.booking.booking_service.dto.pojo.orderitem.OrderItemResponse;
import io.booking.booking_service.dto.pojo.orderitem.OrderItemUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.OrderItemEntity;
import io.booking.booking_service.repository.OrderItemRepository;
import io.booking.booking_service.service.OrderItemService;
import io.booking.booking_service.util.validators.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository repository;
    private final ReactiveTx reactiveTx;
    private final OrderItem validator;
    private final DatabaseClient db;

    public OrderItemServiceImpl(OrderItemRepository repository, ReactiveTx reactiveTx,
                                OrderItem validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<OrderItemResponse> create(String requestId, OrderItemRequest request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_NOT_FOUND));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OrderStatus status = request.getStatus() != null ? request.getStatus() : OrderStatus.CONFIRMED;

        return validator.validateQuantity(request.getQuantity())
                .then(Mono.defer(() -> reactiveTx.required(() -> insert(request, status, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createOrderItem] requestId={} orderId={}", requestId, request.getOrderId()))
                .doOnSuccess(r -> log.info("[createOrderItem] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createOrderItem] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<OrderItemEntity> insert(OrderItemRequest r, OrderStatus status, OffsetDateTime now) {
        var spec = db.sql("""
                INSERT INTO order_items
                    (order_id, line_number, product_id, product_variant_id, offer_id, price_rule_id,
                     product_code_snapshot, product_name_snapshot,
                     variant_code_snapshot, variant_name_snapshot,
                     offer_code_snapshot, offer_name_snapshot,
                     quantity, unit_price, subtotal_amount, discount_amount, tax_amount, total_amount,
                     currency_code, fulfillment_type, service_date, schedule_instance_id,
                     status, notes, created_at, updated_at)
                VALUES
                    (:orderId, :lineNumber, :productId, :productVariantId, :offerId, :priceRuleId,
                     :productCodeSnapshot, :productNameSnapshot,
                     :variantCodeSnapshot, :variantNameSnapshot,
                     :offerCodeSnapshot, :offerNameSnapshot,
                     :quantity, :unitPrice, :subtotalAmount, :discountAmount, :taxAmount, :totalAmount,
                     :currencyCode, CAST(:fulfillmentType AS fulfillment_type), :serviceDate, :scheduleInstanceId,
                     CAST(:status AS order_status), :notes, :createdAt, :updatedAt)
                RETURNING *
                """)
                .bind("orderId", r.getOrderId())
                .bind("lineNumber", r.getLineNumber() != null ? r.getLineNumber() : 1)
                .bind("productCodeSnapshot", r.getProductCodeSnapshot() != null ? r.getProductCodeSnapshot() : "")
                .bind("productNameSnapshot", r.getProductNameSnapshot() != null ? r.getProductNameSnapshot() : "")
                .bind("variantCodeSnapshot", r.getVariantCodeSnapshot() != null ? r.getVariantCodeSnapshot() : "")
                .bind("variantNameSnapshot", r.getVariantNameSnapshot() != null ? r.getVariantNameSnapshot() : "")
                .bind("offerCodeSnapshot", r.getOfferCodeSnapshot() != null ? r.getOfferCodeSnapshot() : "")
                .bind("offerNameSnapshot", r.getOfferNameSnapshot() != null ? r.getOfferNameSnapshot() : "")
                .bind("quantity", r.getQuantity())
                .bind("currencyCode", r.getCurrencyCode() != null ? r.getCurrencyCode() : "")
                .bind("status", status.name())
                .bind("notes", r.getNotes() != null ? r.getNotes() : "")
                .bind("createdAt", now)
                .bind("updatedAt", now);

        if (r.getProductId() != null) spec = spec.bind("productId", r.getProductId());
        else spec = spec.bindNull("productId", UUID.class);
        if (r.getProductVariantId() != null) spec = spec.bind("productVariantId", r.getProductVariantId());
        else spec = spec.bindNull("productVariantId", UUID.class);
        if (r.getOfferId() != null) spec = spec.bind("offerId", r.getOfferId());
        else spec = spec.bindNull("offerId", UUID.class);
        if (r.getPriceRuleId() != null) spec = spec.bind("priceRuleId", r.getPriceRuleId());
        else spec = spec.bindNull("priceRuleId", UUID.class);
        if (r.getUnitPrice() != null) spec = spec.bind("unitPrice", r.getUnitPrice());
        else spec = spec.bindNull("unitPrice", BigDecimal.class);
        if (r.getSubtotalAmount() != null) spec = spec.bind("subtotalAmount", r.getSubtotalAmount());
        else spec = spec.bindNull("subtotalAmount", BigDecimal.class);
        if (r.getDiscountAmount() != null) spec = spec.bind("discountAmount", r.getDiscountAmount());
        else spec = spec.bindNull("discountAmount", BigDecimal.class);
        if (r.getTaxAmount() != null) spec = spec.bind("taxAmount", r.getTaxAmount());
        else spec = spec.bindNull("taxAmount", BigDecimal.class);
        if (r.getTotalAmount() != null) spec = spec.bind("totalAmount", r.getTotalAmount());
        else spec = spec.bindNull("totalAmount", BigDecimal.class);
        if (r.getFulfillmentType() != null) spec = spec.bind("fulfillmentType", r.getFulfillmentType().name());
        else spec = spec.bindNull("fulfillmentType", String.class);
        if (r.getServiceDate() != null) spec = spec.bind("serviceDate", r.getServiceDate());
        else spec = spec.bindNull("serviceDate", LocalDate.class);
        if (r.getScheduleInstanceId() != null) spec = spec.bind("scheduleInstanceId", r.getScheduleInstanceId());
        else spec = spec.bindNull("scheduleInstanceId", UUID.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<OrderItemResponse> update(String requestId, UUID id, OrderItemUpdate request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_NOT_FOUND));
        return validator.load(id)
                .flatMap(e -> {
                    Integer qty = request.getQuantity() != null ? request.getQuantity() : e.getQuantity();
                    return validator.validateQuantity(qty).thenReturn(e);
                })
                .flatMap(e -> Mono.defer(() -> reactiveTx.required(() -> updateItem(e, request))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateOrderItem] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateOrderItem] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<OrderItemEntity> updateItem(OrderItemEntity e, OrderItemUpdate r) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Integer quantity = r.getQuantity() != null ? r.getQuantity() : e.getQuantity();
        OrderStatus status = r.getStatus() != null ? r.getStatus() : e.getStatus();
        FulfillmentType fulfillmentType = r.getFulfillmentType() != null ? r.getFulfillmentType() : e.getFulfillmentType();
        LocalDate serviceDate = r.getServiceDate() != null ? r.getServiceDate() : e.getServiceDate();
        UUID scheduleInstanceId = r.getScheduleInstanceId() != null ? r.getScheduleInstanceId() : e.getScheduleInstanceId();
        String notes = r.getNotes() != null ? r.getNotes() : e.getNotes() != null ? e.getNotes() : "";
        BigDecimal unitPrice = r.getUnitPrice() != null ? r.getUnitPrice() : e.getUnitPrice();
        BigDecimal subtotal = r.getSubtotalAmount() != null ? r.getSubtotalAmount() : e.getSubtotalAmount();
        BigDecimal discount = r.getDiscountAmount() != null ? r.getDiscountAmount() : e.getDiscountAmount();
        BigDecimal tax = r.getTaxAmount() != null ? r.getTaxAmount() : e.getTaxAmount();
        BigDecimal total = r.getTotalAmount() != null ? r.getTotalAmount() : e.getTotalAmount();

        var spec = db.sql("""
                UPDATE order_items
                SET quantity          = :quantity,
                    unit_price        = :unitPrice,
                    subtotal_amount   = :subtotalAmount,
                    discount_amount   = :discountAmount,
                    tax_amount        = :taxAmount,
                    total_amount      = :totalAmount,
                    fulfillment_type  = CAST(:fulfillmentType AS fulfillment_type),
                    service_date      = :serviceDate,
                    schedule_instance_id = :scheduleInstanceId,
                    status            = CAST(:status AS order_status),
                    notes             = :notes,
                    updated_at        = :updatedAt
                WHERE id = :id
                RETURNING *
                """)
                .bind("quantity", quantity)
                .bind("status", status.name())
                .bind("notes", notes)
                .bind("updatedAt", now)
                .bind("id", e.getId());

        if (unitPrice != null) spec = spec.bind("unitPrice", unitPrice);
        else spec = spec.bindNull("unitPrice", BigDecimal.class);
        if (subtotal != null) spec = spec.bind("subtotalAmount", subtotal);
        else spec = spec.bindNull("subtotalAmount", BigDecimal.class);
        if (discount != null) spec = spec.bind("discountAmount", discount);
        else spec = spec.bindNull("discountAmount", BigDecimal.class);
        if (tax != null) spec = spec.bind("taxAmount", tax);
        else spec = spec.bindNull("taxAmount", BigDecimal.class);
        if (total != null) spec = spec.bind("totalAmount", total);
        else spec = spec.bindNull("totalAmount", BigDecimal.class);
        if (fulfillmentType != null) spec = spec.bind("fulfillmentType", fulfillmentType.name());
        else spec = spec.bindNull("fulfillmentType", String.class);
        if (serviceDate != null) spec = spec.bind("serviceDate", serviceDate);
        else spec = spec.bindNull("serviceDate", LocalDate.class);
        if (scheduleInstanceId != null) spec = spec.bind("scheduleInstanceId", scheduleInstanceId);
        else spec = spec.bindNull("scheduleInstanceId", UUID.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<OrderItemResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getOrderItem] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getOrderItem] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Flux<OrderItemResponse> getByOrder(String requestId, UUID orderId) {
        if (orderId == null)
            return Flux.error(BookingException.of(BookingErrorType.ORDER_NOT_FOUND));
        return repository.findByOrderId(orderId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getOrderItemsByOrder] requestId={} orderId={}", requestId, orderId))
                .doOnError(e -> log.error("[getOrderItemsByOrder] Failed requestId={} orderId={} error={}", requestId, orderId, e.getMessage(), e));
    }

    @Override
    public Flux<OrderItemResponse> getByProduct(String requestId, UUID productId) {
        if (productId == null)
            return Flux.error(BookingException.of(BookingErrorType.ORDER_ITEM_PRODUCT_REQUIRED));
        return repository.findByProductId(productId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getOrderItemsByProduct] requestId={} productId={}", requestId, productId))
                .doOnError(e -> log.error("[getOrderItemsByProduct] Failed requestId={} productId={} error={}", requestId, productId, e.getMessage(), e));
    }

    @Override
    public Mono<Void> cancel(String requestId, UUID id) {
        return validator.load(id)
                .flatMap(e -> {
                    if (OrderStatus.CANCELLED.equals(e.getStatus()))
                        return Mono.error(BookingException.of(BookingErrorType.ORDER_CANCELLED));
                    return Mono.just(e);
                })
                .flatMap(e -> reactiveTx.required(() -> db.sql("""
                        UPDATE order_items
                        SET status     = CAST(:status AS order_status),
                            updated_at = :updatedAt
                        WHERE id = :id
                        RETURNING id
                        """)
                        .bind("status", OrderStatus.CANCELLED.name())
                        .bind("updatedAt", OffsetDateTime.now(ZoneOffset.UTC))
                        .bind("id", e.getId())
                        .fetch().rowsUpdated()))
                .then()
                .doOnSubscribe(s -> log.info("[cancelOrderItem] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[cancelOrderItem] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private OrderItemEntity mapRow(io.r2dbc.spi.Row row) {
        String statusRaw = row.get("status", String.class);
        String fulfillmentRaw = row.get("fulfillment_type", String.class);
        return OrderItemEntity.builder()
                .id(row.get("id", UUID.class))
                .orderId(row.get("order_id", UUID.class))
                .lineNumber(row.get("line_number", Integer.class))
                .productId(row.get("product_id", UUID.class))
                .productVariantId(row.get("product_variant_id", UUID.class))
                .offerId(row.get("offer_id", UUID.class))
                .priceRuleId(row.get("price_rule_id", UUID.class))
                .productCodeSnapshot(row.get("product_code_snapshot", String.class))
                .productNameSnapshot(row.get("product_name_snapshot", String.class))
                .variantCodeSnapshot(row.get("variant_code_snapshot", String.class))
                .variantNameSnapshot(row.get("variant_name_snapshot", String.class))
                .offerCodeSnapshot(row.get("offer_code_snapshot", String.class))
                .offerNameSnapshot(row.get("offer_name_snapshot", String.class))
                .quantity(row.get("quantity", Integer.class))
                .unitPrice(row.get("unit_price", BigDecimal.class))
                .subtotalAmount(row.get("subtotal_amount", BigDecimal.class))
                .discountAmount(row.get("discount_amount", BigDecimal.class))
                .taxAmount(row.get("tax_amount", BigDecimal.class))
                .totalAmount(row.get("total_amount", BigDecimal.class))
                .currencyCode(row.get("currency_code", String.class))
                .fulfillmentType(fulfillmentRaw != null ? FulfillmentType.valueOf(fulfillmentRaw) : null)
                .serviceDate(row.get("service_date", LocalDate.class))
                .scheduleInstanceId(row.get("schedule_instance_id", UUID.class))
                .status(statusRaw != null ? OrderStatus.valueOf(statusRaw) : null)
                .notes(row.get("notes", String.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .updatedAt(row.get("updated_at", OffsetDateTime.class))
                .build();
    }

    private OrderItemResponse toResponse(OrderItemEntity e) {
        OrderItemResponse r = new OrderItemResponse();
        r.setId(e.getId());
        r.setOrderId(e.getOrderId());
        r.setLineNumber(e.getLineNumber());
        r.setProductId(e.getProductId());
        r.setProductVariantId(e.getProductVariantId());
        r.setOfferId(e.getOfferId());
        r.setPriceRuleId(e.getPriceRuleId());
        r.setProductCodeSnapshot(e.getProductCodeSnapshot());
        r.setProductNameSnapshot(e.getProductNameSnapshot());
        r.setVariantCodeSnapshot(e.getVariantCodeSnapshot());
        r.setVariantNameSnapshot(e.getVariantNameSnapshot());
        r.setOfferCodeSnapshot(e.getOfferCodeSnapshot());
        r.setOfferNameSnapshot(e.getOfferNameSnapshot());
        r.setQuantity(e.getQuantity());
        r.setUnitPrice(e.getUnitPrice());
        r.setSubtotalAmount(e.getSubtotalAmount());
        r.setDiscountAmount(e.getDiscountAmount());
        r.setTaxAmount(e.getTaxAmount());
        r.setTotalAmount(e.getTotalAmount());
        r.setCurrencyCode(e.getCurrencyCode());
        r.setFulfillmentType(e.getFulfillmentType());
        r.setServiceDate(e.getServiceDate());
        r.setScheduleInstanceId(e.getScheduleInstanceId());
        r.setStatus(e.getStatus());
        r.setNotes(e.getNotes());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}
