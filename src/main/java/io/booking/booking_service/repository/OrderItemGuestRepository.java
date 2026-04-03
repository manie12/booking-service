package io.booking.booking_service.repository;

import io.booking.booking_service.model.OrderItemGuestEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface OrderItemGuestRepository extends ReactiveCrudRepository<OrderItemGuestEntity, UUID> {
    Flux<OrderItemGuestEntity> findByOrderItemId(UUID orderItemId);
    Flux<OrderItemGuestEntity> findByGuestProfileId(UUID guestProfileId);
}
