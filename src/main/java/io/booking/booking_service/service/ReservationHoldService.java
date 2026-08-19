package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.reservationhold.ReservationHoldRequest;
import io.booking.booking_service.dto.pojo.reservationhold.ReservationHoldResponse;
import io.booking.booking_service.dto.pojo.reservationhold.ReservationHoldUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReservationHoldService {
    Mono<ReservationHoldResponse> create(String requestId, ReservationHoldRequest request);
    Mono<ReservationHoldResponse> update(String requestId, UUID id, ReservationHoldUpdate request);
    Mono<ReservationHoldResponse> get(String requestId, UUID id);
    Flux<ReservationHoldResponse> getByCart(String requestId, UUID cartId);
    Flux<ReservationHoldResponse> getByScheduleInstance(String requestId, UUID scheduleInstanceId);
}
