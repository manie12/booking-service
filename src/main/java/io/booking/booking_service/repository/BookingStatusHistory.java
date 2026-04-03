package io.booking.booking_service.repository;

import io.booking.booking_service.model.BookingStatusHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface BookingStatusHistory extends ReactiveCrudRepository<BookingStatusHistoryEntity, UUID> {
    Flux<BookingStatusHistoryEntity> findByBookingId(UUID bookingId);
    Flux<BookingStatusHistoryEntity> findByBookingItemId(UUID bookingItemId);
}
