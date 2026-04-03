package io.booking.booking_service.repository;

import io.booking.booking_service.model.OrderPriceComponentEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface OrderPriceComponentRepository extends ReactiveCrudRepository<OrderPriceComponentEntity, UUID> {
    Flux<OrderPriceComponentEntity> findByOrderItemId(UUID orderItemId);
}
