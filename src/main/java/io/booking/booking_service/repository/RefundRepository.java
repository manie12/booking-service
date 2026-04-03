package io.booking.booking_service.repository;

import io.booking.booking_service.model.RefundEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RefundRepository extends ReactiveCrudRepository<RefundEntity, UUID> {
    Mono<RefundEntity> findByRefundReference(String refundReference);
    Flux<RefundEntity> findByOrderId(UUID orderId);
    Flux<RefundEntity> findByPaymentId(UUID paymentId);
}
