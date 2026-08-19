package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.orderstatushistory.OrderStatusHistoryRequest;
import io.booking.booking_service.dto.pojo.orderstatushistory.OrderStatusHistoryResponse;
import io.booking.booking_service.service.OrderStatusHistoryService;
import io.booking.booking_service.util.validators.OrderStatusHistory;
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
@RequestMapping(path = "/api/v1/order-status-history", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderStatusHistoryController {

    private final OrderStatusHistory validator;
    private final OrderStatusHistoryService service;

    public OrderStatusHistoryController(OrderStatusHistory validator, OrderStatusHistoryService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<OrderStatusHistoryResponse>>> create(
            @Valid @RequestBody OrderStatusHistoryRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createOrderStatusHistory] requestId={} orderId={}", requestId, request.getOrderId());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<OrderStatusHistoryResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getOrderStatusHistory] requestId={} id={}", requestId, id);
        return service.get(requestId, id)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/order/{orderId}")
    public Mono<ResponseEntity<HttpResponse<Flux<OrderStatusHistoryResponse>>>> getByOrder(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getOrderStatusHistoryByOrder] requestId={} orderId={}", requestId, orderId);
        Flux<OrderStatusHistoryResponse> records = service.getByOrder(requestId, orderId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }
}
