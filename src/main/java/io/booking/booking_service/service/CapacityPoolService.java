package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.capacitypool.CapacityPoolRequest;
import io.booking.booking_service.dto.pojo.capacitypool.CapacityPoolResponse;
import io.booking.booking_service.dto.pojo.capacitypool.CapacityPoolUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CapacityPoolService {
    Mono<CapacityPoolResponse> create(String requestId, CapacityPoolRequest request);
    Mono<CapacityPoolResponse> update(String requestId, UUID id, CapacityPoolUpdate request);
    Mono<CapacityPoolResponse> get(String requestId, UUID id);
    Flux<CapacityPoolResponse> getByScheduleInstance(String requestId, UUID scheduleInstanceId);
}
