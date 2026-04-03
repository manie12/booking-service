package io.booking.booking_service.repository;

import io.booking.booking_service.model.GuestProfileEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GuestProfileRepository extends ReactiveCrudRepository<GuestProfileEntity, UUID> {
    Mono<GuestProfileEntity> findByTenantIdAndDocumentTypeAndDocumentNumber(UUID tenantId, String documentType, String documentNumber);
    Flux<GuestProfileEntity> findByCustomerId(UUID customerId);
}
