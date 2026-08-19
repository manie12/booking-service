package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.checkin.CheckInRequest;
import io.booking.booking_service.dto.pojo.checkin.CheckInResponse;
import io.booking.booking_service.dto.pojo.checkin.CheckInUpdate;
import io.booking.booking_service.service.CheckInService;
import io.booking.booking_service.util.validators.CheckIn;
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
@RequestMapping(path = "/api/v1/check-ins", produces = MediaType.APPLICATION_JSON_VALUE)
public class CheckInController {

    private final CheckIn validator;
    private final CheckInService service;

    public CheckInController(CheckIn validator, CheckInService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<CheckInResponse>>> create(
            @Valid @RequestBody CheckInRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createCheckIn] requestId={} entitlementId={}", requestId, request.getEntitlementId());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<CheckInResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CheckInUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateCheckIn] requestId={} id={}", requestId, id);
        return service.update(requestId, id, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<CheckInResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCheckIn] requestId={} id={}", requestId, id);
        return service.get(requestId, id)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/entitlement/{entitlementId}")
    public Mono<ResponseEntity<HttpResponse<Flux<CheckInResponse>>>> getByEntitlement(
            @PathVariable UUID entitlementId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCheckInsByEntitlement] requestId={} entitlementId={}", requestId, entitlementId);
        Flux<CheckInResponse> records = service.getByEntitlement(requestId, entitlementId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @GetMapping(path = "/booking/{bookingId}")
    public Mono<ResponseEntity<HttpResponse<Flux<CheckInResponse>>>> getByBooking(
            @PathVariable UUID bookingId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCheckInsByBooking] requestId={} bookingId={}", requestId, bookingId);
        Flux<CheckInResponse> records = service.getByBooking(requestId, bookingId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @GetMapping(path = "/guest-profile/{guestProfileId}")
    public Mono<ResponseEntity<HttpResponse<Flux<CheckInResponse>>>> getByGuestProfile(
            @PathVariable UUID guestProfileId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCheckInsByGuestProfile] requestId={} guestProfileId={}", requestId, guestProfileId);
        Flux<CheckInResponse> records = service.getByGuestProfile(requestId, guestProfileId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }
}
