package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.accesspass.AccessPassRequest;
import io.booking.booking_service.dto.pojo.accesspass.AccessPassResponse;
import io.booking.booking_service.dto.pojo.accesspass.AccessPassUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AccessPassService {
    Mono<AccessPassResponse> create(String requestId, AccessPassRequest request);
    Mono<AccessPassResponse> update(String requestId, UUID id, AccessPassUpdate request);
    Mono<AccessPassResponse> get(String requestId, UUID id);
    Flux<AccessPassResponse> getByEntitlement(String requestId, UUID entitlementId);
    Mono<AccessPassResponse> revoke(String requestId, UUID id);
}
