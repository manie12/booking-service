package io.booking.booking_service.repository;

import io.booking.booking_service.model.PaymentEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentRepository extends ReactiveCrudRepository<PaymentEntity, UUID> {
    Mono<PaymentEntity> findByPaymentReference(String paymentReference);
    Flux<PaymentEntity> findByOrderId(UUID orderId);
}
