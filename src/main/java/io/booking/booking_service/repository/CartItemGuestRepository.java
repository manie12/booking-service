package io.booking.booking_service.repository;

import io.booking.booking_service.model.CartItemGuestEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface CartItemGuestRepository extends ReactiveCrudRepository<CartItemGuestEntity, UUID> {
    Flux<CartItemGuestEntity> findByCartItemId(UUID cartItemId);
    Flux<CartItemGuestEntity> findByGuestProfileId(UUID guestProfileId);
}
