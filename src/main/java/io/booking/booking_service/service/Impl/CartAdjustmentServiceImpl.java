package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.AdjustmentType;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.dto.pojo.cartadjustment.CartAdjustmentRequest;
import io.booking.booking_service.dto.pojo.cartadjustment.CartAdjustmentResponse;
import io.booking.booking_service.dto.pojo.cartadjustment.CartAdjustmentUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.CartAdjustmentEntity;
import io.booking.booking_service.repository.CartAdjustmentRepository;
import io.booking.booking_service.service.CartAdjustmentService;
import io.booking.booking_service.util.validators.CartAdjustment;
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
public class CartAdjustmentServiceImpl implements CartAdjustmentService {

    private final CartAdjustmentRepository repository;
    private final ReactiveTx reactiveTx;
    private final CartAdjustment validator;
    private final DatabaseClient db;

    public CartAdjustmentServiceImpl(
            CartAdjustmentRepository repository,
            ReactiveTx reactiveTx,
            CartAdjustment validator,
            DatabaseClient db
    ) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    // ============================================================
    // CREATE
    // ============================================================

    @Override
    public Mono<CartAdjustmentResponse> create(
            String requestId,
            CartAdjustmentRequest request
    ) {

        if (request == null) {
            return Mono.error(
                    BookingException.of(
                            BookingErrorType.CART_ADJUSTMENT_REQUEST_INVALID
                    )
            );
        }

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        return validator
                .validateCartRequired(
                        request.getCartId()
                )

                // Cart UUID must actually exist in carts.id
                .then(
                        validator.validateCartExists(
                                request.getCartId()
                        )
                )

                // cartItemId is optional, but if supplied it must exist
                .then(
                        validator.validateCartItemExists(
                                request.getCartItemId()
                        )
                )

                .then(
                        validator.validateTypeRequired(
                                request.getAdjustmentType()
                        )
                )

                .then(
                        validator.validateAmount(
                                request.getAmount(),
                                request.getAdjustmentType()
                        )
                )

                .then(
                        Mono.defer(() ->
                                reactiveTx.required(() ->
                                        insert(request, now)
                                )
                        )
                )

                .map(this::toResponse)

                .doOnSubscribe(subscription ->
                        log.info(
                                "[createCartAdjustment] requestId={} cartId={} cartItemId={}",
                                requestId,
                                request.getCartId(),
                                request.getCartItemId()
                        )
                )

                .doOnSuccess(response ->
                        log.info(
                                "[createCartAdjustment] Success requestId={} id={} cartId={}",
                                requestId,
                                response != null
                                        ? response.getId()
                                        : null,
                                request.getCartId()
                        )
                )

                .doOnError(error ->
                        log.error(
                                "[createCartAdjustment] Failed requestId={} cartId={} cartItemId={} error={}",
                                requestId,
                                request.getCartId(),
                                request.getCartItemId(),
                                error.getMessage(),
                                error
                        )
                );
    }

    // ============================================================
    // INSERT
    // ============================================================

    private Mono<CartAdjustmentEntity> insert(
            CartAdjustmentRequest request,
            OffsetDateTime now
    ) {

        var spec = db.sql("""
                INSERT INTO cart_adjustments
                (
                    cart_id,
                    cart_item_id,
                    adjustment_type,
                    adjustment_name,
                    amount,
                    currency_code,
                    source,
                    reason_code,
                    created_at
                )
                VALUES
                (
                    :cartId,
                    :cartItemId,
                    CAST(:adjustmentType AS adjustment_type),
                    :adjustmentName,
                    :amount,
                    :currencyCode,
                    :source,
                    :reasonCode,
                    :createdAt
                )
                RETURNING *
                """)
                .bind(
                        "cartId",
                        request.getCartId()
                )
                .bind(
                        "adjustmentType",
                        request.getAdjustmentType().name()
                )
                .bind(
                        "adjustmentName",
                        request.getAdjustmentName() != null
                                ? request.getAdjustmentName()
                                : ""
                )
                .bind(
                        "amount",
                        request.getAmount()
                )
                .bind(
                        "currencyCode",
                        request.getCurrencyCode() != null
                                ? request.getCurrencyCode()
                                : ""
                )
                .bind(
                        "source",
                        request.getSource() != null
                                ? request.getSource()
                                : ""
                )
                .bind(
                        "reasonCode",
                        request.getReasonCode() != null
                                ? request.getReasonCode()
                                : ""
                )
                .bind(
                        "createdAt",
                        now
                );

        if (request.getCartItemId() != null) {
            spec = spec.bind(
                    "cartItemId",
                    request.getCartItemId()
            );
        } else {
            spec = spec.bindNull(
                    "cartItemId",
                    UUID.class
            );
        }

        return spec
                .map((row, metadata) ->
                        mapRow(row)
                )
                .one();
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @Override
    public Mono<CartAdjustmentResponse> update(
            String requestId,
            UUID id,
            CartAdjustmentUpdate request
    ) {

        if (request == null) {
            return Mono.error(
                    BookingException.of(
                            BookingErrorType.CART_ADJUSTMENT_REQUEST_INVALID
                    )
            );
        }

        return validator
                .load(id)

                .flatMap(existing -> {

                    AdjustmentType type =
                            request.getAdjustmentType() != null
                                    ? request.getAdjustmentType()
                                    : existing.getAdjustmentType();

                    BigDecimal amount =
                            request.getAmount() != null
                                    ? request.getAmount()
                                    : existing.getAmount();

                    return validator
                            .validateAmount(
                                    amount,
                                    type
                            )
                            .thenReturn(existing);
                })

                .flatMap(existing ->
                        Mono.defer(() ->
                                reactiveTx.required(() ->
                                        updateAdjustment(
                                                existing,
                                                request
                                        )
                                )
                        )
                )

                .map(this::toResponse)

                .doOnSubscribe(subscription ->
                        log.info(
                                "[updateCartAdjustment] requestId={} id={}",
                                requestId,
                                id
                        )
                )

                .doOnSuccess(response ->
                        log.info(
                                "[updateCartAdjustment] Success requestId={} id={}",
                                requestId,
                                id
                        )
                )

                .doOnError(error ->
                        log.error(
                                "[updateCartAdjustment] Failed requestId={} id={} error={}",
                                requestId,
                                id,
                                error.getMessage(),
                                error
                        )
                );
    }

    // ============================================================
    // UPDATE SQL
    // ============================================================

    private Mono<CartAdjustmentEntity> updateAdjustment(
            CartAdjustmentEntity existing,
            CartAdjustmentUpdate request
    ) {

        AdjustmentType type =
                request.getAdjustmentType() != null
                        ? request.getAdjustmentType()
                        : existing.getAdjustmentType();

        String name =
                request.getAdjustmentName() != null
                        ? request.getAdjustmentName()
                        : existing.getAdjustmentName() != null
                        ? existing.getAdjustmentName()
                        : "";

        BigDecimal amount =
                request.getAmount() != null
                        ? request.getAmount()
                        : existing.getAmount();

        String currency =
                request.getCurrencyCode() != null
                        ? request.getCurrencyCode()
                        : existing.getCurrencyCode() != null
                        ? existing.getCurrencyCode()
                        : "";

        String source =
                request.getSource() != null
                        ? request.getSource()
                        : existing.getSource() != null
                        ? existing.getSource()
                        : "";

        String reasonCode =
                request.getReasonCode() != null
                        ? request.getReasonCode()
                        : existing.getReasonCode() != null
                        ? existing.getReasonCode()
                        : "";

        return db.sql("""
                UPDATE cart_adjustments
                SET
                    adjustment_type = CAST(:adjustmentType AS adjustment_type),
                    adjustment_name = :adjustmentName,
                    amount          = :amount,
                    currency_code   = :currencyCode,
                    source          = :source,
                    reason_code     = :reasonCode
                WHERE id = :id
                RETURNING *
                """)
                .bind(
                        "adjustmentType",
                        type.name()
                )
                .bind(
                        "adjustmentName",
                        name
                )
                .bind(
                        "amount",
                        amount
                )
                .bind(
                        "currencyCode",
                        currency
                )
                .bind(
                        "source",
                        source
                )
                .bind(
                        "reasonCode",
                        reasonCode
                )
                .bind(
                        "id",
                        existing.getId()
                )
                .map((row, metadata) ->
                        mapRow(row)
                )
                .one();
    }

    // ============================================================
    // GET
    // ============================================================

    @Override
    public Mono<CartAdjustmentResponse> get(
            String requestId,
            UUID id
    ) {

        return validator
                .load(id)

                .map(this::toResponse)

                .doOnSubscribe(subscription ->
                        log.info(
                                "[getCartAdjustment] requestId={} id={}",
                                requestId,
                                id
                        )
                )

                .doOnSuccess(response ->
                        log.info(
                                "[getCartAdjustment] Success requestId={} id={}",
                                requestId,
                                id
                        )
                )

                .doOnError(error ->
                        log.error(
                                "[getCartAdjustment] Failed requestId={} id={} error={}",
                                requestId,
                                id,
                                error.getMessage(),
                                error
                        )
                );
    }

    // ============================================================
    // GET BY CART
    // ============================================================

    @Override
    public Flux<CartAdjustmentResponse> getByCart(
            String requestId,
            UUID cartId
    ) {

        if (cartId == null) {
            return Flux.error(
                    BookingException.of(
                            BookingErrorType.CART_NOT_FOUND
                    )
            );
        }

        return repository
                .findByCartId(cartId)

                .map(this::toResponse)

                .doOnSubscribe(subscription ->
                        log.info(
                                "[getCartAdjustmentsByCart] requestId={} cartId={}",
                                requestId,
                                cartId
                        )
                )

                .doOnComplete(() ->
                        log.info(
                                "[getCartAdjustmentsByCart] Success requestId={} cartId={}",
                                requestId,
                                cartId
                        )
                )

                .doOnError(error ->
                        log.error(
                                "[getCartAdjustmentsByCart] Failed requestId={} cartId={} error={}",
                                requestId,
                                cartId,
                                error.getMessage(),
                                error
                        )
                );
    }

    // ============================================================
    // GET BY CART ITEM
    // ============================================================

    @Override
    public Flux<CartAdjustmentResponse> getByCartItem(
            String requestId,
            UUID cartItemId
    ) {

        if (cartItemId == null) {
            return Flux.error(
                    BookingException.of(
                            BookingErrorType.CART_ITEM_NOT_FOUND
                    )
            );
        }

        return repository
                .findByCartItemId(cartItemId)

                .map(this::toResponse)

                .doOnSubscribe(subscription ->
                        log.info(
                                "[getCartAdjustmentsByCartItem] requestId={} cartItemId={}",
                                requestId,
                                cartItemId
                        )
                )

                .doOnComplete(() ->
                        log.info(
                                "[getCartAdjustmentsByCartItem] Success requestId={} cartItemId={}",
                                requestId,
                                cartItemId
                        )
                )

                .doOnError(error ->
                        log.error(
                                "[getCartAdjustmentsByCartItem] Failed requestId={} cartItemId={} error={}",
                                requestId,
                                cartItemId,
                                error.getMessage(),
                                error
                        )
                );
    }

    // ============================================================
    // DELETE
    // ============================================================

    @Override
    public Mono<Void> delete(
            String requestId,
            UUID id
    ) {

        return validator
                .load(id)

                .flatMap(existing ->
                        reactiveTx.required(() ->
                                db.sql("""
                                        DELETE FROM cart_adjustments
                                        WHERE id = :id
                                        """)
                                        .bind(
                                                "id",
                                                existing.getId()
                                        )
                                        .fetch()
                                        .rowsUpdated()
                        )
                )

                .then()

                .doOnSubscribe(subscription ->
                        log.info(
                                "[deleteCartAdjustment] requestId={} id={}",
                                requestId,
                                id
                        )
                )

                .doOnSuccess(ignored ->
                        log.info(
                                "[deleteCartAdjustment] Success requestId={} id={}",
                                requestId,
                                id
                        )
                )

                .doOnError(error ->
                        log.error(
                                "[deleteCartAdjustment] Failed requestId={} id={} error={}",
                                requestId,
                                id,
                                error.getMessage(),
                                error
                        )
                );
    }

    // ============================================================
    // ROW MAPPER
    // ============================================================

    private CartAdjustmentEntity mapRow(
            io.r2dbc.spi.Row row
    ) {

        String typeRaw =
                row.get(
                        "adjustment_type",
                        String.class
                );

        return CartAdjustmentEntity
                .builder()

                .id(
                        row.get(
                                "id",
                                UUID.class
                        )
                )

                .cartId(
                        row.get(
                                "cart_id",
                                UUID.class
                        )
                )

                .cartItemId(
                        row.get(
                                "cart_item_id",
                                UUID.class
                        )
                )

                .adjustmentType(
                        typeRaw != null
                                ? AdjustmentType.valueOf(typeRaw)
                                : null
                )

                .adjustmentName(
                        row.get(
                                "adjustment_name",
                                String.class
                        )
                )

                .amount(
                        row.get(
                                "amount",
                                BigDecimal.class
                        )
                )

                .currencyCode(
                        row.get(
                                "currency_code",
                                String.class
                        )
                )

                .source(
                        row.get(
                                "source",
                                String.class
                        )
                )

                .reasonCode(
                        row.get(
                                "reason_code",
                                String.class
                        )
                )

                .createdAt(
                        row.get(
                                "created_at",
                                OffsetDateTime.class
                        )
                )

                .build();
    }

    // ============================================================
    // RESPONSE MAPPER
    // ============================================================

    private CartAdjustmentResponse toResponse(
            CartAdjustmentEntity entity
    ) {

        CartAdjustmentResponse response =
                new CartAdjustmentResponse();

        response.setId(
                entity.getId()
        );

        response.setCartId(
                entity.getCartId()
        );

        response.setCartItemId(
                entity.getCartItemId()
        );

        response.setAdjustmentType(
                entity.getAdjustmentType()
        );

        response.setAdjustmentName(
                entity.getAdjustmentName()
        );

        response.setAmount(
                entity.getAmount()
        );

        response.setCurrencyCode(
                entity.getCurrencyCode()
        );

        response.setSource(
                entity.getSource()
        );

        response.setReasonCode(
                entity.getReasonCode()
        );

        response.setCreatedAt(
                entity.getCreatedAt()
        );

        return response;
    }
}