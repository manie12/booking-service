package io.booking.booking_service.repository;

import io.booking.booking_service.model.CapacityPoolEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CapacityPoolRepository extends ReactiveCrudRepository<CapacityPoolEntity, UUID> {
    Mono<CapacityPoolEntity> findByPoolCode(String poolCode);
    Flux<CapacityPoolEntity> findByScheduleInstanceId(UUID scheduleInstanceId);
}
