package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.cancellation.CancellationRequest;
import io.booking.booking_service.dto.pojo.cancellation.CancellationResponse;
import io.booking.booking_service.dto.pojo.cancellation.CancellationUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CancellationService {
    Mono<CancellationResponse> create(String requestId, CancellationRequest request);
    Mono<CancellationResponse> update(String requestId, UUID id, CancellationUpdate request);
    Mono<CancellationResponse> get(String requestId, UUID id);
    Flux<CancellationResponse> getByOrder(String requestId, UUID orderId);
    Flux<CancellationResponse> getByBooking(String requestId, UUID bookingId);
    Flux<CancellationResponse> getByCustomer(String requestId, UUID customerId);
}
