package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.orderitemguest.OrderItemGuestRequest;
import io.booking.booking_service.dto.pojo.orderitemguest.OrderItemGuestResponse;
import io.booking.booking_service.dto.pojo.orderitemguest.OrderItemGuestUpdate;
import io.booking.booking_service.service.OrderItemGuestService;
import io.booking.booking_service.util.validators.OrderItemGuest;
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
@RequestMapping(path = "/api/v1/order-item-guests", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderItemGuestController {

    private final OrderItemGuest validator;
    private final OrderItemGuestService service;

    public OrderItemGuestController(OrderItemGuest validator, OrderItemGuestService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<OrderItemGuestResponse>>> create(
            @Valid @RequestBody OrderItemGuestRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createOrderItemGuest] requestId={} orderItemId={}", requestId, request.getOrderItemId());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<OrderItemGuestResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody OrderItemGuestUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateOrderItemGuest] requestId={} id={}", requestId, id);
        return service.update(requestId, id, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<OrderItemGuestResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getOrderItemGuest] requestId={} id={}", requestId, id);
        return service.get(requestId, id)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/order-item/{orderItemId}")
    public Mono<ResponseEntity<HttpResponse<Flux<OrderItemGuestResponse>>>> getByOrderItem(
            @PathVariable UUID orderItemId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getOrderItemGuestsByOrderItem] requestId={} orderItemId={}", requestId, orderItemId);
        Flux<OrderItemGuestResponse> records = service.getByOrderItem(requestId, orderItemId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @GetMapping(path = "/guest-profile/{guestProfileId}")
    public Mono<ResponseEntity<HttpResponse<Flux<OrderItemGuestResponse>>>> getByGuestProfile(
            @PathVariable UUID guestProfileId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getOrderItemGuestsByGuestProfile] requestId={} guestProfileId={}", requestId, guestProfileId);
        Flux<OrderItemGuestResponse> records = service.getByGuestProfile(requestId, guestProfileId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @DeleteMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<Void>>> delete(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[deleteOrderItemGuest] requestId={} id={}", requestId, id);
        return service.delete(requestId, id)
                .thenReturn(ResponseEntity.ok(ResponseFactory.<Void>ok(requestId)));
    }
}
