package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.entitlement.EntitlementRequest;
import io.booking.booking_service.dto.pojo.entitlement.EntitlementResponse;
import io.booking.booking_service.dto.pojo.entitlement.EntitlementUpdate;
import io.booking.booking_service.service.EntitlementService;
import io.booking.booking_service.util.validators.Entitlement;
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
@RequestMapping(path = "/api/v1/entitlements", produces = MediaType.APPLICATION_JSON_VALUE)
public class EntitlementController {

    private final Entitlement validator;
    private final EntitlementService service;

    public EntitlementController(Entitlement validator, EntitlementService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<EntitlementResponse>>> create(
            @Valid @RequestBody EntitlementRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createEntitlement] requestId={} entitlementNumber={}", requestId, request.getEntitlementNumber());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<EntitlementResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody EntitlementUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateEntitlement] requestId={} id={}", requestId, id);
        return service.update(requestId, id, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<EntitlementResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getEntitlement] requestId={} id={}", requestId, id);
        return service.get(requestId, id)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/number/{entitlementNumber}")
    public Mono<ResponseEntity<HttpResponse<EntitlementResponse>>> getByEntitlementNumber(
            @PathVariable String entitlementNumber,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getEntitlementByNumber] requestId={} entitlementNumber={}", requestId, entitlementNumber);
        return service.getByEntitlementNumber(requestId, entitlementNumber)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/order/{orderId}")
    public Mono<ResponseEntity<HttpResponse<Flux<EntitlementResponse>>>> getByOrder(
            @PathVariable UUID orderId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getEntitlementsByOrder] requestId={} orderId={}", requestId, orderId);
        Flux<EntitlementResponse> records = service.getByOrder(requestId, orderId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @GetMapping(path = "/booking/{bookingId}")
    public Mono<ResponseEntity<HttpResponse<Flux<EntitlementResponse>>>> getByBooking(
            @PathVariable UUID bookingId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getEntitlementsByBooking] requestId={} bookingId={}", requestId, bookingId);
        Flux<EntitlementResponse> records = service.getByBooking(requestId, bookingId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @GetMapping(path = "/guest-profile/{guestProfileId}")
    public Mono<ResponseEntity<HttpResponse<Flux<EntitlementResponse>>>> getByGuestProfile(
            @PathVariable UUID guestProfileId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getEntitlementsByGuestProfile] requestId={} guestProfileId={}", requestId, guestProfileId);
        Flux<EntitlementResponse> records = service.getByGuestProfile(requestId, guestProfileId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @PatchMapping(path = "/{id}/revoke")
    public Mono<ResponseEntity<HttpResponse<Void>>> revoke(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[revokeEntitlement] requestId={} id={}", requestId, id);
        return service.revoke(requestId, id)
                .thenReturn(ResponseEntity.ok(ResponseFactory.<Void>ok(requestId)));
    }

    @PatchMapping(path = "/{id}/cancel")
    public Mono<ResponseEntity<HttpResponse<Void>>> cancel(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[cancelEntitlement] requestId={} id={}", requestId, id);
        return service.cancel(requestId, id)
                .thenReturn(ResponseEntity.ok(ResponseFactory.<Void>ok(requestId)));
    }
}
