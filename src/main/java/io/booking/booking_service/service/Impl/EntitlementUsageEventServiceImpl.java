package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.UsageEventType;
import io.booking.booking_service.dto.pojo.entitlementusageevent.EntitlementUsageEventRequest;
import io.booking.booking_service.dto.pojo.entitlementusageevent.EntitlementUsageEventResponse;
import io.booking.booking_service.dto.pojo.entitlementusageevent.EntitlementUsageEventUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.EntitlementUsageEventEntity;
import io.booking.booking_service.repository.EntitlementUsageEventRepository;
import io.booking.booking_service.service.EntitlementUsageEventService;
import io.booking.booking_service.util.validators.EntitlementUsageEvent;
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
public class EntitlementUsageEventServiceImpl implements EntitlementUsageEventService {

    private final EntitlementUsageEventRepository repository;
    private final ReactiveTx reactiveTx;
    private final EntitlementUsageEvent validator;
    private final DatabaseClient db;

    public EntitlementUsageEventServiceImpl(EntitlementUsageEventRepository repository, ReactiveTx reactiveTx,
                                            EntitlementUsageEvent validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<EntitlementUsageEventResponse> create(String requestId, EntitlementUsageEventRequest request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_USAGE_EVENT_REQUEST_INVALID));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime eventAt = request.getEventAt() != null ? request.getEventAt() : now;

        return validator.validateEntitlementRequired(request.getEntitlementId())
                .then(validator.validateEventTypeRequired(request.getEventType()))
                .then(validator.validateUsageDelta(request.getUsageDelta()))
                .then(Mono.defer(() -> reactiveTx.required(() -> insert(request, eventAt, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createEntitlementUsageEvent] requestId={} entitlementId={}", requestId, request.getEntitlementId()))
                .doOnSuccess(r -> log.info("[createEntitlementUsageEvent] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createEntitlementUsageEvent] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<EntitlementUsageEventEntity> insert(EntitlementUsageEventRequest r,
                                                     OffsetDateTime eventAt, OffsetDateTime now) {
        return db.sql("""
                INSERT INTO entitlement_usage_events
                    (entitlement_id, event_type, usage_delta,
                     event_at, location_code, reference_code, payload_json, created_at)
                VALUES
                    (:entitlementId, CAST(:eventType AS usage_event_type), :usageDelta,
                     :eventAt, :locationCode, :referenceCode, :payloadJson, :createdAt)
                RETURNING *
                """)
                .bind("entitlementId", r.getEntitlementId())
                .bind("eventType", r.getEventType().name())
                .bind("usageDelta", r.getUsageDelta())
                .bind("eventAt", eventAt)
                .bind("locationCode", r.getLocationCode() != null ? r.getLocationCode() : "")
                .bind("referenceCode", r.getReferenceCode() != null ? r.getReferenceCode() : "")
                .bind("payloadJson", r.getPayloadJson() != null ? r.getPayloadJson() : "")
                .bind("createdAt", now)
                .map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<EntitlementUsageEventResponse> update(String requestId, UUID id, EntitlementUsageEventUpdate request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.ENTITLEMENT_USAGE_EVENT_REQUEST_INVALID));
        return validator.load(id)
                .flatMap(e -> {
                    Integer delta = request.getUsageDelta() != null ? request.getUsageDelta() : e.getUsageDelta();
                    return validator.validateUsageDelta(delta).thenReturn(e);
                })
                .flatMap(e -> Mono.defer(() -> reactiveTx.required(() -> updateEvent(e, request))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateEntitlementUsageEvent] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateEntitlementUsageEvent] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<EntitlementUsageEventEntity> updateEvent(EntitlementUsageEventEntity e, EntitlementUsageEventUpdate r) {
        UsageEventType eventType = r.getEventType() != null ? r.getEventType() : e.getEventType();
        Integer usageDelta = r.getUsageDelta() != null ? r.getUsageDelta() : e.getUsageDelta();
        OffsetDateTime eventAt = r.getEventAt() != null ? r.getEventAt() : e.getEventAt();
        String locationCode = r.getLocationCode() != null ? r.getLocationCode() : e.getLocationCode() != null ? e.getLocationCode() : "";
        String referenceCode = r.getReferenceCode() != null ? r.getReferenceCode() : e.getReferenceCode() != null ? e.getReferenceCode() : "";
        String payloadJson = r.getPayloadJson() != null ? r.getPayloadJson() : e.getPayloadJson() != null ? e.getPayloadJson() : "";

        return db.sql("""
                UPDATE entitlement_usage_events
                SET event_type     = CAST(:eventType AS usage_event_type),
                    usage_delta    = :usageDelta,
                    event_at       = :eventAt,
                    location_code  = :locationCode,
                    reference_code = :referenceCode,
                    payload_json   = :payloadJson
                WHERE id = :id
                RETURNING *
                """)
                .bind("eventType", eventType.name())
                .bind("usageDelta", usageDelta)
                .bind("eventAt", eventAt)
                .bind("locationCode", locationCode)
                .bind("referenceCode", referenceCode)
                .bind("payloadJson", payloadJson)
                .bind("id", e.getId())
                .map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<EntitlementUsageEventResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getEntitlementUsageEvent] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getEntitlementUsageEvent] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Flux<EntitlementUsageEventResponse> getByEntitlement(String requestId, UUID entitlementId) {
        if (entitlementId == null)
            return Flux.error(BookingException.of(BookingErrorType.ENTITLEMENT_NOT_FOUND));
        return repository.findByEntitlementId(entitlementId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getEntitlementUsageEventsByEntitlement] requestId={} entitlementId={}", requestId, entitlementId))
                .doOnError(e -> log.error("[getEntitlementUsageEventsByEntitlement] Failed requestId={} entitlementId={} error={}", requestId, entitlementId, e.getMessage(), e));
    }

    private EntitlementUsageEventEntity mapRow(io.r2dbc.spi.Row row) {
        String eventTypeRaw = row.get("event_type", String.class);
        return EntitlementUsageEventEntity.builder()
                .id(row.get("id", UUID.class))
                .entitlementId(row.get("entitlement_id", UUID.class))
                .eventType(eventTypeRaw != null ? UsageEventType.valueOf(eventTypeRaw) : null)
                .usageDelta(row.get("usage_delta", Integer.class))
                .eventAt(row.get("event_at", OffsetDateTime.class))
                .locationCode(row.get("location_code", String.class))
                .referenceCode(row.get("reference_code", String.class))
                .payloadJson(row.get("payload_json", String.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .build();
    }

    private EntitlementUsageEventResponse toResponse(EntitlementUsageEventEntity e) {
        EntitlementUsageEventResponse r = new EntitlementUsageEventResponse();
        r.setId(e.getId());
        r.setEntitlementId(e.getEntitlementId());
        r.setEventType(e.getEventType());
        r.setUsageDelta(e.getUsageDelta());
        r.setEventAt(e.getEventAt());
        r.setLocationCode(e.getLocationCode());
        r.setReferenceCode(e.getReferenceCode());
        r.setPayloadJson(e.getPayloadJson());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }
}
