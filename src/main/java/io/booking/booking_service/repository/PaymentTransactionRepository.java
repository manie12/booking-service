package io.booking.booking_service.repository;

import io.booking.booking_service.model.PaymentTransactionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentTransactionRepository extends ReactiveCrudRepository<PaymentTransactionEntity, UUID> {
    Mono<PaymentTransactionEntity> findByTransactionReference(String transactionReference);
    Flux<PaymentTransactionEntity> findByPaymentId(UUID paymentId);
}
