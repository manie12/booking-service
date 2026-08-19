package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.orderitem.OrderItemRequest;
import io.booking.booking_service.dto.pojo.orderitem.OrderItemResponse;
import io.booking.booking_service.dto.pojo.orderitem.OrderItemUpdate;
import io.booking.booking_service.service.OrderItemService;
import io.booking.booking_service.util.validators.OrderItem;
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
@RequestMapping(path = "/api/v1/order-items", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderItemController {

    private final OrderItem validator;
    private final OrderItemService service;

    public OrderItemController(OrderItem validator, OrderItemService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<OrderItemResponse>>> create(
            @Valid @RequestBody OrderItemRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createOrderItem] requestId={} orderId={}", requestId, request.getOrderId());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<OrderItemResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody OrderItemUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateOrderItem] requestId={} id={}", requestId, id);
        return service.update(requestId, id, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<OrderItemResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getOrderItem] requestId={} id={}", requestId, id);
        return service.get(requestId, id)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/order/{orderId}")
    public Mono<ResponseEntity<HttpResponse<Flux<OrderItemResponse>>>> getByOrder(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getOrderItemsByOrder] requestId={} orderId={}", requestId, orderId);
        Flux<OrderItemResponse> records = service.getByOrder(requestId, orderId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @GetMapping(path = "/product/{productId}")
    public Mono<ResponseEntity<HttpResponse<Flux<OrderItemResponse>>>> getByProduct(
            @PathVariable UUID productId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getOrderItemsByProduct] requestId={} productId={}", requestId, productId);
        Flux<OrderItemResponse> records = service.getByProduct(requestId, productId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @PatchMapping(path = "/{id}/cancel")
    public Mono<ResponseEntity<HttpResponse<Void>>> cancel(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[cancelOrderItem] requestId={} id={}", requestId, id);
        return service.cancel(requestId, id)
                .thenReturn(ResponseEntity.ok(ResponseFactory.<Void>ok(requestId)));
    }
}
