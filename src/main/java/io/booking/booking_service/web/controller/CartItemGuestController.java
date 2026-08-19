package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.cartitemguest.CartItemGuestRequest;
import io.booking.booking_service.dto.pojo.cartitemguest.CartItemGuestResponse;
import io.booking.booking_service.dto.pojo.cartitemguest.CartItemGuestUpdate;
import io.booking.booking_service.service.CartItemGuestService;
import io.booking.booking_service.util.validators.CartItemGuest;
import io.booking.booking_service.web.http.HttpResponse;
import io.booking.booking_service.web.http.ResponseFactory;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/cart-item-guests", produces = MediaType.APPLICATION_JSON_VALUE)
public class CartItemGuestController {

    private final CartItemGuest validator;
    private final CartItemGuestService service;

    public CartItemGuestController(CartItemGuest validator, CartItemGuestService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<CartItemGuestResponse>>> create(
            @Valid @RequestBody CartItemGuestRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createCartItemGuest] requestId={} cartItemId={}", requestId, request.getCartItemId());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<CartItemGuestResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CartItemGuestUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateCartItemGuest] requestId={} id={}", requestId, id);
        return service.update(requestId, id, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<CartItemGuestResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCartItemGuest] requestId={} id={}", requestId, id);
        return service.get(requestId, id)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/cart-item/{cartItemId}")
    public Mono<ResponseEntity<HttpResponse<Flux<CartItemGuestResponse>>>> getByCartItem(
            @PathVariable String cartItemId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCartItemGuestsByCartItem] requestId={} cartItemId={}", requestId, cartItemId);
        Flux<CartItemGuestResponse> records = service.getByCartItem(requestId, UUID.fromString(cartItemId));
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @GetMapping(path = "/guest-profile/{guestProfileId}")
    public Mono<ResponseEntity<HttpResponse<Flux<CartItemGuestResponse>>>> getByGuestProfile(
            @PathVariable UUID guestProfileId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCartItemGuestsByGuestProfile] requestId={} guestProfileId={}", requestId, guestProfileId);
        Flux<CartItemGuestResponse> records = service.getByGuestProfile(requestId, guestProfileId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @DeleteMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<Void>>> delete(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[deleteCartItemGuest] requestId={} id={}", requestId, id);
        return service.delete(requestId, id)
                .thenReturn(ResponseEntity.ok(ResponseFactory.<Void>ok(requestId)));
    }
}
