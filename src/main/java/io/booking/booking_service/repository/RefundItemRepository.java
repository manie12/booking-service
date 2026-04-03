package io.booking.booking_service.repository;

import io.booking.booking_service.model.RefundItemEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface RefundItemRepository extends ReactiveCrudRepository<RefundItemEntity, UUID> {
    Flux<RefundItemEntity> findByRefundId(UUID refundId);
    Flux<RefundItemEntity> findByOrderItemId(UUID orderItemId);
}
