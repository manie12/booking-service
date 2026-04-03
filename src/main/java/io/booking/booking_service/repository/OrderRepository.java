package io.booking.booking_service.repository;

import io.booking.booking_service.model.OrderEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderRepository extends ReactiveCrudRepository<OrderEntity, UUID> {
    Mono<OrderEntity> findByOrderNumber(String orderNumber);
    Mono<OrderEntity> findByIdempotencyKey(String idempotencyKey);
}
