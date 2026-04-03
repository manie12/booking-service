package io.booking.booking_service.repository;

import io.booking.booking_service.model.CancellationEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface CancellationRepository extends ReactiveCrudRepository<CancellationEntity, UUID> {
    Flux<CancellationEntity> findByOrderId(UUID orderId);
    Flux<CancellationEntity> findByBookingId(UUID bookingId);
    Flux<CancellationEntity> findByCustomerId(UUID customerId);
}
