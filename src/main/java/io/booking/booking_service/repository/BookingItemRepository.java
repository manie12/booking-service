package io.booking.booking_service.repository;

import io.booking.booking_service.model.BookingItemEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface BookingItemRepository extends ReactiveCrudRepository<BookingItemEntity, UUID> {
    Flux<BookingItemEntity> findByBookingId(UUID bookingId);
    Flux<BookingItemEntity> findByScheduleInstanceId(UUID scheduleInstanceId);
    Flux<BookingItemEntity> findByOrderItemId(UUID orderItemId);
}
