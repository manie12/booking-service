package io.booking.booking_service.repository;

import io.booking.booking_service.model.BookingGuestEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface BookingGuestRepository extends ReactiveCrudRepository<BookingGuestEntity, UUID> {
    Flux<BookingGuestEntity> findByBookingItemId(UUID bookingItemId);
    Flux<BookingGuestEntity> findByGuestProfileId(UUID guestProfileId);
}
