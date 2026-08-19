package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.accesspass.AccessPassRequest;
import io.booking.booking_service.dto.pojo.accesspass.AccessPassResponse;
import io.booking.booking_service.dto.pojo.accesspass.AccessPassUpdate;
import io.booking.booking_service.service.AccessPassService;
import io.booking.booking_service.util.validators.AccessPass;
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
@RequestMapping(path = "/api/v1/access-passes", produces = MediaType.APPLICATION_JSON_VALUE)
public class AccessPassController {

    private final AccessPass validator;
    private final AccessPassService service;

    public AccessPassController(AccessPass validator, AccessPassService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<AccessPassResponse>>> create(
            @Valid @RequestBody AccessPassRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, request.getPassNumber());
        log.info("[createAccessPass] requestId={} passNumber={}", requestId, request.getPassNumber());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<AccessPassResponse>>> update(
            @PathVariable String id,
            @Valid @RequestBody AccessPassUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateAccessPass] requestId={} id={}", requestId, id);
        UUID keyId = UUID.fromString(id);
        return service.update(requestId, keyId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<AccessPassResponse>>> get(
            @PathVariable String id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getAccessPass] requestId={} id={}", requestId, id);
        UUID keyId = UUID.fromString(id);
        return service.get(requestId, keyId)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/entitlement/{entitlementId}")
    public Mono<ResponseEntity<HttpResponse<Flux<AccessPassResponse>>>> getByEntitlement(
            @PathVariable String entitlementId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getAccessPassByEntitlement] requestId={} entitlementId={}", requestId, entitlementId);
        UUID keyEntitlementId = UUID.fromString(entitlementId);
        Flux<AccessPassResponse> passes = service.getByEntitlement(requestId, keyEntitlementId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, passes)));
    }

    @PatchMapping(path = "/{id}/revoke")
    public Mono<ResponseEntity<HttpResponse<AccessPassResponse>>> revoke(
            @PathVariable String id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[revokeAccessPass] requestId={} id={}", requestId, id);
        UUID keyId = UUID.fromString(id);
        return service.revoke(requestId, keyId)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }
}
