package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.checkin.CheckInRequest;
import io.booking.booking_service.dto.pojo.checkin.CheckInResponse;
import io.booking.booking_service.dto.pojo.checkin.CheckInUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CheckInService {
    Mono<CheckInResponse> create(String requestId, CheckInRequest request);
    Mono<CheckInResponse> update(String requestId, UUID id, CheckInUpdate request);
    Mono<CheckInResponse> get(String requestId, UUID id);
    Flux<CheckInResponse> getByEntitlement(String requestId, UUID entitlementId);
    Flux<CheckInResponse> getByBooking(String requestId, UUID bookingId);
    Flux<CheckInResponse> getByGuestProfile(String requestId, UUID guestProfileId);
}
