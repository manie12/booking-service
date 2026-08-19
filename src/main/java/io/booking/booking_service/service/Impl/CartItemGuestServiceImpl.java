package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.GuestType;
import io.booking.booking_service.dto.pojo.cartitemguest.CartItemGuestRequest;
import io.booking.booking_service.dto.pojo.cartitemguest.CartItemGuestResponse;
import io.booking.booking_service.dto.pojo.cartitemguest.CartItemGuestUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.CartItemGuestEntity;
import io.booking.booking_service.repository.CartItemGuestRepository;
import io.booking.booking_service.service.CartItemGuestService;
import io.booking.booking_service.util.validators.CartItemGuest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
public class CartItemGuestServiceImpl implements CartItemGuestService {

    private final CartItemGuestRepository repository;
    private final ReactiveTx reactiveTx;
    private final CartItemGuest validator;
    private final DatabaseClient db;

    public CartItemGuestServiceImpl(
            CartItemGuestRepository repository,
            ReactiveTx reactiveTx,
            CartItemGuest validator,
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
    public Mono<CartItemGuestResponse> create(
            String requestId,
            CartItemGuestRequest request
    ) {

        if (request == null) {
            return Mono.error(
                    BookingException.of(
                            BookingErrorType.CART_ITEM_NOT_FOUND
                    )
            );
        }

        OffsetDateTime now =
                OffsetDateTime.now(ZoneOffset.UTC);

        return validator
                .validateCartItemRequired(
                        request.getCartItemId()
                )

                .then(
                        validator.validateNameRequired(
                                request.getGuestFirstName(),
                                request.getGuestLastName()
                        )
                )

                .then(
                        validator.validateGuestTypeRequired(
                                request.getGuestType()
                        )
                )

                .then(
                        validator.validateDateOfBirth(
                                request.getGuestDateOfBirth()
                        )
                )

                .then(
                        validator.validateGuestProfileExists(
                                request.getGuestProfileId()
                        )
                )

                .then(
                        validator.assertNoDuplicateGuest(
                                request.getCartItemId(),
                                request.getGuestProfileId()
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
                                "[createCartItemGuest] requestId={} cartItemId={} guestProfileId={}",
                                requestId,
                                request.getCartItemId(),
                                request.getGuestProfileId()
                        )
                )

                .doOnSuccess(response ->
                        log.info(
                                "[createCartItemGuest] Success requestId={} id={}",
                                requestId,
                                response != null
                                        ? response.getId()
                                        : null
                        )
                )

                .doOnError(error ->
                        log.error(
                                "[createCartItemGuest] Failed requestId={} cartItemId={} error={}",
                                requestId,
                                request.getCartItemId(),
                                error.getMessage(),
                                error
                        )
                );
    }

    private Mono<CartItemGuestEntity> insert(
            CartItemGuestRequest request,
            OffsetDateTime now
    ) {

        var spec = db.sql("""
                        INSERT INTO cart_item_guests
                        (
                            cart_item_id,
                            guest_profile_id,
                            guest_first_name,
                            guest_last_name,
                            guest_date_of_birth,
                            guest_type,
                            notes,
                            created_at
                        )
                        VALUES
                        (
                            :cartItemId,
                            :guestProfileId,
                            :guestFirstName,
                            :guestLastName,
                            :guestDateOfBirth,
                            CAST(:guestType AS guest_type),
                            :notes,
                            :createdAt
                        )
                        RETURNING *
                        """)
                .bind(
                        "cartItemId",
                        request.getCartItemId()
                )
                .bind(
                        "guestFirstName",
                        request.getGuestFirstName()
                )
                .bind(
                        "guestLastName",
                        request.getGuestLastName()
                )
                .bind(
                        "guestType",
                        request.getGuestType().name()
                )
                .bind(
                        "notes",
                        request.getNotes() != null
                                ? request.getNotes()
                                : ""
                )
                .bind(
                        "createdAt",
                        now
                );

        // Guest profile is optional.
        if (request.getGuestProfileId() != null) {
            spec = spec.bind(
                    "guestProfileId",
                    request.getGuestProfileId()
            );
        } else {
            spec = spec.bindNull(
                    "guestProfileId",
                    UUID.class
            );
        }

        // Date of birth is optional.
        if (request.getGuestDateOfBirth() != null) {
            spec = spec.bind(
                    "guestDateOfBirth",
                    request.getGuestDateOfBirth()
            );
        } else {
            spec = spec.bindNull(
                    "guestDateOfBirth",
                    LocalDate.class
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
    public Mono<CartItemGuestResponse> update(
            String requestId,
            UUID id,
            CartItemGuestUpdate request
    ) {

        if (request == null) {
            return Mono.error(
                    BookingException.of(
                            BookingErrorType.CART_ITEM_NOT_FOUND
                    )
            );
        }

        return validator
                .load(id)

                .flatMap(existing ->
                        validator
                                .validateDateOfBirth(
                                        request.getGuestDateOfBirth()
                                )

                                .then(
                                        validator.validateGuestProfileExists(
                                                request.getGuestProfileId()
                                        )
                                )

                                .thenReturn(existing)
                )

                .flatMap(existing ->
                        Mono.defer(() ->
                                reactiveTx.required(() ->
                                        updateGuest(
                                                existing,
                                                request
                                        )
                                )
                        )
                )

                .map(this::toResponse)

                .doOnSubscribe(subscription ->
                        log.info(
                                "[updateCartItemGuest] requestId={} id={}",
                                requestId,
                                id
                        )
                )

                .doOnSuccess(response ->
                        log.info(
                                "[updateCartItemGuest] Success requestId={} id={}",
                                requestId,
                                response != null
                                        ? response.getId()
                                        : null
                        )
                )

                .doOnError(error ->
                        log.error(
                                "[updateCartItemGuest] Failed requestId={} id={} error={}",
                                requestId,
                                id,
                                error.getMessage(),
                                error
                        )
                );
    }

    private Mono<CartItemGuestEntity> updateGuest(
            CartItemGuestEntity existing,
            CartItemGuestUpdate request
    ) {

        String firstName =
                request.getGuestFirstName() != null
                        ? request.getGuestFirstName()
                        : existing.getGuestFirstName();

        String lastName =
                request.getGuestLastName() != null
                        ? request.getGuestLastName()
                        : existing.getGuestLastName();

        GuestType guestType =
                request.getGuestType() != null
                        ? request.getGuestType()
                        : existing.getGuestType();

        LocalDate dateOfBirth =
                request.getGuestDateOfBirth() != null
                        ? request.getGuestDateOfBirth()
                        : existing.getGuestDateOfBirth();

        UUID guestProfileId =
                request.getGuestProfileId() != null
                        ? request.getGuestProfileId()
                        : existing.getGuestProfileId();

        String notes =
                request.getNotes() != null
                        ? request.getNotes()
                        : existing.getNotes() != null
                        ? existing.getNotes()
                        : "";

        var spec = db.sql("""
                        UPDATE cart_item_guests
                        SET
                            guest_profile_id     = :guestProfileId,
                            guest_first_name     = :guestFirstName,
                            guest_last_name      = :guestLastName,
                            guest_date_of_birth  = :guestDateOfBirth,
                            guest_type           = CAST(:guestType AS guest_type),
                            notes                = :notes
                        WHERE id = :id
                        RETURNING *
                        """)
                .bind(
                        "guestFirstName",
                        firstName
                )
                .bind(
                        "guestLastName",
                        lastName
                )
                .bind(
                        "guestType",
                        guestType.name()
                )
                .bind(
                        "notes",
                        notes
                )
                .bind(
                        "id",
                        existing.getId()
                );

        if (guestProfileId != null) {
            spec = spec.bind(
                    "guestProfileId",
                    guestProfileId
            );
        } else {
            spec = spec.bindNull(
                    "guestProfileId",
                    UUID.class
            );
        }

        if (dateOfBirth != null) {
            spec = spec.bind(
                    "guestDateOfBirth",
                    dateOfBirth
            );
        } else {
            spec = spec.bindNull(
                    "guestDateOfBirth",
                    LocalDate.class
            );
        }

        return spec
                .map((row, metadata) ->
                        mapRow(row)
                )
                .one();
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @Override
    public Mono<CartItemGuestResponse> get(
            String requestId,
            UUID id
    ) {

        return validator
                .load(id)

                .map(this::toResponse)

                .doOnSubscribe(subscription ->
                        log.info(
                                "[getCartItemGuest] requestId={} id={}",
                                requestId,
                                id
                        )
                )

                .doOnSuccess(response ->
                        log.info(
                                "[getCartItemGuest] Success requestId={} id={}",
                                requestId,
                                id
                        )
                )

                .doOnError(error ->
                        log.error(
                                "[getCartItemGuest] Failed requestId={} id={} error={}",
                                requestId,
                                id,
                                error.getMessage(),
                                error
                        )
                );
    }

    // ============================================================
    // GET BY CART ITEM
    // ============================================================

    @Override
    public Flux<CartItemGuestResponse> getByCartItem(
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
                                "[getCartItemGuestsByCartItem] requestId={} cartItemId={}",
                                requestId,
                                cartItemId
                        )
                )

                .doOnComplete(() ->
                        log.info(
                                "[getCartItemGuestsByCartItem] Success requestId={} cartItemId={}",
                                requestId,
                                cartItemId
                        )
                )

                .doOnError(error ->
                        log.error(
                                "[getCartItemGuestsByCartItem] Failed requestId={} cartItemId={} error={}",
                                requestId,
                                cartItemId,
                                error.getMessage(),
                                error
                        )
                );
    }

    // ============================================================
    // GET BY GUEST PROFILE
    // ============================================================

    @Override
    public Flux<CartItemGuestResponse> getByGuestProfile(
            String requestId,
            UUID guestProfileId
    ) {

        if (guestProfileId == null) {
            return Flux.error(
                    BookingException.of(
                            BookingErrorType.CART_ITEM_NOT_FOUND
                    )
            );
        }

        return repository
                .findByGuestProfileId(guestProfileId)

                .map(this::toResponse)

                .doOnSubscribe(subscription ->
                        log.info(
                                "[getCartItemGuestsByGuestProfile] requestId={} guestProfileId={}",
                                requestId,
                                guestProfileId
                        )
                )

                .doOnComplete(() ->
                        log.info(
                                "[getCartItemGuestsByGuestProfile] Success requestId={} guestProfileId={}",
                                requestId,
                                guestProfileId
                        )
                )

                .doOnError(error ->
                        log.error(
                                "[getCartItemGuestsByGuestProfile] Failed requestId={} guestProfileId={} error={}",
                                requestId,
                                guestProfileId,
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
                                                DELETE FROM cart_item_guests
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
                                "[deleteCartItemGuest] requestId={} id={}",
                                requestId,
                                id
                        )
                )

                .doOnSuccess(ignored ->
                        log.info(
                                "[deleteCartItemGuest] Success requestId={} id={}",
                                requestId,
                                id
                        )
                )

                .doOnError(error ->
                        log.error(
                                "[deleteCartItemGuest] Failed requestId={} id={} error={}",
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

    private CartItemGuestEntity mapRow(
            io.r2dbc.spi.Row row
    ) {

        String guestTypeRaw =
                row.get(
                        "guest_type",
                        String.class
                );

        return CartItemGuestEntity
                .builder()

                .id(
                        row.get(
                                "id",
                                UUID.class
                        )
                )

                .cartItemId(
                        row.get(
                                "cart_item_id",
                                UUID.class
                        )
                )

                .guestProfileId(
                        row.get(
                                "guest_profile_id",
                                UUID.class
                        )
                )

                .guestFirstName(
                        row.get(
                                "guest_first_name",
                                String.class
                        )
                )

                .guestLastName(
                        row.get(
                                "guest_last_name",
                                String.class
                        )
                )

                .guestDateOfBirth(
                        row.get(
                                "guest_date_of_birth",
                                LocalDate.class
                        )
                )

                .guestType(
                        guestTypeRaw != null
                                ? GuestType.valueOf(guestTypeRaw)
                                : null
                )

                .notes(
                        row.get(
                                "notes",
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

    private CartItemGuestResponse toResponse(
            CartItemGuestEntity entity
    ) {

        CartItemGuestResponse response =
                new CartItemGuestResponse();

        response.setId(
                entity.getId()
        );

        response.setCartItemId(
                entity.getCartItemId()
        );

        response.setGuestProfileId(
                entity.getGuestProfileId()
        );

        response.setGuestFirstName(
                entity.getGuestFirstName()
        );

        response.setGuestLastName(
                entity.getGuestLastName()
        );

        response.setGuestDateOfBirth(
                entity.getGuestDateOfBirth()
        );

        response.setGuestType(
                entity.getGuestType()
        );

        response.setNotes(
                entity.getNotes()
        );

        response.setCreatedAt(
                entity.getCreatedAt()
        );

        return response;
    }
}