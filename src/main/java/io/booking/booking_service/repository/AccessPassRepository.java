package io.booking.booking_service.repository;

import io.booking.booking_service.model.AccessPassEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AccessPassRepository extends ReactiveCrudRepository<AccessPassEntity, UUID> {
    Mono<AccessPassEntity> findByPassNumber(String passNumber);
    Mono<AccessPassEntity> findByBarcodeValue(String barcodeValue);
    Mono<AccessPassEntity> findByQrToken(String qrToken);
    Flux<AccessPassEntity> findByEntitlementId(UUID entitlementId);
}
