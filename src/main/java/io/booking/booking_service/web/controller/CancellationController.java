package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.cancellation.CancellationRequest;
import io.booking.booking_service.dto.pojo.cancellation.CancellationResponse;
import io.booking.booking_service.dto.pojo.cancellation.CancellationUpdate;
import io.booking.booking_service.service.CancellationService;
import io.booking.booking_service.util.validators.Cancellation;
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
@RequestMapping(path = "/api/v1/cancellations", produces = MediaType.APPLICATION_JSON_VALUE)
public class CancellationController {

    private final Cancellation validator;
    private final CancellationService service;

    public CancellationController(Cancellation validator, CancellationService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<CancellationResponse>>> create(
            @Valid @RequestBody CancellationRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createCancellation] requestId={} orderId={}", requestId, request.getOrderId());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<CancellationResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CancellationUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateCancellation] requestId={} id={}", requestId, id);
        return service.update(requestId, id, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<CancellationResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCancellation] requestId={} id={}", requestId, id);
        return service.get(requestId, id)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/order/{orderId}")
    public Mono<ResponseEntity<HttpResponse<Flux<CancellationResponse>>>> getByOrder(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCancellationsByOrder] requestId={} orderId={}", requestId, orderId);
        Flux<CancellationResponse> records = service.getByOrder(requestId, orderId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @GetMapping(path = "/booking/{bookingId}")
    public Mono<ResponseEntity<HttpResponse<Flux<CancellationResponse>>>> getByBooking(
            @PathVariable UUID bookingId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCancellationsByBooking] requestId={} bookingId={}", requestId, bookingId);
        Flux<CancellationResponse> records = service.getByBooking(requestId, bookingId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @GetMapping(path = "/customer/{customerId}")
    public Mono<ResponseEntity<HttpResponse<Flux<CancellationResponse>>>> getByCustomer(
            @PathVariable UUID customerId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCancellationsByCustomer] requestId={} customerId={}", requestId, customerId);
        Flux<CancellationResponse> records = service.getByCustomer(requestId, customerId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }
}
