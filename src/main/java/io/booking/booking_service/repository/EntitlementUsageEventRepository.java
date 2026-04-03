package io.booking.booking_service.repository;

import io.booking.booking_service.model.EntitlementUsageEventEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface EntitlementUsageEventRepository extends ReactiveCrudRepository<EntitlementUsageEventEntity, UUID> {
    Flux<EntitlementUsageEventEntity> findByEntitlementId(UUID entitlementId);
}
