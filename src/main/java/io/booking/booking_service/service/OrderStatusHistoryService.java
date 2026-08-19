package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.orderstatushistory.OrderStatusHistoryRequest;
import io.booking.booking_service.dto.pojo.orderstatushistory.OrderStatusHistoryResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderStatusHistoryService {
    Mono<OrderStatusHistoryResponse> create(String requestId, OrderStatusHistoryRequest request);
    Mono<OrderStatusHistoryResponse> get(String requestId, UUID id);
    Flux<OrderStatusHistoryResponse> getByOrder(String requestId, UUID orderId);
}
