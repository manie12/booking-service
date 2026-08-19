package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.cartitem.CartItemRequest;
import io.booking.booking_service.dto.pojo.cartitem.CartItemResponse;
import io.booking.booking_service.dto.pojo.cartitem.CartItemUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CartItemService {
    Mono<CartItemResponse> create(String requestId, CartItemRequest request);
    Mono<CartItemResponse> update(String requestId, UUID id, CartItemUpdate request);
    Mono<CartItemResponse> get(String requestId, UUID id);
    Flux<CartItemResponse> getByCart(String requestId, UUID cartId);
    Flux<CartItemResponse> getByProduct(String requestId, UUID productId);
    Flux<CartItemResponse> getByScheduleInstance(String requestId, UUID scheduleInstanceId);
    Mono<Void> delete(String requestId, UUID id);
}
