package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.FulfillmentType;
import io.booking.booking_service.dto.pojo.cartitem.CartItemRequest;
import io.booking.booking_service.dto.pojo.cartitem.CartItemResponse;
import io.booking.booking_service.dto.pojo.cartitem.CartItemUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.CartItemEntity;
import io.booking.booking_service.repository.CartItemRepository;
import io.booking.booking_service.service.CartItemService;
import io.booking.booking_service.util.validators.CartItem;
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
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository repository;
    private final ReactiveTx reactiveTx;
    private final CartItem validator;
    private final DatabaseClient db;

    public CartItemServiceImpl(CartItemRepository repository, ReactiveTx reactiveTx,
                               CartItem validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<CartItemResponse> create(String requestId, CartItemRequest request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.CART_ITEM_NOT_FOUND));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        return Mono.defer(() -> reactiveTx.required(() -> insert(request, now)))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createCartItem] requestId={}", requestId))
                .doOnSuccess(r -> log.info("[createCartItem] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createCartItem] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<CartItemEntity> insert(
            CartItemRequest r,
            OffsetDateTime now
    ) {

        var spec = db.sql("""
                        INSERT INTO cart_items
                            (
                                cart_id,
                                product_id,
                                quantity,
                                unit_price,
                                subtotal_amount,
                                discount_amount,
                                tax_amount,
                                total_amount,
                                currency_code,
                                fulfillment_type,
                                notes,
                                created_at,
                                updated_at
                            )
                        VALUES
                            (
                                :cartId,
                                :productId,
                                :quantity,
                                :unitPrice,
                                :subtotal,
                                :discount,
                                :tax,
                                :total,
                                :currencyCode,
                                CAST(:fulfillmentType AS fulfillment_type),
                                :notes,
                                :createdAt,
                                :updatedAt
                            )
                        RETURNING *
                        """)
                .bind("cartId", r.getCartId())
                .bind("productId", r.getProductId())
                .bind("quantity", 1)

                // TEMPORARY values until pricing is implemented
                .bind("unitPrice", BigDecimal.ZERO)
                .bind("subtotal", BigDecimal.ZERO)
                .bind("discount", BigDecimal.ZERO)
                .bind("tax", BigDecimal.ZERO)
                .bind("total", BigDecimal.ZERO)

                .bind("currencyCode", "KES")
                .bind("fulfillmentType", FulfillmentType.NO_FULFILLMENT.name())
                .bind("notes", r.getNotes() != null ? r.getNotes() : "")
                .bind("createdAt", now)
                .bind("updatedAt", now);

        return spec
                .map((row, meta) -> mapRow(row))
                .one();
    }

    @Override
    public Mono<CartItemResponse> update(String requestId, UUID id, CartItemUpdate request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.CART_ITEM_NOT_FOUND));
        return validator.load(id)
                .flatMap(e -> {
                    Integer qty = request.getQuantity() != null ? request.getQuantity() : e.getQuantity();
                    return validator.validateQuantity(qty).thenReturn(e);
                })
                .flatMap(e -> Mono.defer(() -> reactiveTx.required(() -> updateItem(e, request))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateCartItem] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateCartItem] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<CartItemEntity> updateItem(CartItemEntity e, CartItemUpdate r) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Integer quantity = r.getQuantity() != null ? r.getQuantity() : e.getQuantity();
        BigDecimal unitPrice = r.getUnitPrice() != null ? r.getUnitPrice() : e.getUnitPrice();
        BigDecimal subtotal = r.getSubtotalAmount() != null ? r.getSubtotalAmount() : e.getSubtotalAmount();
        BigDecimal discount = r.getDiscountAmount() != null ? r.getDiscountAmount() : e.getDiscountAmount();
        BigDecimal tax = r.getTaxAmount() != null ? r.getTaxAmount() : e.getTaxAmount();
        BigDecimal total = r.getTotalAmount() != null ? r.getTotalAmount() : e.getTotalAmount();
        FulfillmentType fulfillmentType = r.getFulfillmentType() != null ? r.getFulfillmentType() : e.getFulfillmentType();
        LocalDate serviceDate = r.getServiceDate() != null ? r.getServiceDate() : e.getServiceDate();
        String notes = r.getNotes() != null ? r.getNotes() : e.getNotes() != null ? e.getNotes() : "";
        Integer sortOrder = r.getSortOrder() != null ? r.getSortOrder() : e.getSortOrder() != null ? e.getSortOrder() : 0;

        var spec = db.sql("""
                        UPDATE cart_items
                        SET quantity          = :quantity,
                            unit_price        = :unitPrice,
                            subtotal_amount   = :subtotalAmount,
                            discount_amount   = :discountAmount,
                            tax_amount        = :taxAmount,
                            total_amount      = :totalAmount,
                            fulfillment_type  = CAST(:fulfillmentType AS fulfillment_type),
                            service_date      = :serviceDate,
                            notes             = :notes,
                            sort_order        = :sortOrder,
                            updated_at        = :updatedAt
                        WHERE id = :id
                        RETURNING *
                        """)
                .bind("quantity", quantity)
                .bind("notes", notes)
                .bind("sortOrder", sortOrder)
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

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<CartItemResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCartItem] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getCartItem] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Flux<CartItemResponse> getByCart(String requestId, UUID cartId) {
        if (cartId == null)
            return Flux.error(BookingException.of(BookingErrorType.CART_NOT_FOUND));
        return repository.findByCartId(cartId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCartItemsByCart] requestId={} cartId={}", requestId, cartId))
                .doOnError(e -> log.error("[getCartItemsByCart] Failed requestId={} cartId={} error={}", requestId, cartId, e.getMessage(), e));
    }

    @Override
    public Flux<CartItemResponse> getByProduct(String requestId, UUID productId) {
        if (productId == null)
            return Flux.error(BookingException.of(BookingErrorType.CART_ITEM_PRODUCT_REQUIRED));
        return repository.findByProductId(productId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCartItemsByProduct] requestId={} productId={}", requestId, productId))
                .doOnError(e -> log.error("[getCartItemsByProduct] Failed requestId={} productId={} error={}", requestId, productId, e.getMessage(), e));
    }

    @Override
    public Flux<CartItemResponse> getByScheduleInstance(String requestId, UUID scheduleInstanceId) {
        if (scheduleInstanceId == null)
            return Flux.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_NOT_FOUND));
        return repository.findByScheduleInstanceId(scheduleInstanceId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCartItemsByScheduleInstance] requestId={} scheduleInstanceId={}", requestId, scheduleInstanceId))
                .doOnError(e -> log.error("[getCartItemsByScheduleInstance] Failed requestId={} scheduleInstanceId={} error={}", requestId, scheduleInstanceId, e.getMessage(), e));
    }

    @Override
    public Mono<Void> delete(String requestId, UUID id) {
        return validator.load(id)
                .flatMap(e -> reactiveTx.required(() -> db.sql("""
                                DELETE FROM cart_items WHERE id = :id
                                """)
                        .bind("id", e.getId())
                        .fetch().rowsUpdated()))
                .then()
                .doOnSubscribe(s -> log.info("[deleteCartItem] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[deleteCartItem] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private CartItemEntity mapRow(io.r2dbc.spi.Row row) {
        String fulfillmentRaw = row.get("fulfillment_type", String.class);
        return CartItemEntity.builder()
                .id(row.get("id", UUID.class))
                .cartId(row.get("cart_id", String.class))
                .productId(row.get("product_id", UUID.class))
                .productVariantId(row.get("product_variant_id", UUID.class))
                .offerId(row.get("offer_id", UUID.class))
                .priceRuleId(row.get("price_rule_id", UUID.class))
                .scheduleInstanceId(row.get("schedule_instance_id", UUID.class))
                .quantity(row.get("quantity", Integer.class))
                .unitPrice(row.get("unit_price", BigDecimal.class))
                .subtotalAmount(row.get("subtotal_amount", BigDecimal.class))
                .discountAmount(row.get("discount_amount", BigDecimal.class))
                .taxAmount(row.get("tax_amount", BigDecimal.class))
                .totalAmount(row.get("total_amount", BigDecimal.class))
                .currencyCode(row.get("currency_code", String.class))
                .fulfillmentType(fulfillmentRaw != null ? FulfillmentType.valueOf(fulfillmentRaw) : null)
                .serviceDate(row.get("service_date", LocalDate.class))
                .notes(row.get("notes", String.class))
                .sortOrder(row.get("sort_order", Integer.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .updatedAt(row.get("updated_at", OffsetDateTime.class))
                .build();
    }

    private CartItemResponse toResponse(CartItemEntity e) {
        CartItemResponse r = new CartItemResponse();
        r.setId(e.getId());
        r.setCartId(e.getCartId());
        r.setProductId(e.getProductId());
        r.setProductVariantId(e.getProductVariantId());
        r.setOfferId(e.getOfferId());
        r.setPriceRuleId(e.getPriceRuleId());
        r.setScheduleInstanceId(e.getScheduleInstanceId());
        r.setQuantity(e.getQuantity());
        r.setUnitPrice(e.getUnitPrice());
        r.setSubtotalAmount(e.getSubtotalAmount());
        r.setDiscountAmount(e.getDiscountAmount());
        r.setTaxAmount(e.getTaxAmount());
        r.setTotalAmount(e.getTotalAmount());
        r.setCurrencyCode(e.getCurrencyCode());
        r.setFulfillmentType(e.getFulfillmentType());
        r.setServiceDate(e.getServiceDate());
        r.setNotes(e.getNotes());
        r.setSortOrder(e.getSortOrder());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}
