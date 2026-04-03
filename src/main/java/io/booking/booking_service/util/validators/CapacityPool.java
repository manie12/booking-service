package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.CapacityPoolStatus;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.CapacityPoolEntity;
import io.booking.booking_service.repository.CapacityPoolRepository;
import io.booking.booking_service.util.SharedUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CapacityPool {

    private final SharedUtils sharedUtils;
    private final CapacityPoolRepository repository;

    /** Load capacity pool by id, fail with NOT_FOUND if missing */
    public Mono<CapacityPoolEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.CAPACITY_POOL_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.CAPACITY_POOL_NOT_FOUND)));
    }

    /** Ensure pool code is unique; pass ignoreId=null on create */
    public Mono<Void> ensureUniqueCode(String poolCode, UUID ignoreId) {
        if (sharedUtils.isNullOrEmptyOrBlank(poolCode))
            return Mono.error(BookingException.of(BookingErrorType.CAPACITY_POOL_REQUEST_INVALID));
        return repository.findByPoolCode(poolCode).flatMap(e -> {
            if (ignoreId != null && ignoreId.equals(e.getId())) return Mono.empty();
            return Mono.error(BookingException.of(BookingErrorType.CAPACITY_POOL_ALREADY_EXISTS));
        }).then();
    }

    /** Validate total capacity is >= 1 */
    public Mono<Void> validateTotalCapacity(Integer capacityTotal) {
        if (capacityTotal == null || capacityTotal < 1)
            return Mono.error(BookingException.of(BookingErrorType.CAPACITY_POOL_TOTAL_INVALID));
        return Mono.empty();
    }

    /** Validate schedule instance is linked */
    public Mono<Void> validateScheduleInstanceRequired(UUID scheduleInstanceId) {
        if (scheduleInstanceId == null)
            return Mono.error(BookingException.of(BookingErrorType.SCHEDULE_INSTANCE_NOT_FOUND));
        return Mono.empty();
    }

    /** Validate status is not null */
    public Mono<Void> validateStatusRequired(CapacityPoolStatus status) {
        if (status == null)
            return Mono.error(BookingException.of(BookingErrorType.CAPACITY_POOL_STATUS_INVALID));
        return Mono.empty();
    }

    /** Assert pool has sufficient available capacity for the requested quantity */
    public Mono<CapacityPoolEntity> assertSufficientCapacity(CapacityPoolEntity pool, int requested) {
        int available = pool.getCapacityAvailable() != null ? pool.getCapacityAvailable() : 0;
        if (available < requested)
            return Mono.error(BookingException.of(BookingErrorType.CAPACITY_POOL_INSUFFICIENT));
        return Mono.just(pool);
    }

    /** Assert pool is not closed */
    public Mono<CapacityPoolEntity> assertNotClosed(CapacityPoolEntity pool) {
        if (CapacityPoolStatus.CLOSED.equals(pool.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.CAPACITY_POOL_CLOSED));
        return Mono.just(pool);
    }

    /** Assert pool is not inactive */
    public Mono<CapacityPoolEntity> assertNotInactive(CapacityPoolEntity pool) {
        if (CapacityPoolStatus.INACTIVE.equals(pool.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.CAPACITY_POOL_STATUS_INVALID));
        return Mono.just(pool);
    }

    /** Load and assert pool is open and has capacity */
    public Mono<CapacityPoolEntity> loadAvailable(UUID id, int requested) {
        return load(id)
                .flatMap(this::assertNotInactive)
                .flatMap(this::assertNotClosed)
                .flatMap(p -> assertSufficientCapacity(p, requested));
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
