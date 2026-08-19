package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.scheduleinstance.ScheduleInstanceRequest;
import io.booking.booking_service.dto.pojo.scheduleinstance.ScheduleInstanceResponse;
import io.booking.booking_service.dto.pojo.scheduleinstance.ScheduleInstanceUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ScheduleInstanceService {
    Mono<ScheduleInstanceResponse> create(String requestId, ScheduleInstanceRequest request);
    Mono<ScheduleInstanceResponse> update(String requestId, UUID id, ScheduleInstanceUpdate request);
    Mono<ScheduleInstanceResponse> get(String requestId, UUID id);
    Flux<ScheduleInstanceResponse> getByProduct(String requestId, UUID productId);
    Flux<ScheduleInstanceResponse> getByProductVariant(String requestId, UUID productVariantId);
}
