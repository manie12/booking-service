package io.booking.booking_service.repository;

import io.booking.booking_service.model.ReservationHoldEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReservationHoldRepository extends ReactiveCrudRepository<ReservationHoldEntity, UUID> {
    Mono<ReservationHoldEntity> findByHoldReference(String holdReference);
    Flux<ReservationHoldEntity> findByCartId(UUID cartId);
    Flux<ReservationHoldEntity> findByScheduleInstanceId(UUID scheduleInstanceId);
}
