package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.cartadjustment.CartAdjustmentRequest;
import io.booking.booking_service.dto.pojo.cartadjustment.CartAdjustmentResponse;
import io.booking.booking_service.dto.pojo.cartadjustment.CartAdjustmentUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CartAdjustmentService {
    Mono<CartAdjustmentResponse> create(String requestId, CartAdjustmentRequest request);
    Mono<CartAdjustmentResponse> update(String requestId, UUID id, CartAdjustmentUpdate request);
    Mono<CartAdjustmentResponse> get(String requestId, UUID id);
    Flux<CartAdjustmentResponse> getByCart(String requestId, UUID cartId);
    Flux<CartAdjustmentResponse> getByCartItem(String requestId, UUID cartItemId);
    Mono<Void> delete(String requestId, UUID id);
}
