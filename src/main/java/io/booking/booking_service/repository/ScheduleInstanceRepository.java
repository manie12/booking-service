package io.booking.booking_service.repository;

import io.booking.booking_service.model.ScheduleInstanceEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ScheduleInstanceRepository extends ReactiveCrudRepository<ScheduleInstanceEntity, UUID> {
    Mono<ScheduleInstanceEntity> findByInstanceCode(String instanceCode);
    Flux<ScheduleInstanceEntity> findByProductId(UUID productId);
    Flux<ScheduleInstanceEntity> findByProductVariantId(UUID productVariantId);
}
