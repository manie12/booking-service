package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.orderitem.OrderItemRequest;
import io.booking.booking_service.dto.pojo.orderitem.OrderItemResponse;
import io.booking.booking_service.dto.pojo.orderitem.OrderItemUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderItemService {
    Mono<OrderItemResponse> create(String requestId, OrderItemRequest request);
    Mono<OrderItemResponse> update(String requestId, UUID id, OrderItemUpdate request);
    Mono<OrderItemResponse> get(String requestId, UUID id);
    Flux<OrderItemResponse> getByOrder(String requestId, UUID orderId);
    Flux<OrderItemResponse> getByProduct(String requestId, UUID productId);
    Mono<Void> cancel(String requestId, UUID id);
}
