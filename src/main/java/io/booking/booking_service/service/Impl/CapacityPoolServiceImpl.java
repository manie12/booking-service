package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.CapacityPoolStatus;
import io.booking.booking_service.dto.pojo.capacitypool.CapacityPoolRequest;
import io.booking.booking_service.dto.pojo.capacitypool.CapacityPoolResponse;
import io.booking.booking_service.dto.pojo.capacitypool.CapacityPoolUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.CapacityPoolEntity;
import io.booking.booking_service.repository.CapacityPoolRepository;
import io.booking.booking_service.service.CapacityPoolService;
import io.booking.booking_service.util.validators.CapacityPool;
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
public class CapacityPoolServiceImpl implements CapacityPoolService {

    private final CapacityPoolRepository repository;
    private final ReactiveTx reactiveTx;
    private final CapacityPool validator;
    private final DatabaseClient db;

    public CapacityPoolServiceImpl(CapacityPoolRepository repository, ReactiveTx reactiveTx,
                                   CapacityPool validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<CapacityPoolResponse> create(String requestId, CapacityPoolRequest request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.CAPACITY_POOL_REQUEST_INVALID));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        CapacityPoolStatus status = request.getStatus() != null ? request.getStatus() : CapacityPoolStatus.ACTIVE;
        int held = request.getCapacityHeld() != null ? request.getCapacityHeld() : 0;
        int booked = request.getCapacityBooked() != null ? request.getCapacityBooked() : 0;
        int available = request.getCapacityAvailable() != null
                ? request.getCapacityAvailable()
                : request.getCapacityTotal() - held - booked;

        return validator.validateScheduleInstanceRequired(request.getScheduleInstanceId())
                .then(validator.validateTotalCapacity(request.getCapacityTotal()))
                .then(validator.ensureUniqueCode(request.getPoolCode(), null))
                .then(Mono.defer(() -> reactiveTx.required(() -> insert(request, status, held, booked, available, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createCapacityPool] requestId={} poolCode={}", requestId, request.getPoolCode()))
                .doOnSuccess(r -> log.info("[createCapacityPool] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createCapacityPool] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<CapacityPoolEntity> insert(CapacityPoolRequest r, CapacityPoolStatus status,
                                            int held, int booked, int available, OffsetDateTime now) {
        return db.sql("""
                INSERT INTO capacity_pools
                    (schedule_instance_id, pool_code, pool_name,
                     capacity_total, capacity_held, capacity_booked, capacity_available,
                     status, created_at, updated_at)
                VALUES
                    (:scheduleInstanceId, :poolCode, :poolName,
                     :capacityTotal, :capacityHeld, :capacityBooked, :capacityAvailable,
                     CAST(:status AS capacity_pool_status), :createdAt, :updatedAt)
                RETURNING *
                """)
                .bind("scheduleInstanceId", r.getScheduleInstanceId())
                .bind("poolCode", r.getPoolCode())
                .bind("poolName", r.getPoolName() != null ? r.getPoolName() : "")
                .bind("capacityTotal", r.getCapacityTotal())
                .bind("capacityHeld", held)
                .bind("capacityBooked", booked)
                .bind("capacityAvailable", available)
                .bind("status", status.name())
                .bind("createdAt", now)
                .bind("updatedAt", now)
                .map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<CapacityPoolResponse> update(String requestId, UUID id, CapacityPoolUpdate request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.CAPACITY_POOL_REQUEST_INVALID));
        return validator.load(id)
                .flatMap(validator::assertNotClosed)
                .flatMap(e -> {
                    Integer total = request.getCapacityTotal() != null ? request.getCapacityTotal() : e.getCapacityTotal();
                    return validator.validateTotalCapacity(total).thenReturn(e);
                })
                .flatMap(e -> Mono.defer(() -> reactiveTx.required(() -> updatePool(e, request))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateCapacityPool] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateCapacityPool] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<CapacityPoolEntity> updatePool(CapacityPoolEntity e, CapacityPoolUpdate r) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String poolName = r.getPoolName() != null ? r.getPoolName() : e.getPoolName() != null ? e.getPoolName() : "";
        Integer total = r.getCapacityTotal() != null ? r.getCapacityTotal() : e.getCapacityTotal();
        Integer held = r.getCapacityHeld() != null ? r.getCapacityHeld() : e.getCapacityHeld() != null ? e.getCapacityHeld() : 0;
        Integer booked = r.getCapacityBooked() != null ? r.getCapacityBooked() : e.getCapacityBooked() != null ? e.getCapacityBooked() : 0;
        Integer available = r.getCapacityAvailable() != null ? r.getCapacityAvailable() : total - held - booked;
        CapacityPoolStatus status = r.getStatus() != null ? r.getStatus() : e.getStatus();

        return db.sql("""
                UPDATE capacity_pools
                SET pool_name           = :poolName,
                    capacity_total      = :capacityTotal,
                    capacity_held       = :capacityHeld,
                    capacity_booked     = :capacityBooked,
                    capacity_available  = :capacityAvailable,
                    status              = CAST(:status AS capacity_pool_status),
                    updated_at          = :updatedAt
                WHERE id = :id
                RETURNING *
                """)
                .bind("poolName", poolName)
                .bind("capacityTotal", total)
                .bind("capacityHeld", held)
                .bind("capacityBooked", booked)
                .bind("capacityAvailable", available)
                .bind("status", status.name())
                .bind("updatedAt", now)
                .bind("id", e.getId())
                .map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<CapacityPoolResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCapacityPool] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getCapacityPool] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Flux<CapacityPoolResponse> getByScheduleInstance(String requestId, UUID scheduleInstanceId) {
        if (scheduleInstanceId == null)
            return Flux.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_NOT_FOUND));
        return repository.findByScheduleInstanceId(scheduleInstanceId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCapacityPoolsByScheduleInstance] requestId={} scheduleInstanceId={}", requestId, scheduleInstanceId))
                .doOnError(e -> log.error("[getCapacityPoolsByScheduleInstance] Failed requestId={} scheduleInstanceId={} error={}", requestId, scheduleInstanceId, e.getMessage(), e));
    }

    private CapacityPoolEntity mapRow(io.r2dbc.spi.Row row) {
        String statusRaw = row.get("status", String.class);
        return CapacityPoolEntity.builder()
                .id(row.get("id", UUID.class))
                .scheduleInstanceId(row.get("schedule_instance_id", UUID.class))
                .poolCode(row.get("pool_code", String.class))
                .poolName(row.get("pool_name", String.class))
                .capacityTotal(row.get("capacity_total", Integer.class))
                .capacityHeld(row.get("capacity_held", Integer.class))
                .capacityBooked(row.get("capacity_booked", Integer.class))
                .capacityAvailable(row.get("capacity_available", Integer.class))
                .status(statusRaw != null ? CapacityPoolStatus.valueOf(statusRaw) : null)
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .updatedAt(row.get("updated_at", OffsetDateTime.class))
                .build();
    }

    private CapacityPoolResponse toResponse(CapacityPoolEntity e) {
        CapacityPoolResponse r = new CapacityPoolResponse();
        r.setId(e.getId());
        r.setScheduleInstanceId(e.getScheduleInstanceId());
        r.setPoolCode(e.getPoolCode());
        r.setPoolName(e.getPoolName());
        r.setCapacityTotal(e.getCapacityTotal());
        r.setCapacityHeld(e.getCapacityHeld());
        r.setCapacityBooked(e.getCapacityBooked());
        r.setCapacityAvailable(e.getCapacityAvailable());
        r.setStatus(e.getStatus());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}
