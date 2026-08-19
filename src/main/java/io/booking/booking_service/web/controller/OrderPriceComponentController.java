package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.orderpricecomponent.OrderPriceComponentRequest;
import io.booking.booking_service.dto.pojo.orderpricecomponent.OrderPriceComponentResponse;
import io.booking.booking_service.dto.pojo.orderpricecomponent.OrderPriceComponentUpdate;
import io.booking.booking_service.service.OrderPriceComponentService;
import io.booking.booking_service.util.validators.OrderPriceComponent;
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
@RequestMapping(path = "/api/v1/order-price-components", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderPriceComponentController {

    private final OrderPriceComponent validator;
    private final OrderPriceComponentService service;

    public OrderPriceComponentController(OrderPriceComponent validator, OrderPriceComponentService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<OrderPriceComponentResponse>>> create(
            @Valid @RequestBody OrderPriceComponentRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createOrderPriceComponent] requestId={} orderItemId={}", requestId, request.getOrderItemId());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<OrderPriceComponentResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody OrderPriceComponentUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateOrderPriceComponent] requestId={} id={}", requestId, id);
        return service.update(requestId, id, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<OrderPriceComponentResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getOrderPriceComponent] requestId={} id={}", requestId, id);
        return service.get(requestId, id)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/order-item/{orderItemId}")
    public Mono<ResponseEntity<HttpResponse<Flux<OrderPriceComponentResponse>>>> getByOrderItem(
            @PathVariable UUID orderItemId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getOrderPriceComponentsByOrderItem] requestId={} orderItemId={}", requestId, orderItemId);
        Flux<OrderPriceComponentResponse> records = service.getByOrderItem(requestId, orderItemId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @DeleteMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<Void>>> delete(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[deleteOrderPriceComponent] requestId={} id={}", requestId, id);
        return service.delete(requestId, id)
                .thenReturn(ResponseEntity.ok(ResponseFactory.<Void>ok(requestId)));
    }
}
