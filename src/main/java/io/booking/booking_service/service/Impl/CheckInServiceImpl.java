package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CheckInResultStatus;
import io.booking.booking_service.dto.pojo.checkin.CheckInRequest;
import io.booking.booking_service.dto.pojo.checkin.CheckInResponse;
import io.booking.booking_service.dto.pojo.checkin.CheckInUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.CheckInEntity;
import io.booking.booking_service.repository.CheckInRepository;
import io.booking.booking_service.service.CheckInService;
import io.booking.booking_service.util.validators.CheckIn;
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
public class CheckInServiceImpl implements CheckInService {

    private final CheckInRepository repository;
    private final ReactiveTx reactiveTx;
    private final CheckIn validator;
    private final DatabaseClient db;

    public CheckInServiceImpl(CheckInRepository repository, ReactiveTx reactiveTx,
                               CheckIn validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<CheckInResponse> create(String requestId, CheckInRequest request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.CHECK_IN_REQUEST_INVALID));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        CheckInResultStatus resultStatus = request.getResultStatus() != null
                ? request.getResultStatus() : CheckInResultStatus.GRANTED;
        OffsetDateTime checkInAt = request.getCheckInAt() != null ? request.getCheckInAt() : now;

        return validator.validateEntitlementRequired(request.getEntitlementId())
                .then(validator.validateLocationRequired(request.getLocationCode()))
                .then(validator.validateDeviceRequired(request.getDeviceCode()))
                .then(validator.assertNotAlreadyCheckedIn(request.getEntitlementId()))
                .then(Mono.defer(() -> reactiveTx.required(() -> insert(request, resultStatus, checkInAt, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createCheckIn] requestId={} entitlementId={}", requestId, request.getEntitlementId()))
                .doOnSuccess(r -> log.info("[createCheckIn] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createCheckIn] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<CheckInEntity> insert(CheckInRequest r, CheckInResultStatus resultStatus,
                                       OffsetDateTime checkInAt, OffsetDateTime now) {
        var spec = db.sql("""
                INSERT INTO check_ins
                    (tenant_id, entitlement_id, booking_id, booking_item_id, guest_profile_id,
                     check_in_at, location_code, device_code,
                     actor_type, actor_id, result_status, notes, created_at)
                VALUES
                    (:tenantId, :entitlementId, :bookingId, :bookingItemId, :guestProfileId,
                     :checkInAt, :locationCode, :deviceCode,
                     CAST(:actorType AS actor_type), :actorId,
                     CAST(:resultStatus AS check_in_result_status), :notes, :createdAt)
                RETURNING *
                """)
                .bind("tenantId", r.getTenantId())
                .bind("entitlementId", r.getEntitlementId())
                .bind("locationCode", r.getLocationCode())
                .bind("deviceCode", r.getDeviceCode())
                .bind("actorId", r.getActorId() != null ? r.getActorId() : "")
                .bind("resultStatus", resultStatus.name())
                .bind("notes", r.getNotes() != null ? r.getNotes() : "")
                .bind("checkInAt", checkInAt)
                .bind("createdAt", now);

        if (r.getBookingId() != null) spec = spec.bind("bookingId", r.getBookingId());
        else spec = spec.bindNull("bookingId", UUID.class);

        if (r.getBookingItemId() != null) spec = spec.bind("bookingItemId", r.getBookingItemId());
        else spec = spec.bindNull("bookingItemId", UUID.class);

        if (r.getGuestProfileId() != null) spec = spec.bind("guestProfileId", r.getGuestProfileId());
        else spec = spec.bindNull("guestProfileId", UUID.class);

        if (r.getActorType() != null) spec = spec.bind("actorType", r.getActorType().name());
        else spec = spec.bindNull("actorType", String.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<CheckInResponse> update(String requestId, UUID id, CheckInUpdate request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.CHECK_IN_REQUEST_INVALID));
        return validator.load(id)
                .flatMap(validator::assertNotDenied)
                .flatMap(e -> Mono.defer(() -> reactiveTx.required(() -> updateCheckIn(e, request))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateCheckIn] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateCheckIn] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<CheckInEntity> updateCheckIn(CheckInEntity e, CheckInUpdate r) {
        OffsetDateTime checkInAt = r.getCheckInAt() != null ? r.getCheckInAt() : e.getCheckInAt();
        String locationCode = r.getLocationCode() != null ? r.getLocationCode() : e.getLocationCode();
        String deviceCode = r.getDeviceCode() != null ? r.getDeviceCode() : e.getDeviceCode();
        ActorType actorType = r.getActorType() != null ? r.getActorType() : e.getActorType();
        String actorId = r.getActorId() != null ? r.getActorId() : e.getActorId() != null ? e.getActorId() : "";
        CheckInResultStatus resultStatus = r.getResultStatus() != null ? r.getResultStatus() : e.getResultStatus();
        String notes = r.getNotes() != null ? r.getNotes() : e.getNotes() != null ? e.getNotes() : "";

        var spec = db.sql("""
                UPDATE check_ins
                SET check_in_at   = :checkInAt,
                    location_code = :locationCode,
                    device_code   = :deviceCode,
                    actor_type    = CAST(:actorType AS actor_type),
                    actor_id      = :actorId,
                    result_status = CAST(:resultStatus AS check_in_result_status),
                    notes         = :notes
                WHERE id = :id
                RETURNING *
                """)
                .bind("checkInAt", checkInAt)
                .bind("locationCode", locationCode)
                .bind("deviceCode", deviceCode)
                .bind("actorId", actorId)
                .bind("resultStatus", resultStatus.name())
                .bind("notes", notes)
                .bind("id", e.getId());

        if (actorType != null) spec = spec.bind("actorType", actorType.name());
        else spec = spec.bindNull("actorType", String.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<CheckInResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCheckIn] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getCheckIn] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Flux<CheckInResponse> getByEntitlement(String requestId, UUID entitlementId) {
        if (entitlementId == null)
            return Flux.error(BookingException.of(BookingErrorType.CHECK_IN_ENTITLEMENT_REQUIRED));
        return repository.findByEntitlementId(entitlementId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCheckInsByEntitlement] requestId={} entitlementId={}", requestId, entitlementId))
                .doOnError(e -> log.error("[getCheckInsByEntitlement] Failed requestId={} entitlementId={} error={}", requestId, entitlementId, e.getMessage(), e));
    }

    @Override
    public Flux<CheckInResponse> getByBooking(String requestId, UUID bookingId) {
        if (bookingId == null)
            return Flux.error(BookingException.of(BookingErrorType.BOOKING_NOT_FOUND));
        return repository.findByBookingId(bookingId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCheckInsByBooking] requestId={} bookingId={}", requestId, bookingId))
                .doOnError(e -> log.error("[getCheckInsByBooking] Failed requestId={} bookingId={} error={}", requestId, bookingId, e.getMessage(), e));
    }

    @Override
    public Flux<CheckInResponse> getByGuestProfile(String requestId, UUID guestProfileId) {
        if (guestProfileId == null)
            return Flux.error(BookingException.of(BookingErrorType.CHECK_IN_NOT_FOUND));
        return repository.findByGuestProfileId(guestProfileId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCheckInsByGuestProfile] requestId={} guestProfileId={}", requestId, guestProfileId))
                .doOnError(e -> log.error("[getCheckInsByGuestProfile] Failed requestId={} guestProfileId={} error={}", requestId, guestProfileId, e.getMessage(), e));
    }

    private CheckInEntity mapRow(io.r2dbc.spi.Row row) {
        String actorTypeRaw = row.get("actor_type", String.class);
        String resultStatusRaw = row.get("result_status", String.class);
        return CheckInEntity.builder()
                .id(row.get("id", UUID.class))
                .tenantId(row.get("tenant_id", UUID.class))
                .entitlementId(row.get("entitlement_id", UUID.class))
                .bookingId(row.get("booking_id", UUID.class))
                .bookingItemId(row.get("booking_item_id", UUID.class))
                .guestProfileId(row.get("guest_profile_id", UUID.class))
                .checkInAt(row.get("check_in_at", OffsetDateTime.class))
                .locationCode(row.get("location_code", String.class))
                .deviceCode(row.get("device_code", String.class))
                .actorType(actorTypeRaw != null ? ActorType.valueOf(actorTypeRaw) : null)
                .actorId(row.get("actor_id", String.class))
                .resultStatus(resultStatusRaw != null ? CheckInResultStatus.valueOf(resultStatusRaw) : null)
                .notes(row.get("notes", String.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .build();
    }

    private CheckInResponse toResponse(CheckInEntity e) {
        CheckInResponse r = new CheckInResponse();
        r.setId(e.getId());
        r.setTenantId(e.getTenantId());
        r.setEntitlementId(e.getEntitlementId());
        r.setBookingId(e.getBookingId());
        r.setBookingItemId(e.getBookingItemId());
        r.setGuestProfileId(e.getGuestProfileId());
        r.setCheckInAt(e.getCheckInAt());
        r.setLocationCode(e.getLocationCode());
        r.setDeviceCode(e.getDeviceCode());
        r.setActorType(e.getActorType());
        r.setActorId(e.getActorId());
        r.setResultStatus(e.getResultStatus());
        r.setNotes(e.getNotes());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}
