package io.booking.booking_service.repository;

import io.booking.booking_service.model.CustomerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CustomerRepository extends ReactiveCrudRepository<CustomerEntity, UUID> {
    Mono<CustomerEntity> findByCustomerNumber(String customerNumber);
    Mono<CustomerEntity> findByEmail(String email);
}
