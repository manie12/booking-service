package io.booking.booking_service.repository;

import io.booking.booking_service.model.OrderItemEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItemEntity, UUID> {
    Flux<OrderItemEntity> findByOrderId(UUID orderId);
    Flux<OrderItemEntity> findByProductId(UUID productId);
}
