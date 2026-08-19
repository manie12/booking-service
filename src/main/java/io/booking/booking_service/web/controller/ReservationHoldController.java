package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.reservationhold.ReservationHoldRequest;
import io.booking.booking_service.dto.pojo.reservationhold.ReservationHoldResponse;
import io.booking.booking_service.dto.pojo.reservationhold.ReservationHoldUpdate;
import io.booking.booking_service.service.ReservationHoldService;
import io.booking.booking_service.util.validators.ReservationHold;
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
@RequestMapping(path = "/api/v1/reservation-holds", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReservationHoldController {

    private final ReservationHold validator;
    private final ReservationHoldService service;

    public ReservationHoldController(ReservationHold validator, ReservationHoldService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<ReservationHoldResponse>>> create(
            @Valid @RequestBody ReservationHoldRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createReservationHold] requestId={} cartId={}", requestId, request.getCartId());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<ReservationHoldResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ReservationHoldUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateReservationHold] requestId={} id={}", requestId, id);
        return service.update(requestId, id, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<ReservationHoldResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getReservationHold] requestId={} id={}", requestId, id);
        return service.get(requestId, id)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/cart/{cartId}")
    public Mono<ResponseEntity<HttpResponse<Flux<ReservationHoldResponse>>>> getByCart(
            @PathVariable UUID cartId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getReservationHoldByCart] requestId={} cartId={}", requestId, cartId);
        Flux<ReservationHoldResponse> records = service.getByCart(requestId, cartId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @GetMapping(path = "/schedule-instance/{scheduleInstanceId}")
    public Mono<ResponseEntity<HttpResponse<Flux<ReservationHoldResponse>>>> getByScheduleInstance(
            @PathVariable UUID scheduleInstanceId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getReservationHoldByScheduleInstance] requestId={} scheduleInstanceId={}", requestId, scheduleInstanceId);
        Flux<ReservationHoldResponse> records = service.getByScheduleInstance(requestId, scheduleInstanceId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }
}
