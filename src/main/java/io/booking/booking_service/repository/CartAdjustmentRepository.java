package io.booking.booking_service.repository;

import io.booking.booking_service.model.CartAdjustmentEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface CartAdjustmentRepository extends ReactiveCrudRepository<CartAdjustmentEntity, UUID> {
    Flux<CartAdjustmentEntity> findByCartId(UUID cartId);
    Flux<CartAdjustmentEntity> findByCartItemId(UUID cartItemId);
}
