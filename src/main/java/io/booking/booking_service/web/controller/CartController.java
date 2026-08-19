package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.cart.CartRequest;
import io.booking.booking_service.dto.pojo.cart.CartResponse;
import io.booking.booking_service.dto.pojo.cart.CartUpdate;
import io.booking.booking_service.service.CartService;
import io.booking.booking_service.util.validators.Cart;
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
@RequestMapping(path = "/api/v1/carts", produces = MediaType.APPLICATION_JSON_VALUE)
public class CartController {

    private final Cart validator;
    private final CartService cartService;

    public CartController(Cart validator, CartService cartService) {
        this.validator = validator;
        this.cartService = cartService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<CartResponse>>> create(
            @Valid @RequestBody CartRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createCart] requestId={} cartNumber={}", requestId, request.getCartNumber());
        return cartService.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<CartResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CartUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateCart] requestId={} id={}", requestId, id);
        return cartService.update(requestId, id, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<CartResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCart] requestId={} id={}", requestId, id);
        return cartService.get(requestId, id)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/number/{cartNumber}")
    public Mono<ResponseEntity<HttpResponse<CartResponse>>> getByCartNumber(
            @PathVariable String cartNumber,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCartByNumber] requestId={} cartNumber={}", requestId, cartNumber);
        return cartService.getByCartNumber(requestId, cartNumber)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/session/{sessionId}")
    public Mono<ResponseEntity<HttpResponse<CartResponse>>> getBySessionId(
            @PathVariable String sessionId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCartBySession] requestId={} sessionId={}", requestId, sessionId);
        return cartService.getBySessionId(requestId, sessionId)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/customer/{customerId}")
    public Mono<ResponseEntity<HttpResponse<Flux<CartResponse>>>> getByCustomer(
            @PathVariable UUID customerId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCartsByCustomer] requestId={} customerId={}", requestId, customerId);
        Flux<CartResponse> records = cartService.getByCustomer(requestId, customerId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @PatchMapping(path = "/{id}/expire")
    public Mono<ResponseEntity<HttpResponse<Void>>> expire(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[expireCart] requestId={} id={}", requestId, id);
        return cartService.expire(requestId, id)
                .thenReturn(ResponseEntity.ok(ResponseFactory.<Void>ok(requestId)));
    }

    @PatchMapping(path = "/{id}/abandon")
    public Mono<ResponseEntity<HttpResponse<Void>>> abandon(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[abandonCart] requestId={} id={}", requestId, id);
        return cartService.abandon(requestId, id)
                .thenReturn(ResponseEntity.ok(ResponseFactory.<Void>ok(requestId)));
    }
}
