package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.cartitem.CartItemRequest;
import io.booking.booking_service.dto.pojo.cartitem.CartItemResponse;
import io.booking.booking_service.dto.pojo.cartitem.CartItemUpdate;
import io.booking.booking_service.service.CartItemService;
import io.booking.booking_service.util.validators.CartItem;
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
@RequestMapping(path = "/api/v1/cart-items", produces = MediaType.APPLICATION_JSON_VALUE)
public class CartItemController {

    private final CartItem validator;
    private final CartItemService service;

    public CartItemController(CartItem validator, CartItemService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<CartItemResponse>>> create(
            @Valid @RequestBody CartItemRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createCartItem] requxestId={}", requestId);
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<CartItemResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CartItemUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateCartItem] requestId={} id={}", requestId, id);
        return service.update(requestId, id, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<CartItemResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCartItem] requestId={} id={}", requestId, id);
        return service.get(requestId, id)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/cart/{cartId}")
    public Mono<ResponseEntity<HttpResponse<Flux<CartItemResponse>>>> getByCart(
            @PathVariable UUID cartId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCartItemsByCart] requestId={} cartId={}", requestId, cartId);
        Flux<CartItemResponse> records = service.getByCart(requestId, cartId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @GetMapping(path = "/product/{productId}")
    public Mono<ResponseEntity<HttpResponse<Flux<CartItemResponse>>>> getByProduct(
            @PathVariable UUID productId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCartItemsByProduct] requestId={} productId={}", requestId, productId);
        Flux<CartItemResponse> records = service.getByProduct(requestId, productId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @GetMapping(path = "/schedule-instance/{scheduleInstanceId}")
    public Mono<ResponseEntity<HttpResponse<Flux<CartItemResponse>>>> getByScheduleInstance(
            @PathVariable UUID scheduleInstanceId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCartItemsByScheduleInstance] requestId={} scheduleInstanceId={}", requestId, scheduleInstanceId);
        Flux<CartItemResponse> records = service.getByScheduleInstance(requestId, scheduleInstanceId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @DeleteMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<Void>>> delete(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[deleteCartItem] requestId={} id={}", requestId, id);
        return service.delete(requestId, id)
                .thenReturn(ResponseEntity.ok(ResponseFactory.<Void>ok(requestId)));
    }
}
