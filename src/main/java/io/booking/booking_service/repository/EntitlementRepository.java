package io.booking.booking_service.repository;

import io.booking.booking_service.model.EntitlementEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface EntitlementRepository extends ReactiveCrudRepository<EntitlementEntity, UUID> {
    Mono<EntitlementEntity> findByEntitlementNumber(String entitlementNumber);
    Flux<EntitlementEntity> findByOrderId(UUID orderId);
    Flux<EntitlementEntity> findByBookingId(UUID bookingId);
    Flux<EntitlementEntity> findByGuestProfileId(UUID guestProfileId);
}
