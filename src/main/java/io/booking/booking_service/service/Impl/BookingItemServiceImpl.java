package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.BookingStatus;
import io.booking.booking_service.dto.pojo.bookingitem.BookingItemRequest;
import io.booking.booking_service.dto.pojo.bookingitem.BookingItemResponse;
import io.booking.booking_service.dto.pojo.bookingitem.BookingItemUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.BookingItemEntity;
import io.booking.booking_service.repository.BookingItemRepository;
import io.booking.booking_service.service.BookingItemService;
import io.booking.booking_service.util.validators.BookingItem;
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
public class BookingItemServiceImpl implements BookingItemService {

    private final BookingItemRepository repository;
    private final ReactiveTx reactiveTx;
    private final BookingItem validator;
    private final DatabaseClient db;

    public BookingItemServiceImpl(BookingItemRepository repository, ReactiveTx reactiveTx,
                                  BookingItem validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<BookingItemResponse> create(String requestId, BookingItemRequest request) {
        if (request == null) return Mono.error(BookingException.of(BookingErrorType.BOOKING_ITEM_REQUEST_REQUIRED));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        BookingStatus status = request.getStatus() != null ? request.getStatus() : BookingStatus.CONFIRMED;

        return validator.validateBookingRequired(request.getBookingId())
                .then(validator.validateScheduleInstanceRequired(request.getScheduleInstanceId()))
                .then(validator.validateQuantity(request.getQuantity()))
                .then(validator.validateTimeWindow(request.getStartAt(), request.getEndAt()))
                .then(Mono.defer(() -> reactiveTx.required(() -> insertItem(request, status, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createBookingItem] requestId={} bookingId={}", requestId, request.getBookingId()))
                .doOnSuccess(r -> log.info("[createBookingItem] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createBookingItem] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<BookingItemEntity> insertItem(BookingItemRequest r, BookingStatus status, OffsetDateTime now) {
        var spec = db.sql("""
                INSERT INTO booking_items
                    (booking_id, order_item_id, schedule_instance_id, capacity_pool_id,
                     product_id, product_variant_id, offer_id,
                     quantity, unit_count, start_at, end_at,
                     status, notes, created_at, updated_at)
                VALUES
                    (:bookingId, :orderItemId, :scheduleInstanceId, :capacityPoolId,
                     :productId, :productVariantId, :offerId,
                     :quantity, :unitCount, :startAt, :endAt,
                     CAST(:status AS booking_status), :notes, :createdAt, :updatedAt)
                RETURNING *
                """)
                .bind("bookingId", r.getBookingId())
                .bind("orderItemId", r.getOrderItemId())
                .bind("scheduleInstanceId", r.getScheduleInstanceId())
                .bind("quantity", r.getQuantity())
                .bind("unitCount", r.getUnitCount() != null ? r.getUnitCount() : r.getQuantity())
                .bind("status", status.name())
                .bind("notes", r.getNotes() != null ? r.getNotes() : "")
                .bind("createdAt", now)
                .bind("updatedAt", now);
        if (r.getCapacityPoolId() != null) spec = spec.bind("capacityPoolId", r.getCapacityPoolId());
        else spec = spec.bindNull("capacityPoolId", UUID.class);
        if (r.getProductId() != null) spec = spec.bind("productId", r.getProductId());
        else spec = spec.bindNull("productId", UUID.class);
        if (r.getProductVariantId() != null) spec = spec.bind("productVariantId", r.getProductVariantId());
        else spec = spec.bindNull("productVariantId", UUID.class);
        if (r.getOfferId() != null) spec = spec.bind("offerId", r.getOfferId());
        else spec = spec.bindNull("offerId", UUID.class);
        if (r.getStartAt() != null) spec = spec.bind("startAt", r.getStartAt());
        else spec = spec.bindNull("startAt", OffsetDateTime.class);
        if (r.getEndAt() != null) spec = spec.bind("endAt", r.getEndAt());
        else spec = spec.bindNull("endAt", OffsetDateTime.class);
        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<BookingItemResponse> update(String requestId, UUID id, BookingItemUpdate request) {
        if (request == null) return Mono.error(BookingException.of(BookingErrorType.BOOKING_ITEM_REQUEST_REQUIRED));
        return validator.load(id)
                .flatMap(validator::assertNotCancelled)
                .flatMap(validator::assertNotCompleted)
                .flatMap(e -> validator.validateTimeWindow(
                        request.getStartAt() != null ? request.getStartAt() : e.getStartAt(),
                        request.getEndAt() != null ? request.getEndAt() : e.getEndAt()
                ).thenReturn(e))
                .flatMap(e -> validator.validateQuantity(
                        request.getQuantity() != null ? request.getQuantity() : e.getQuantity()
                ).thenReturn(e))
                .flatMap(e -> Mono.defer(() -> reactiveTx.required(() -> updateItem(e, request))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateBookingItem] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateBookingItem] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<BookingItemEntity> updateItem(BookingItemEntity e, BookingItemUpdate r) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Integer quantity  = r.getQuantity() != null ? r.getQuantity() : e.getQuantity();
        Integer unitCount = r.getUnitCount() != null ? r.getUnitCount() : e.getUnitCount();
        OffsetDateTime startAt = r.getStartAt() != null ? r.getStartAt() : e.getStartAt();
        OffsetDateTime endAt   = r.getEndAt() != null ? r.getEndAt() : e.getEndAt();
        BookingStatus status   = r.getStatus() != null ? r.getStatus() : e.getStatus();
        String notes           = r.getNotes() != null ? r.getNotes() : e.getNotes() != null ? e.getNotes() : "";

        var spec = db.sql("""
                UPDATE booking_items
                SET quantity   = :quantity,
                    unit_count = :unitCount,
                    start_at   = :startAt,
                    end_at     = :endAt,
                    status     = CAST(:status AS booking_status),
                    notes      = :notes,
                    updated_at = :updatedAt
                WHERE id = :id
                RETURNING *
                """)
                .bind("quantity", quantity)
                .bind("unitCount", unitCount != null ? unitCount : quantity)
                .bind("status", status.name())
                .bind("notes", notes)
                .bind("updatedAt", now)
                .bind("id", e.getId());
        if (startAt != null) spec = spec.bind("startAt", startAt);
        else spec = spec.bindNull("startAt", OffsetDateTime.class);
        if (endAt != null) spec = spec.bind("endAt", endAt);
        else spec = spec.bindNull("endAt", OffsetDateTime.class);
        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<BookingItemResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getBookingItem] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getBookingItem] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Flux<BookingItemResponse> getByBooking(String requestId, UUID bookingId) {
        if (bookingId == null) return Flux.error(BookingException.of(BookingErrorType.BOOKING_ITEM_REQUEST_INVALID));
        return repository.findByBookingId(bookingId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getBookingItemsByBooking] requestId={} bookingId={}", requestId, bookingId))
                .doOnError(e -> log.error("[getBookingItemsByBooking] Failed requestId={} bookingId={} error={}", requestId, bookingId, e.getMessage(), e));
    }

    @Override
    public Flux<BookingItemResponse> getByScheduleInstance(String requestId, UUID scheduleInstanceId) {
        if (scheduleInstanceId == null) return Flux.error(BookingException.of(BookingErrorType.BOOKING_ITEM_REQUEST_INVALID));
        return repository.findByScheduleInstanceId(scheduleInstanceId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getBookingItemsByScheduleInstance] requestId={} scheduleInstanceId={}", requestId, scheduleInstanceId))
                .doOnError(e -> log.error("[getBookingItemsByScheduleInstance] Failed requestId={} scheduleInstanceId={} error={}", requestId, scheduleInstanceId, e.getMessage(), e));
    }

    @Override
    public Flux<BookingItemResponse> getByOrderItem(String requestId, UUID orderItemId) {
        if (orderItemId == null) return Flux.error(BookingException.of(BookingErrorType.BOOKING_ITEM_REQUEST_INVALID));
        return repository.findByOrderItemId(orderItemId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getBookingItemsByOrderItem] requestId={} orderItemId={}", requestId, orderItemId))
                .doOnError(e -> log.error("[getBookingItemsByOrderItem] Failed requestId={} orderItemId={} error={}", requestId, orderItemId, e.getMessage(), e));
    }

    @Override
    public Mono<Void> cancel(String requestId, UUID id) {
        return validator.load(id)
                .flatMap(validator::assertNotCancelled)
                .flatMap(e -> reactiveTx.required(() -> db.sql("""
                        UPDATE booking_items
                        SET status     = CAST(:status AS booking_status),
                            updated_at = :updatedAt
                        WHERE id = :id
                        RETURNING id
                        """)
                        .bind("status", BookingStatus.CANCELLED.name())
                        .bind("updatedAt", OffsetDateTime.now(ZoneOffset.UTC))
                        .bind("id", e.getId())
                        .fetch().rowsUpdated()))
                .then()
                .doOnSubscribe(s -> log.info("[cancelBookingItem] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[cancelBookingItem] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private BookingItemEntity mapRow(io.r2dbc.spi.Row row) {
        return BookingItemEntity.builder()
                .id(row.get("id", UUID.class))
                .bookingId(row.get("booking_id", UUID.class))
                .orderItemId(row.get("order_item_id", UUID.class))
                .scheduleInstanceId(row.get("schedule_instance_id", UUID.class))
                .capacityPoolId(row.get("capacity_pool_id", UUID.class))
                .productId(row.get("product_id", UUID.class))
                .productVariantId(row.get("product_variant_id", UUID.class))
                .offerId(row.get("offer_id", UUID.class))
                .quantity(row.get("quantity", Integer.class))
                .unitCount(row.get("unit_count", Integer.class))
                .startAt(row.get("start_at", OffsetDateTime.class))
                .endAt(row.get("end_at", OffsetDateTime.class))
                .status(BookingStatus.valueOf(row.get("status", String.class)))
                .notes(row.get("notes", String.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .updatedAt(row.get("updated_at", OffsetDateTime.class))
                .build();
    }

    private BookingItemResponse toResponse(BookingItemEntity e) {
        BookingItemResponse r = new BookingItemResponse();
        r.setId(e.getId());
        r.setBookingId(e.getBookingId());
        r.setOrderItemId(e.getOrderItemId());
        r.setScheduleInstanceId(e.getScheduleInstanceId());
        r.setCapacityPoolId(e.getCapacityPoolId());
        r.setProductId(e.getProductId());
        r.setProductVariantId(e.getProductVariantId());
        r.setOfferId(e.getOfferId());
        r.setQuantity(e.getQuantity());
        r.setUnitCount(e.getUnitCount());
        r.setStartAt(e.getStartAt());
        r.setEndAt(e.getEndAt());
        r.setStatus(e.getStatus());
        r.setNotes(e.getNotes());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}
