package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.entitlementusageevent.EntitlementUsageEventRequest;
import io.booking.booking_service.dto.pojo.entitlementusageevent.EntitlementUsageEventResponse;
import io.booking.booking_service.dto.pojo.entitlementusageevent.EntitlementUsageEventUpdate;
import io.booking.booking_service.service.EntitlementUsageEventService;
import io.booking.booking_service.util.validators.EntitlementUsageEvent;
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
@RequestMapping(path = "/api/v1/entitlement-usage-events", produces = MediaType.APPLICATION_JSON_VALUE)
public class EntitlementUsageEventController {

    private final EntitlementUsageEvent validator;
    private final EntitlementUsageEventService service;

    public EntitlementUsageEventController(EntitlementUsageEvent validator, EntitlementUsageEventService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<EntitlementUsageEventResponse>>> create(
            @Valid @RequestBody EntitlementUsageEventRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createEntitlementUsageEvent] requestId={} entitlementId={}", requestId, request.getEntitlementId());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<EntitlementUsageEventResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody EntitlementUsageEventUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateEntitlementUsageEvent] requestId={} id={}", requestId, id);
        return service.update(requestId, id, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<EntitlementUsageEventResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getEntitlementUsageEvent] requestId={} id={}", requestId, id);
        return service.get(requestId, id)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/entitlement/{entitlementId}")
    public Mono<ResponseEntity<HttpResponse<Flux<EntitlementUsageEventResponse>>>> getByEntitlement(
            @PathVariable UUID entitlementId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getEntitlementUsageEventsByEntitlement] requestId={} entitlementId={}", requestId, entitlementId);
        Flux<EntitlementUsageEventResponse> records = service.getByEntitlement(requestId, entitlementId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }
}
