package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.cart.CartRequest;
import io.booking.booking_service.dto.pojo.cart.CartResponse;
import io.booking.booking_service.dto.pojo.cart.CartUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CartService {
    Mono<CartResponse> create(String requestId, CartRequest request);
    Mono<CartResponse> update(String requestId, UUID id, CartUpdate request);
    Mono<CartResponse> get(String requestId, UUID id);
    Mono<CartResponse> getByCartNumber(String requestId, String cartNumber);
    Mono<CartResponse> getBySessionId(String requestId, String sessionId);
    Flux<CartResponse> getByCustomer(String requestId, UUID customerId);
    Mono<Void> expire(String requestId, UUID id);
    Mono<Void> abandon(String requestId, UUID id);
}
