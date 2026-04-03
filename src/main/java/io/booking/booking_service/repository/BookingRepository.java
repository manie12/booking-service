package io.booking.booking_service.repository;

import io.booking.booking_service.model.BookingEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BookingRepository extends ReactiveCrudRepository<BookingEntity, UUID> {
    Mono<BookingEntity> findByBookingNumber(String bookingNumber);
    Flux<BookingEntity> findByOrderId(UUID orderId);
    Flux<BookingEntity> findByCustomerId(UUID customerId);
}
