package io.booking.booking_service.repository;

import io.booking.booking_service.model.CartEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CartRepository extends ReactiveCrudRepository<CartEntity, UUID> {
    Mono<CartEntity> findByCartNumber(String cartNumber);
    Flux<CartEntity> findByCustomerId(UUID customerId);
    Mono<CartEntity> findBySessionId(String sessionId);
}
