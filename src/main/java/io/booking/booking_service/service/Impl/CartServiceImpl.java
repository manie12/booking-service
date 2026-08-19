package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.CartStatus;
import io.booking.booking_service.dto.pojo.cart.CartRequest;
import io.booking.booking_service.dto.pojo.cart.CartResponse;
import io.booking.booking_service.dto.pojo.cart.CartUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.CartEntity;
import io.booking.booking_service.repository.CartRepository;
import io.booking.booking_service.service.CartService;
import io.booking.booking_service.util.validators.Cart;
import io.r2dbc.spi.Row;
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
public class CartServiceImpl implements CartService {

    private final CartRepository repository;
    private final ReactiveTx reactiveTx;
    private final Cart validator;
    private final DatabaseClient db;

    public CartServiceImpl(CartRepository repository, ReactiveTx reactiveTx,
                            Cart validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<CartResponse> create(String requestId, CartRequest request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.CART_NOT_FOUND));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        CartStatus status = request.getStatus() != null ? request.getStatus() : CartStatus.ACTIVE;

        return validator.ensureUniqueNumber(request.getCartNumber(), null)
                .then(validator.validateCurrency(request.getCurrencyCode()))
                .then(validator.validateCustomer(request.getCustomerId()))
                .then(Mono.defer(() -> reactiveTx.required(() -> insert(request, status, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createCart] requestId={} cartNumber={}", requestId, request.getCartNumber()))
                .doOnSuccess(r -> log.info("[createCart] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createCart] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<CartEntity> insert(CartRequest r, CartStatus status, OffsetDateTime now) {
        BigDecimal zero = BigDecimal.ZERO;
        var spec = db.sql("""
                INSERT INTO carts
                    (tenant_id, cart_number, customer_id, session_id, channel_id,
                     country_code, currency_code, status, expires_at,
                     subtotal_amount, discount_amount, tax_amount, total_amount,
                     notes, created_at, updated_at)
                VALUES
                    (:tenantId, :cartNumber, :customerId, :sessionId, :channelId,
                     :countryCode, :currencyCode, CAST(:status AS cart_status), :expiresAt,
                     :subtotalAmount, :discountAmount, :taxAmount, :totalAmount,
                     :notes, :createdAt, :updatedAt)
                RETURNING *
                """)
                .bind("tenantId", r.getTenantId())
                .bind("cartNumber", r.getCartNumber())
                .bind("customerId", r.getCustomerId())
                .bind("sessionId", r.getSessionId() != null ? r.getSessionId() : "")
                .bind("countryCode", r.getCountryCode() != null ? r.getCountryCode() : "")
                .bind("currencyCode", r.getCurrencyCode())
                .bind("status", status.name())
                .bind("subtotalAmount", r.getSubtotalAmount() != null ? r.getSubtotalAmount() : zero)
                .bind("discountAmount", r.getDiscountAmount() != null ? r.getDiscountAmount() : zero)
                .bind("taxAmount", r.getTaxAmount() != null ? r.getTaxAmount() : zero)
                .bind("totalAmount", r.getTotalAmount() != null ? r.getTotalAmount() : zero)
                .bind("notes", r.getNotes() != null ? r.getNotes() : "")
                .bind("createdAt", now)
                .bind("updatedAt", now);

        spec = r.getChannelId() != null ? spec.bind("channelId", r.getChannelId()) : spec.bindNull("channelId", UUID.class);
        spec = r.getExpiresAt() != null ? spec.bind("expiresAt", r.getExpiresAt()) : spec.bindNull("expiresAt", OffsetDateTime.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<CartResponse> update(String requestId, UUID id, CartUpdate request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.CART_NOT_FOUND));
        return validator.load(id)
                .flatMap(e -> Mono.defer(() -> reactiveTx.required(() -> updateCart(e, request))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateCart] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateCart] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<CartEntity> updateCart(CartEntity e, CartUpdate r) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        CartStatus status = r.getStatus() != null ? r.getStatus() : e.getStatus();
        BigDecimal subtotal = r.getSubtotalAmount() != null ? r.getSubtotalAmount() : e.getSubtotalAmount();
        BigDecimal discount = r.getDiscountAmount() != null ? r.getDiscountAmount() : e.getDiscountAmount();
        BigDecimal tax = r.getTaxAmount() != null ? r.getTaxAmount() : e.getTaxAmount();
        BigDecimal total = r.getTotalAmount() != null ? r.getTotalAmount() : e.getTotalAmount();
        String notes = r.getNotes() != null ? r.getNotes() : e.getNotes() != null ? e.getNotes() : "";
        OffsetDateTime expiresAt = r.getExpiresAt() != null ? r.getExpiresAt() : e.getExpiresAt();

        var spec = db.sql("""
                UPDATE carts
                SET status           = CAST(:status AS cart_status),
                    expires_at       = :expiresAt,
                    subtotal_amount  = :subtotalAmount,
                    discount_amount  = :discountAmount,
                    tax_amount       = :taxAmount,
                    total_amount     = :totalAmount,
                    notes            = :notes,
                    updated_at       = :updatedAt
                WHERE id = :id
                RETURNING *
                """)
                .bind("status", status.name())
                .bind("subtotalAmount", subtotal)
                .bind("discountAmount", discount)
                .bind("taxAmount", tax)
                .bind("totalAmount", total)
                .bind("notes", notes)
                .bind("updatedAt", now)
                .bind("id", e.getId());

        spec = expiresAt != null ? spec.bind("expiresAt", expiresAt) : spec.bindNull("expiresAt", OffsetDateTime.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<CartResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCart] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getCart] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Mono<CartResponse> getByCartNumber(String requestId, String cartNumber) {
        if (cartNumber == null || cartNumber.isBlank())
            return Mono.error(BookingException.of(BookingErrorType.CART_NOT_FOUND));
        return repository.findByCartNumber(cartNumber)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.CART_NOT_FOUND)))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCartByNumber] requestId={} cartNumber={}", requestId, cartNumber))
                .doOnError(e -> log.error("[getCartByNumber] Failed requestId={} cartNumber={} error={}", requestId, cartNumber, e.getMessage(), e));
    }

    @Override
    public Mono<CartResponse> getBySessionId(String requestId, String sessionId) {
        if (sessionId == null || sessionId.isBlank())
            return Mono.error(BookingException.of(BookingErrorType.CART_NOT_FOUND));
        return repository.findBySessionId(sessionId)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.CART_NOT_FOUND)))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCartBySession] requestId={} sessionId={}", requestId, sessionId))
                .doOnError(e -> log.error("[getCartBySession] Failed requestId={} sessionId={} error={}", requestId, sessionId, e.getMessage(), e));
    }

    @Override
    public Flux<CartResponse> getByCustomer(String requestId, UUID customerId) {
        return repository.findByCustomerId(customerId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCartsByCustomer] requestId={} customerId={}", requestId, customerId))
                .doOnError(e -> log.error("[getCartsByCustomer] Failed requestId={} customerId={} error={}", requestId, customerId, e.getMessage(), e));
    }

    @Override
    public Mono<Void> expire(String requestId, UUID id) {
        return validator.load(id)
                .flatMap(e -> reactiveTx.required(() -> db.sql("""
                        UPDATE carts
                        SET status     = CAST(:status AS cart_status),
                            updated_at = :updatedAt
                        WHERE id = :id
                        RETURNING id
                        """)
                        .bind("status", CartStatus.EXPIRED.name())
                        .bind("updatedAt", OffsetDateTime.now(ZoneOffset.UTC))
                        .bind("id", e.getId())
                        .fetch().rowsUpdated()))
                .then()
                .doOnSubscribe(s -> log.info("[expireCart] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[expireCart] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Mono<Void> abandon(String requestId, UUID id) {
        return validator.load(id)
                .flatMap(e -> reactiveTx.required(() -> db.sql("""
                        UPDATE carts
                        SET status     = CAST(:status AS cart_status),
                            updated_at = :updatedAt
                        WHERE id = :id
                        RETURNING id
                        """)
                        .bind("status", CartStatus.ABANDONED.name())
                        .bind("updatedAt", OffsetDateTime.now(ZoneOffset.UTC))
                        .bind("id", e.getId())
                        .fetch().rowsUpdated()))
                .then()
                .doOnSubscribe(s -> log.info("[abandonCart] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[abandonCart] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private CartEntity mapRow(Row row) {
        return CartEntity.builder()
                .id(row.get("id", UUID.class))
                .tenantId(row.get("tenant_id", UUID.class))
                .cartNumber(row.get("cart_number", String.class))
                .customerId(row.get("customer_id", String.class))
                .sessionId(row.get("session_id", String.class))
                .channelId(row.get("channel_id", String.class))
                .countryCode(row.get("country_code", String.class))
                .currencyCode(row.get("currency_code", String.class))
                .status(CartStatus.valueOf(row.get("status", String.class)))
                .expiresAt(row.get("expires_at", OffsetDateTime.class))
                .subtotalAmount(row.get("subtotal_amount", BigDecimal.class))
                .discountAmount(row.get("discount_amount", BigDecimal.class))
                .taxAmount(row.get("tax_amount", BigDecimal.class))
                .totalAmount(row.get("total_amount", BigDecimal.class))
                .notes(row.get("notes", String.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .updatedAt(row.get("updated_at", OffsetDateTime.class))
                .build();
    }

    private CartResponse toResponse(CartEntity e) {
        CartResponse r = new CartResponse();
        r.setId(e.getId());
        r.setTenantId(e.getTenantId());
        r.setCartNumber(e.getCartNumber());
        r.setCustomerId(e.getCustomerId());
        r.setSessionId(e.getSessionId());
        r.setChannelId(e.getChannelId());
        r.setCountryCode(e.getCountryCode());
        r.setCurrencyCode(e.getCurrencyCode());
        r.setStatus(e.getStatus());
        r.setExpiresAt(e.getExpiresAt());
        r.setSubtotalAmount(e.getSubtotalAmount());
        r.setDiscountAmount(e.getDiscountAmount());
        r.setTaxAmount(e.getTaxAmount());
        r.setTotalAmount(e.getTotalAmount());
        r.setNotes(e.getNotes());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}
