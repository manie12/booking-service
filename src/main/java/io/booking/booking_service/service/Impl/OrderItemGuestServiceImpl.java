package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.GuestType;
import io.booking.booking_service.dto.pojo.orderitemguest.OrderItemGuestRequest;
import io.booking.booking_service.dto.pojo.orderitemguest.OrderItemGuestResponse;
import io.booking.booking_service.dto.pojo.orderitemguest.OrderItemGuestUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.OrderItemGuestEntity;
import io.booking.booking_service.repository.OrderItemGuestRepository;
import io.booking.booking_service.service.OrderItemGuestService;
import io.booking.booking_service.util.validators.OrderItemGuest;
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
public class OrderItemGuestServiceImpl implements OrderItemGuestService {

    private final OrderItemGuestRepository repository;
    private final ReactiveTx reactiveTx;
    private final OrderItemGuest validator;
    private final DatabaseClient db;

    public OrderItemGuestServiceImpl(OrderItemGuestRepository repository, ReactiveTx reactiveTx,
                                     OrderItemGuest validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<OrderItemGuestResponse> create(String requestId, OrderItemGuestRequest request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_GUEST_NOT_FOUND));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        return validator.validateOrderItemRequired(request.getOrderItemId())
                .then(validator.validateNameRequired(request.getGuestFirstName(), request.getGuestLastName()))
                .then(validator.validateGuestTypeRequired(request.getGuestType()))
                .then(validator.validateDateOfBirth(request.getGuestDateOfBirth()))
                .then(Mono.defer(() -> reactiveTx.required(() -> insert(request, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createOrderItemGuest] requestId={} orderItemId={}", requestId, request.getOrderItemId()))
                .doOnSuccess(r -> log.info("[createOrderItemGuest] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createOrderItemGuest] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<OrderItemGuestEntity> insert(OrderItemGuestRequest r, OffsetDateTime now) {
        var spec = db.sql("""
                INSERT INTO order_item_guests
                    (order_item_id, guest_profile_id, guest_first_name, guest_last_name,
                     guest_date_of_birth, guest_type, ticket_required, waiver_required,
                     notes, created_at)
                VALUES
                    (:orderItemId, :guestProfileId, :guestFirstName, :guestLastName,
                     :guestDateOfBirth, CAST(:guestType AS guest_type), :ticketRequired, :waiverRequired,
                     :notes, :createdAt)
                RETURNING *
                """)
                .bind("orderItemId", r.getOrderItemId())
                .bind("guestFirstName", r.getGuestFirstName())
                .bind("guestLastName", r.getGuestLastName())
                .bind("guestType", r.getGuestType().name())
                .bind("ticketRequired", r.getTicketRequired() != null ? r.getTicketRequired() : false)
                .bind("waiverRequired", r.getWaiverRequired() != null ? r.getWaiverRequired() : false)
                .bind("notes", r.getNotes() != null ? r.getNotes() : "")
                .bind("createdAt", now);

        if (r.getGuestProfileId() != null) spec = spec.bind("guestProfileId", r.getGuestProfileId());
        else spec = spec.bindNull("guestProfileId", UUID.class);

        if (r.getGuestDateOfBirth() != null) spec = spec.bind("guestDateOfBirth", r.getGuestDateOfBirth());
        else spec = spec.bindNull("guestDateOfBirth", LocalDate.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<OrderItemGuestResponse> update(String requestId, UUID id, OrderItemGuestUpdate request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_GUEST_NOT_FOUND));
        return validator.load(id)
                .flatMap(e -> validator.validateDateOfBirth(request.getGuestDateOfBirth()).thenReturn(e))
                .flatMap(e -> Mono.defer(() -> reactiveTx.required(() -> updateGuest(e, request))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateOrderItemGuest] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateOrderItemGuest] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<OrderItemGuestEntity> updateGuest(OrderItemGuestEntity e, OrderItemGuestUpdate r) {
        String firstName = r.getGuestFirstName() != null ? r.getGuestFirstName() : e.getGuestFirstName();
        String lastName = r.getGuestLastName() != null ? r.getGuestLastName() : e.getGuestLastName();
        GuestType guestType = r.getGuestType() != null ? r.getGuestType() : e.getGuestType();
        LocalDate dob = r.getGuestDateOfBirth() != null ? r.getGuestDateOfBirth() : e.getGuestDateOfBirth();
        UUID guestProfileId = r.getGuestProfileId() != null ? r.getGuestProfileId() : e.getGuestProfileId();
        Boolean ticketRequired = r.getTicketRequired() != null ? r.getTicketRequired() : e.getTicketRequired() != null ? e.getTicketRequired() : false;
        Boolean waiverRequired = r.getWaiverRequired() != null ? r.getWaiverRequired() : e.getWaiverRequired() != null ? e.getWaiverRequired() : false;
        String notes = r.getNotes() != null ? r.getNotes() : e.getNotes() != null ? e.getNotes() : "";

        var spec = db.sql("""
                UPDATE order_item_guests
                SET guest_profile_id    = :guestProfileId,
                    guest_first_name    = :guestFirstName,
                    guest_last_name     = :guestLastName,
                    guest_date_of_birth = :guestDateOfBirth,
                    guest_type          = CAST(:guestType AS guest_type),
                    ticket_required     = :ticketRequired,
                    waiver_required     = :waiverRequired,
                    notes               = :notes
                WHERE id = :id
                RETURNING *
                """)
                .bind("guestFirstName", firstName)
                .bind("guestLastName", lastName)
                .bind("guestType", guestType.name())
                .bind("ticketRequired", ticketRequired)
                .bind("waiverRequired", waiverRequired)
                .bind("notes", notes)
                .bind("id", e.getId());

        if (guestProfileId != null) spec = spec.bind("guestProfileId", guestProfileId);
        else spec = spec.bindNull("guestProfileId", UUID.class);

        if (dob != null) spec = spec.bind("guestDateOfBirth", dob);
        else spec = spec.bindNull("guestDateOfBirth", LocalDate.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<OrderItemGuestResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getOrderItemGuest] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getOrderItemGuest] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Flux<OrderItemGuestResponse> getByOrderItem(String requestId, UUID orderItemId) {
        if (orderItemId == null)
            return Flux.error(BookingException.of(BookingErrorType.ORDER_ITEM_NOT_FOUND));
        return repository.findByOrderItemId(orderItemId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getOrderItemGuestsByOrderItem] requestId={} orderItemId={}", requestId, orderItemId))
                .doOnError(e -> log.error("[getOrderItemGuestsByOrderItem] Failed requestId={} orderItemId={} error={}", requestId, orderItemId, e.getMessage(), e));
    }

    @Override
    public Flux<OrderItemGuestResponse> getByGuestProfile(String requestId, UUID guestProfileId) {
        if (guestProfileId == null)
            return Flux.error(BookingException.of(BookingErrorType.ORDER_ITEM_GUEST_NOT_FOUND));
        return repository.findByGuestProfileId(guestProfileId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getOrderItemGuestsByGuestProfile] requestId={} guestProfileId={}", requestId, guestProfileId))
                .doOnError(e -> log.error("[getOrderItemGuestsByGuestProfile] Failed requestId={} guestProfileId={} error={}", requestId, guestProfileId, e.getMessage(), e));
    }

    @Override
    public Mono<Void> delete(String requestId, UUID id) {
        return validator.load(id)
                .flatMap(e -> reactiveTx.required(() -> db.sql("""
                        DELETE FROM order_item_guests
                        WHERE id = :id
                        """)
                        .bind("id", e.getId())
                        .fetch().rowsUpdated()))
                .then()
                .doOnSubscribe(s -> log.info("[deleteOrderItemGuest] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[deleteOrderItemGuest] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private OrderItemGuestEntity mapRow(io.r2dbc.spi.Row row) {
        String guestTypeRaw = row.get("guest_type", String.class);
        return OrderItemGuestEntity.builder()
                .id(row.get("id", UUID.class))
                .orderItemId(row.get("order_item_id", UUID.class))
                .guestProfileId(row.get("guest_profile_id", UUID.class))
                .guestFirstName(row.get("guest_first_name", String.class))
                .guestLastName(row.get("guest_last_name", String.class))
                .guestDateOfBirth(row.get("guest_date_of_birth", LocalDate.class))
                .guestType(guestTypeRaw != null ? GuestType.valueOf(guestTypeRaw) : null)
                .ticketRequired(row.get("ticket_required", Boolean.class))
                .waiverRequired(row.get("waiver_required", Boolean.class))
                .notes(row.get("notes", String.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .build();
    }

    private OrderItemGuestResponse toResponse(OrderItemGuestEntity e) {
        OrderItemGuestResponse r = new OrderItemGuestResponse();
        r.setId(e.getId());
        r.setOrderItemId(e.getOrderItemId());
        r.setGuestProfileId(e.getGuestProfileId());
        r.setGuestFirstName(e.getGuestFirstName());
        r.setGuestLastName(e.getGuestLastName());
        r.setGuestDateOfBirth(e.getGuestDateOfBirth());
        r.setGuestType(e.getGuestType());
        r.setTicketRequired(e.getTicketRequired());
        r.setWaiverRequired(e.getWaiverRequired());
        r.setNotes(e.getNotes());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}
