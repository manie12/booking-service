package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.ScheduleInstanceStatus;
import io.booking.booking_service.dto.pojo.scheduleinstance.ScheduleInstanceRequest;
import io.booking.booking_service.dto.pojo.scheduleinstance.ScheduleInstanceResponse;
import io.booking.booking_service.dto.pojo.scheduleinstance.ScheduleInstanceUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.ScheduleInstanceEntity;
import io.booking.booking_service.repository.ScheduleInstanceRepository;
import io.booking.booking_service.service.ScheduleInstanceService;
import io.booking.booking_service.util.validators.ScheduleInstance;
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
public class ScheduleInstanceServiceImpl implements ScheduleInstanceService {

    private final ScheduleInstanceRepository repository;
    private final ReactiveTx reactiveTx;
    private final ScheduleInstance validator;
    private final DatabaseClient db;

    public ScheduleInstanceServiceImpl(ScheduleInstanceRepository repository, ReactiveTx reactiveTx,
                                       ScheduleInstance validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<ScheduleInstanceResponse> create(String requestId, ScheduleInstanceRequest request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_REQUEST_INVALID));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        return validator.validateProductRequired(request.getProductId())
                .then(validator.validateTimezoneRequired(request.getTimezone()))
                .then(validator.validateTimeWindow(request.getStartAt(), request.getEndAt()))
                .then(validator.ensureUniqueCode(request.getInstanceCode(), null))
                .then(Mono.defer(() -> reactiveTx.required(() -> insert(request, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createScheduleInstance] requestId={} code={}", requestId, request.getInstanceCode()))
                .doOnSuccess(r -> log.info("[createScheduleInstance] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createScheduleInstance] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<ScheduleInstanceEntity> insert(ScheduleInstanceRequest r, OffsetDateTime now) {
        ScheduleInstanceStatus status = r.getStatus() != null ? r.getStatus() : ScheduleInstanceStatus.DRAFT;

        var spec = db.sql("""
                INSERT INTO schedule_instances
                    (tenant_id, product_id, product_variant_id,
                     instance_code, instance_name,
                     start_at, end_at, timezone,
                     venue_code, resource_reference,
                     status, created_at, updated_at)
                VALUES
                    (:tenantId, :productId, :productVariantId,
                     :instanceCode, :instanceName,
                     :startAt, :endAt, :timezone,
                     :venueCode, :resourceReference,
                     CAST(:status AS schedule_instance_status), :createdAt, :updatedAt)
                RETURNING *
                """)
                .bind("tenantId", r.getTenantId())
                .bind("productId", r.getProductId())
                .bind("instanceCode", r.getInstanceCode())
                .bind("instanceName", r.getInstanceName() != null ? r.getInstanceName() : "")
                .bind("startAt", r.getStartAt())
                .bind("endAt", r.getEndAt())
                .bind("timezone", r.getTimezone())
                .bind("venueCode", r.getVenueCode() != null ? r.getVenueCode() : "")
                .bind("resourceReference", r.getResourceReference() != null ? r.getResourceReference() : "")
                .bind("status", status.name())
                .bind("createdAt", now)
                .bind("updatedAt", now);

        if (r.getProductVariantId() != null) spec = spec.bind("productVariantId", r.getProductVariantId());
        else spec = spec.bindNull("productVariantId", UUID.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<ScheduleInstanceResponse> update(String requestId, UUID id, ScheduleInstanceUpdate request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_REQUEST_INVALID));
        return validator.load(id)
                .flatMap(validator::assertNotCancelled)
                .flatMap(e -> {
                    OffsetDateTime startAt = request.getStartAt() != null ? request.getStartAt() : e.getStartAt();
                    OffsetDateTime endAt = request.getEndAt() != null ? request.getEndAt() : e.getEndAt();
                    return validator.validateTimeWindow(startAt, endAt)
                            .then(reactiveTx.required(() -> updateInstance(e, request)));
                })
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateScheduleInstance] requestId={} id={}", requestId, id))
                .doOnSuccess(r -> log.info("[updateScheduleInstance] Success requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateScheduleInstance] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<ScheduleInstanceEntity> updateInstance(ScheduleInstanceEntity e, ScheduleInstanceUpdate r) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        ScheduleInstanceStatus status = r.getStatus() != null ? r.getStatus() : e.getStatus();

        return db.sql("""
                UPDATE schedule_instances
                SET instance_name      = :instanceName,
                    start_at           = :startAt,
                    end_at             = :endAt,
                    timezone           = :timezone,
                    venue_code         = :venueCode,
                    resource_reference = :resourceReference,
                    status             = CAST(:status AS schedule_instance_status),
                    updated_at         = :updatedAt
                WHERE id = :id
                RETURNING *
                """)
                .bind("instanceName", r.getInstanceName() != null ? r.getInstanceName() : (e.getInstanceName() != null ? e.getInstanceName() : ""))
                .bind("startAt", r.getStartAt() != null ? r.getStartAt() : e.getStartAt())
                .bind("endAt", r.getEndAt() != null ? r.getEndAt() : e.getEndAt())
                .bind("timezone", r.getTimezone() != null ? r.getTimezone() : e.getTimezone())
                .bind("venueCode", r.getVenueCode() != null ? r.getVenueCode() : (e.getVenueCode() != null ? e.getVenueCode() : ""))
                .bind("resourceReference", r.getResourceReference() != null ? r.getResourceReference() : (e.getResourceReference() != null ? e.getResourceReference() : ""))
                .bind("status", status.name())
                .bind("updatedAt", now)
                .bind("id", e.getId())
                .map((row, meta) -> mapRow(row))
                .one();
    }

    @Override
    public Mono<ScheduleInstanceResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getScheduleInstance] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getScheduleInstance] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Flux<ScheduleInstanceResponse> getByProduct(String requestId, UUID productId) {
        if (productId == null)
            return Flux.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_PRODUCT_REQUIRED));
        return repository.findByProductId(productId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getScheduleInstanceByProduct] requestId={} productId={}", requestId, productId))
                .doOnError(e -> log.error("[getScheduleInstanceByProduct] Failed requestId={} productId={} error={}", requestId, productId, e.getMessage(), e));
    }

    @Override
    public Flux<ScheduleInstanceResponse> getByProductVariant(String requestId, UUID productVariantId) {
        if (productVariantId == null)
            return Flux.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_REQUEST_INVALID));
        return repository.findByProductVariantId(productVariantId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getScheduleInstanceByProductVariant] requestId={} productVariantId={}", requestId, productVariantId))
                .doOnError(e -> log.error("[getScheduleInstanceByProductVariant] Failed requestId={} productVariantId={} error={}", requestId, productVariantId, e.getMessage(), e));
    }

    private ScheduleInstanceEntity mapRow(io.r2dbc.spi.Row row) {
        String statusRaw = row.get("status", String.class);
        return ScheduleInstanceEntity.builder()
                .id(row.get("id", UUID.class))
                .tenantId(row.get("tenant_id", UUID.class))
                .productId(row.get("product_id", UUID.class))
                .productVariantId(row.get("product_variant_id", UUID.class))
                .instanceCode(row.get("instance_code", String.class))
                .instanceName(row.get("instance_name", String.class))
                .startAt(row.get("start_at", OffsetDateTime.class))
                .endAt(row.get("end_at", OffsetDateTime.class))
                .timezone(row.get("timezone", String.class))
                .venueCode(row.get("venue_code", String.class))
                .resourceReference(row.get("resource_reference", String.class))
                .status(statusRaw != null ? ScheduleInstanceStatus.valueOf(statusRaw) : null)
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .updatedAt(row.get("updated_at", OffsetDateTime.class))
                .build();
    }

    private ScheduleInstanceResponse toResponse(ScheduleInstanceEntity e) {
        ScheduleInstanceResponse r = new ScheduleInstanceResponse();
        r.setId(e.getId());
        r.setTenantId(e.getTenantId());
        r.setProductId(e.getProductId());
        r.setProductVariantId(e.getProductVariantId());
        r.setInstanceCode(e.getInstanceCode());
        r.setInstanceName(e.getInstanceName());
        r.setStartAt(e.getStartAt());
        r.setEndAt(e.getEndAt());
        r.setTimezone(e.getTimezone());
        r.setVenueCode(e.getVenueCode());
        r.setResourceReference(e.getResourceReference());
        r.setStatus(e.getStatus());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}
