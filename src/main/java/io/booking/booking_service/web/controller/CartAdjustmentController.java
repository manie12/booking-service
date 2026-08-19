package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.cartadjustment.CartAdjustmentRequest;
import io.booking.booking_service.dto.pojo.cartadjustment.CartAdjustmentResponse;
import io.booking.booking_service.dto.pojo.cartadjustment.CartAdjustmentUpdate;
import io.booking.booking_service.service.CartAdjustmentService;
import io.booking.booking_service.util.validators.CartAdjustment;
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
@RequestMapping(path = "/api/v1/cart-adjustments", produces = MediaType.APPLICATION_JSON_VALUE)
public class CartAdjustmentController {

    private final CartAdjustment validator;
    private final CartAdjustmentService service;

    public CartAdjustmentController(CartAdjustment validator, CartAdjustmentService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<CartAdjustmentResponse>>> create(
            @Valid @RequestBody CartAdjustmentRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createCartAdjustment] requestId={} cartId={}", requestId, request.getCartId());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<CartAdjustmentResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CartAdjustmentUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateCartAdjustment] requestId={} id={}", requestId, id);
        return service.update(requestId, id, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<CartAdjustmentResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCartAdjustment] requestId={} id={}", requestId, id);
        return service.get(requestId, id)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/cart/{cartId}")
    public Mono<ResponseEntity<HttpResponse<Flux<CartAdjustmentResponse>>>> getByCart(
            @PathVariable UUID cartId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCartAdjustmentsByCart] requestId={} cartId={}", requestId, cartId);
        Flux<CartAdjustmentResponse> records = service.getByCart(requestId, cartId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @GetMapping(path = "/cart-item/{cartItemId}")
    public Mono<ResponseEntity<HttpResponse<Flux<CartAdjustmentResponse>>>> getByCartItem(
            @PathVariable UUID cartItemId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCartAdjustmentsByCartItem] requestId={} cartItemId={}", requestId, cartItemId);
        Flux<CartAdjustmentResponse> records = service.getByCartItem(requestId, cartItemId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @DeleteMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<Void>>> delete(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[deleteCartAdjustment] requestId={} id={}", requestId, id);
        return service.delete(requestId, id)
                .thenReturn(ResponseEntity.ok(ResponseFactory.<Void>ok(requestId)));
    }
}
