package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.BookingStatus;
import io.booking.booking_service.datatype.booking.GuestType;
import io.booking.booking_service.dto.pojo.bookingguest.BookingGuestRequest;
import io.booking.booking_service.dto.pojo.bookingguest.BookingGuestResponse;
import io.booking.booking_service.dto.pojo.bookingguest.BookingGuestUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.BookingGuestEntity;
import io.booking.booking_service.repository.BookingGuestRepository;
import io.booking.booking_service.service.BookingGuestService;
import io.booking.booking_service.util.validators.BookingGuest;
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
public class BookingGuestServiceImpl implements BookingGuestService {

    private final BookingGuestRepository repository;
    private final ReactiveTx reactiveTx;
    private final BookingGuest validator;
    private final DatabaseClient db;

    public BookingGuestServiceImpl(BookingGuestRepository repository, ReactiveTx reactiveTx,
                                   BookingGuest validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<BookingGuestResponse> create(String requestId, BookingGuestRequest request) {
        if (request == null) return Mono.error(BookingException.of(BookingErrorType.BOOKING_GUEST_REQUEST_REQUIRED));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        BookingStatus status = request.getStatus() != null ? request.getStatus() : BookingStatus.CONFIRMED;

        return validator.validateBookingItemRequired(request.getBookingItemId())
                .then(validator.validateNameRequired(request.getGuestFirstName(), request.getGuestLastName()))
                .then(validator.validateGuestTypeRequired(request.getGuestType()))
                .then(validator.validateDateOfBirth(request.getGuestDateOfBirth()))
                .then(Mono.defer(() -> reactiveTx.required(() -> insertGuest(request, status, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createBookingGuest] requestId={} bookingItemId={}", requestId, request.getBookingItemId()))
                .doOnSuccess(r -> log.info("[createBookingGuest] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createBookingGuest] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<BookingGuestEntity> insertGuest(BookingGuestRequest r, BookingStatus status, OffsetDateTime now) {
        return db.sql("""
                INSERT INTO booking_guests
                    (booking_item_id, order_item_guest_id, guest_profile_id,
                     guest_first_name, guest_last_name, guest_date_of_birth,
                     guest_type, status, notes, created_at)
                VALUES
                    (:bookingItemId, :orderItemGuestId, :guestProfileId,
                     :guestFirstName, :guestLastName, :guestDateOfBirth,
                     CAST(:guestType AS guest_type), CAST(:status AS booking_status), :notes, :createdAt)
                RETURNING *
                """)
                .bind("bookingItemId", r.getBookingItemId())
                .bind("orderItemGuestId", r.getOrderItemGuestId() != null ? r.getOrderItemGuestId() : (Object) null)
                .bind("guestProfileId", r.getGuestProfileId() != null ? r.getGuestProfileId() : (Object) null)
                .bind("guestFirstName", r.getGuestFirstName().trim())
                .bind("guestLastName", r.getGuestLastName().trim())
                .bind("guestDateOfBirth", r.getGuestDateOfBirth() != null ? r.getGuestDateOfBirth() : (Object) null)
                .bind("guestType", r.getGuestType().name())
                .bind("status", status.name())
                .bind("notes", r.getNotes() != null ? r.getNotes() : "")
                .bind("createdAt", now)
                .map((row, meta) -> mapRow(row))
                .one();
    }

    @Override
    public Mono<BookingGuestResponse> update(String requestId, UUID id, BookingGuestUpdate request) {
        if (request == null) return Mono.error(BookingException.of(BookingErrorType.BOOKING_GUEST_REQUEST_REQUIRED));
        return validator.load(id)
                .flatMap(e -> validator.validateDateOfBirth(request.getGuestDateOfBirth()).thenReturn(e))
                .flatMap(e -> Mono.defer(() -> reactiveTx.required(() -> updateGuest(e, request))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateBookingGuest] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateBookingGuest] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<BookingGuestEntity> updateGuest(BookingGuestEntity e, BookingGuestUpdate r) {
        String firstName = r.getGuestFirstName() != null ? r.getGuestFirstName().trim() : e.getGuestFirstName();
        String lastName = r.getGuestLastName() != null ? r.getGuestLastName().trim() : e.getGuestLastName();
        LocalDate dob = r.getGuestDateOfBirth() != null ? r.getGuestDateOfBirth() : e.getGuestDateOfBirth();
        GuestType guestType = r.getGuestType() != null ? r.getGuestType() : e.getGuestType();
        BookingStatus status = r.getStatus() != null ? r.getStatus() : e.getStatus();
        String notes = r.getNotes() != null ? r.getNotes() : e.getNotes() != null ? e.getNotes() : "";

        return db.sql("""
                UPDATE booking_guests
                SET guest_first_name   = :guestFirstName,
                    guest_last_name    = :guestLastName,
                    guest_date_of_birth = :guestDateOfBirth,
                    guest_type         = CAST(:guestType AS guest_type),
                    status             = CAST(:status AS booking_status),
                    notes              = :notes
                WHERE id = :id
                RETURNING *
                """)
                .bind("guestFirstName", firstName)
                .bind("guestLastName", lastName)
                .bind("guestDateOfBirth", dob != null ? dob : (Object) null)
                .bind("guestType", guestType.name())
                .bind("status", status.name())
                .bind("notes", notes)
                .bind("id", e.getId())
                .map((row, meta) -> mapRow(row))
                .one();
    }

    @Override
    public Mono<BookingGuestResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getBookingGuest] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getBookingGuest] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Flux<BookingGuestResponse> getByBookingItem(String requestId, UUID bookingItemId) {
        if (bookingItemId == null) return Flux.error(BookingException.of(BookingErrorType.BOOKING_GUEST_REQUEST_INVALID));
        return repository.findByBookingItemId(bookingItemId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getBookingGuestByBookingItem] requestId={} bookingItemId={}", requestId, bookingItemId))
                .doOnError(e -> log.error("[getBookingGuestByBookingItem] Failed requestId={} bookingItemId={} error={}", requestId, bookingItemId, e.getMessage(), e));
    }

    @Override
    public Flux<BookingGuestResponse> getByGuestProfile(String requestId, UUID guestProfileId) {
        if (guestProfileId == null) return Flux.error(BookingException.of(BookingErrorType.BOOKING_GUEST_REQUEST_INVALID));
        return repository.findByGuestProfileId(guestProfileId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getBookingGuestByGuestProfile] requestId={} guestProfileId={}", requestId, guestProfileId))
                .doOnError(e -> log.error("[getBookingGuestByGuestProfile] Failed requestId={} guestProfileId={} error={}", requestId, guestProfileId, e.getMessage(), e));
    }

    @Override
    public Mono<Void> delete(String requestId, UUID id) {
        return validator.load(id)
                .flatMap(e -> reactiveTx.required(() -> repository.deleteById(e.getId())))
                .doOnSubscribe(s -> log.info("[deleteBookingGuest] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[deleteBookingGuest] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private BookingGuestEntity mapRow(io.r2dbc.spi.Row row) {
        return BookingGuestEntity.builder()
                .id(row.get("id", UUID.class))
                .bookingItemId(row.get("booking_item_id", UUID.class))
                .orderItemGuestId(row.get("order_item_guest_id", UUID.class))
                .guestProfileId(row.get("guest_profile_id", UUID.class))
                .guestFirstName(row.get("guest_first_name", String.class))
                .guestLastName(row.get("guest_last_name", String.class))
                .guestDateOfBirth(row.get("guest_date_of_birth", LocalDate.class))
                .guestType(GuestType.valueOf(row.get("guest_type", String.class)))
                .status(BookingStatus.valueOf(row.get("status", String.class)))
                .notes(row.get("notes", String.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .build();
    }

    private BookingGuestResponse toResponse(BookingGuestEntity e) {
        BookingGuestResponse r = new BookingGuestResponse();
        r.setId(e.getId());
        r.setBookingItemId(e.getBookingItemId());
        r.setOrderItemGuestId(e.getOrderItemGuestId());
        r.setGuestProfileId(e.getGuestProfileId());
        r.setGuestFirstName(e.getGuestFirstName());
        r.setGuestLastName(e.getGuestLastName());
        r.setGuestDateOfBirth(e.getGuestDateOfBirth());
        r.setGuestType(e.getGuestType());
        r.setStatus(e.getStatus());
        r.setNotes(e.getNotes());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}
