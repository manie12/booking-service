package io.booking.booking_service.repository;

import io.booking.booking_service.model.RescheduleEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface RescheduleRepository extends ReactiveCrudRepository<RescheduleEntity, UUID> {
    Flux<RescheduleEntity> findByBookingId(UUID bookingId);
    Flux<RescheduleEntity> findByBookingItemId(UUID bookingItemId);
}
