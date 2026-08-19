package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.reschedule.RescheduleRequest;
import io.booking.booking_service.dto.pojo.reschedule.RescheduleResponse;
import io.booking.booking_service.dto.pojo.reschedule.RescheduleUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RescheduleService {
    Mono<RescheduleResponse> create(String requestId, RescheduleRequest request);
    Mono<RescheduleResponse> update(String requestId, UUID id, RescheduleUpdate request);
    Mono<RescheduleResponse> get(String requestId, UUID id);
    Flux<RescheduleResponse> getByBooking(String requestId, UUID bookingId);
    Flux<RescheduleResponse> getByBookingItem(String requestId, UUID bookingItemId);
}
