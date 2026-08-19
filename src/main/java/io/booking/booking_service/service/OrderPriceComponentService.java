package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.orderpricecomponent.OrderPriceComponentRequest;
import io.booking.booking_service.dto.pojo.orderpricecomponent.OrderPriceComponentResponse;
import io.booking.booking_service.dto.pojo.orderpricecomponent.OrderPriceComponentUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderPriceComponentService {
    Mono<OrderPriceComponentResponse> create(String requestId, OrderPriceComponentRequest request);
    Mono<OrderPriceComponentResponse> update(String requestId, UUID id, OrderPriceComponentUpdate request);
    Mono<OrderPriceComponentResponse> get(String requestId, UUID id);
    Flux<OrderPriceComponentResponse> getByOrderItem(String requestId, UUID orderItemId);
    Mono<Void> delete(String requestId, UUID id);
}
