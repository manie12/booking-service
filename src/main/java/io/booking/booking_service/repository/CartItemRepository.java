package io.booking.booking_service.repository;

import io.booking.booking_service.model.CartItemEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface CartItemRepository extends ReactiveCrudRepository<CartItemEntity, UUID> {
    Flux<CartItemEntity> findByCartId(UUID cartId);
    Flux<CartItemEntity> findByProductId(UUID productId);
    Flux<CartItemEntity> findByScheduleInstanceId(UUID scheduleInstanceId);
}
