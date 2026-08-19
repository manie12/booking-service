package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.guestprofile.GuestProfileRequest;
import io.booking.booking_service.dto.pojo.guestprofile.GuestProfileResponse;
import io.booking.booking_service.dto.pojo.guestprofile.GuestProfileUpdate;
import io.booking.booking_service.service.GuestProfileService;
import io.booking.booking_service.util.validators.GuestProfile;
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
@RequestMapping(path = "/api/v1/guest-profiles", produces = MediaType.APPLICATION_JSON_VALUE)
public class GuestProfileController {

    private final GuestProfile validator;
    private final GuestProfileService service;

    public GuestProfileController(GuestProfile validator, GuestProfileService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<GuestProfileResponse>>> create(
            @Valid @RequestBody GuestProfileRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createGuestProfile] requestId={} firstName={} lastName={}", requestId, request.getFirstName(), request.getLastName());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<GuestProfileResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody GuestProfileUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateGuestProfile] requestId={} id={}", requestId, id);
        return service.update(requestId, id, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<GuestProfileResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getGuestProfile] requestId={} id={}", requestId, id);
        return service.get(requestId, id)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/customer/{customerId}")
    public Mono<ResponseEntity<HttpResponse<Flux<GuestProfileResponse>>>> getByCustomer(
            @PathVariable String customerId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getGuestProfilesByCustomer] requestId={} customerId={}", requestId, customerId);
        Flux<GuestProfileResponse> records = service.getByCustomer(requestId, customerId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @DeleteMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<Void>>> delete(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[deleteGuestProfile] requestId={} id={}", requestId, id);
        return service.delete(requestId, id)
                .thenReturn(ResponseEntity.ok(ResponseFactory.<Void>ok(requestId)));
    }
}
