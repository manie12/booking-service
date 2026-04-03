package io.booking.booking_service.repository;

import io.booking.booking_service.model.OrderStatusHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface OrderStatusHistoryRepository extends ReactiveCrudRepository<OrderStatusHistoryEntity, UUID> {
    Flux<OrderStatusHistoryEntity> findByOrderId(UUID orderId);
}
