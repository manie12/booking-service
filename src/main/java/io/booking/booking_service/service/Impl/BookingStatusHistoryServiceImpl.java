package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.BookingStatus;
import io.booking.booking_service.dto.pojo.bookingstatushistory.BookingStatusHistoryRequest;
import io.booking.booking_service.dto.pojo.bookingstatushistory.BookingStatusHistoryResponse;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.BookingStatusHistoryEntity;
import io.booking.booking_service.service.BookingStatusHistoryService;
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
public class BookingStatusHistoryServiceImpl implements BookingStatusHistoryService {

    private final io.booking.booking_service.repository.BookingStatusHistory bookingStatusHistory;
    private final ReactiveTx reactiveTx;
    private final io.booking.booking_service.util.validators.BookingStatusHistoryValidator validator;
    private final DatabaseClient db;

    public BookingStatusHistoryServiceImpl(
            io.booking.booking_service.repository.BookingStatusHistory bookingStatusHistory,
            ReactiveTx reactiveTx,
            io.booking.booking_service.util.validators.BookingStatusHistoryValidator validator,
            DatabaseClient db) {
        this.bookingStatusHistory = bookingStatusHistory;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<BookingStatusHistoryResponse> create(String requestId, BookingStatusHistoryRequest request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_REQUEST_INVALID));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        return validator.validateBookingRequired(request.getBookingId())
                .then(validator.validateNewStatusRequired(request.getNewStatus()))
                .then(validator.assertStatusTransition(request.getOldStatus(), request.getNewStatus()))
                .then(Mono.defer(() -> reactiveTx.required(() -> insert(request, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createBookingStatusHistory] requestId={} bookingId={}", requestId, request.getBookingId()))
                .doOnSuccess(r -> log.info("[createBookingStatusHistory] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createBookingStatusHistory] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<BookingStatusHistoryEntity> insert(BookingStatusHistoryRequest r, OffsetDateTime now) {
        var spec = db.sql("""
                INSERT INTO booking_status_history
                    (booking_id, booking_item_id, old_status, new_status,
                     actor_type, actor_id, reason_code, notes, created_at)
                VALUES
                    (:bookingId, :bookingItemId, CAST(:oldStatus AS booking_status), CAST(:newStatus AS booking_status),
                     CAST(:actorType AS actor_type), :actorId, :reasonCode, :notes, :createdAt)
                RETURNING *
                """)
                .bind("bookingId", r.getBookingId())
                .bind("actorId", r.getActorId() != null ? r.getActorId() : "")
                .bind("reasonCode", r.getReasonCode() != null ? r.getReasonCode() : "")
                .bind("notes", r.getNotes() != null ? r.getNotes() : "")
                .bind("createdAt", now);

        if (r.getBookingItemId() != null) spec = spec.bind("bookingItemId", r.getBookingItemId());
        else spec = spec.bindNull("bookingItemId", UUID.class);

        if (r.getOldStatus() != null) spec = spec.bind("oldStatus", r.getOldStatus().name());
        else spec = spec.bindNull("oldStatus", String.class);

        spec = spec.bind("newStatus", r.getNewStatus().name());

        if (r.getActorType() != null) spec = spec.bind("actorType", r.getActorType().name());
        else spec = spec.bindNull("actorType", String.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<BookingStatusHistoryResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getBookingStatusHistory] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getBookingStatusHistory] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Flux<BookingStatusHistoryResponse> getByBooking(String requestId, UUID bookingId) {
        if (bookingId == null)
            return Flux.error(BookingException.of(BookingErrorType.BOOKING_NOT_FOUND));
        return bookingStatusHistory.findByBookingId(bookingId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getBookingStatusHistoryByBooking] requestId={} bookingId={}", requestId, bookingId))
                .doOnError(e -> log.error("[getBookingStatusHistoryByBooking] Failed requestId={} bookingId={} error={}", requestId, bookingId, e.getMessage(), e));
    }

    @Override
    public Flux<BookingStatusHistoryResponse> getByBookingItem(String requestId, UUID bookingItemId) {
        if (bookingItemId == null)
            return Flux.error(BookingException.of(BookingErrorType.BOOKING_NOT_FOUND));
        return bookingStatusHistory.findByBookingItemId(bookingItemId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getBookingStatusHistoryByBookingItem] requestId={} bookingItemId={}", requestId, bookingItemId))
                .doOnError(e -> log.error("[getBookingStatusHistoryByBookingItem] Failed requestId={} bookingItemId={} error={}", requestId, bookingItemId, e.getMessage(), e));
    }

    private BookingStatusHistoryEntity mapRow(io.r2dbc.spi.Row row) {
        String oldStatusRaw = row.get("old_status", String.class);
        String actorTypeRaw = row.get("actor_type", String.class);
        return BookingStatusHistoryEntity.builder()
                .id(row.get("id", UUID.class))
                .bookingId(row.get("booking_id", UUID.class))
                .bookingItemId(row.get("booking_item_id", UUID.class))
                .oldStatus(oldStatusRaw != null ? BookingStatus.valueOf(oldStatusRaw) : null)
                .newStatus(BookingStatus.valueOf(row.get("new_status", String.class)))
                .actorType(actorTypeRaw != null ? ActorType.valueOf(actorTypeRaw) : null)
                .actorId(row.get("actor_id", String.class))
                .reasonCode(row.get("reason_code", String.class))
                .notes(row.get("notes", String.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .build();
    }

    private BookingStatusHistoryResponse toResponse(BookingStatusHistoryEntity e) {
        BookingStatusHistoryResponse r = new BookingStatusHistoryResponse();
        r.setId(e.getId());
        r.setBookingId(e.getBookingId());
        r.setBookingItemId(e.getBookingItemId());
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
