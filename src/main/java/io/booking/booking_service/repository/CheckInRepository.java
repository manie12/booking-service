package io.booking.booking_service.repository;

import io.booking.booking_service.model.CheckInEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface CheckInRepository extends ReactiveCrudRepository<CheckInEntity, UUID> {
    Flux<CheckInEntity> findByEntitlementId(UUID entitlementId);
    Flux<CheckInEntity> findByGuestProfileId(UUID guestProfileId);
    Flux<CheckInEntity> findByBookingId(UUID bookingId);
}
