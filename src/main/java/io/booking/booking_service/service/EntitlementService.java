package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.entitlement.EntitlementRequest;
import io.booking.booking_service.dto.pojo.entitlement.EntitlementResponse;
import io.booking.booking_service.dto.pojo.entitlement.EntitlementUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface EntitlementService {
    Mono<EntitlementResponse> create(String requestId, EntitlementRequest request);
    Mono<EntitlementResponse> update(String requestId, UUID id, EntitlementUpdate request);
    Mono<EntitlementResponse> get(String requestId, UUID id);
    Mono<EntitlementResponse> getByEntitlementNumber(String requestId, String entitlementNumber);
    Flux<EntitlementResponse> getByOrder(String requestId, UUID orderId);
    Flux<EntitlementResponse> getByBooking(String requestId, UUID bookingId);
    Flux<EntitlementResponse> getByGuestProfile(String requestId, UUID guestProfileId);
    Mono<Void> revoke(String requestId, UUID id);
    Mono<Void> cancel(String requestId, UUID id);
}
