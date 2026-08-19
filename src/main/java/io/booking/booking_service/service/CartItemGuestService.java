package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.cartitemguest.CartItemGuestRequest;
import io.booking.booking_service.dto.pojo.cartitemguest.CartItemGuestResponse;
import io.booking.booking_service.dto.pojo.cartitemguest.CartItemGuestUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CartItemGuestService {
    Mono<CartItemGuestResponse> create(String requestId, CartItemGuestRequest request);
    Mono<CartItemGuestResponse> update(String requestId, UUID id, CartItemGuestUpdate request);
    Mono<CartItemGuestResponse> get(String requestId, UUID id);
    Flux<CartItemGuestResponse> getByCartItem(String requestId, UUID cartItemId);
    Flux<CartItemGuestResponse> getByGuestProfile(String requestId, UUID guestProfileId);
    Mono<Void> delete(String requestId, UUID id);
}
