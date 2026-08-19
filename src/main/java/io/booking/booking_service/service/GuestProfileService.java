package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.guestprofile.GuestProfileRequest;
import io.booking.booking_service.dto.pojo.guestprofile.GuestProfileResponse;
import io.booking.booking_service.dto.pojo.guestprofile.GuestProfileUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GuestProfileService {
    Mono<GuestProfileResponse> create(String requestId, GuestProfileRequest request);
    Mono<GuestProfileResponse> update(String requestId, UUID id, GuestProfileUpdate request);
    Mono<GuestProfileResponse> get(String requestId, UUID id);
    Flux<GuestProfileResponse> getByCustomer(String requestId, String customerId);
    Mono<Void> delete(String requestId, UUID id);
}
