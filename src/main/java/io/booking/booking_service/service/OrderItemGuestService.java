package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.orderitemguest.OrderItemGuestRequest;
import io.booking.booking_service.dto.pojo.orderitemguest.OrderItemGuestResponse;
import io.booking.booking_service.dto.pojo.orderitemguest.OrderItemGuestUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderItemGuestService {
    Mono<OrderItemGuestResponse> create(String requestId, OrderItemGuestRequest request);
    Mono<OrderItemGuestResponse> update(String requestId, UUID id, OrderItemGuestUpdate request);
    Mono<OrderItemGuestResponse> get(String requestId, UUID id);
    Flux<OrderItemGuestResponse> getByOrderItem(String requestId, UUID orderItemId);
    Flux<OrderItemGuestResponse> getByGuestProfile(String requestId, UUID guestProfileId);
    Mono<Void> delete(String requestId, UUID id);
}
