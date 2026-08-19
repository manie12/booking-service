package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.reschedule.RescheduleRequest;
import io.booking.booking_service.dto.pojo.reschedule.RescheduleResponse;
import io.booking.booking_service.dto.pojo.reschedule.RescheduleUpdate;
import io.booking.booking_service.service.RescheduleService;
import io.booking.booking_service.util.validators.Reschedule;
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
@RequestMapping(path = "/api/v1/reschedules", produces = MediaType.APPLICATION_JSON_VALUE)
public class RescheduleController {

    private final Reschedule validator;
    private final RescheduleService service;

    public RescheduleController(Reschedule validator, RescheduleService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<RescheduleResponse>>> create(
            @Valid @RequestBody RescheduleRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createReschedule] requestId={} bookingId={}", requestId, request.getBookingId());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<RescheduleResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateReschedule] requestId={} id={}", requestId, id);
        return service.update(requestId, id, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<RescheduleResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getReschedule] requestId={} id={}", requestId, id);
        return service.get(requestId, id)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/booking/{bookingId}")
    public Mono<ResponseEntity<HttpResponse<Flux<RescheduleResponse>>>> getByBooking(
            @PathVariable UUID bookingId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getRescheduleByBooking] requestId={} bookingId={}", requestId, bookingId);
        Flux<RescheduleResponse> records = service.getByBooking(requestId, bookingId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @GetMapping(path = "/booking-item/{bookingItemId}")
    public Mono<ResponseEntity<HttpResponse<Flux<RescheduleResponse>>>> getByBookingItem(
            @PathVariable UUID bookingItemId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getRescheduleByBookingItem] requestId={} bookingItemId={}", requestId, bookingItemId);
        Flux<RescheduleResponse> records = service.getByBookingItem(requestId, bookingItemId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }
}
