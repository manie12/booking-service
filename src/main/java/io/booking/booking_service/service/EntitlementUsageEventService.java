package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.entitlementusageevent.EntitlementUsageEventRequest;
import io.booking.booking_service.dto.pojo.entitlementusageevent.EntitlementUsageEventResponse;
import io.booking.booking_service.dto.pojo.entitlementusageevent.EntitlementUsageEventUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface EntitlementUsageEventService {
    Mono<EntitlementUsageEventResponse> create(String requestId, EntitlementUsageEventRequest request);
    Mono<EntitlementUsageEventResponse> update(String requestId, UUID id, EntitlementUsageEventUpdate request);
    Mono<EntitlementUsageEventResponse> get(String requestId, UUID id);
    Flux<EntitlementUsageEventResponse> getByEntitlement(String requestId, UUID entitlementId);
}
