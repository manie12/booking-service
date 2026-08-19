package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.scheduleinstance.ScheduleInstanceRequest;
import io.booking.booking_service.dto.pojo.scheduleinstance.ScheduleInstanceResponse;
import io.booking.booking_service.dto.pojo.scheduleinstance.ScheduleInstanceUpdate;
import io.booking.booking_service.service.ScheduleInstanceService;
import io.booking.booking_service.util.validators.ScheduleInstance;
import io.booking.booking_service.web.http.HttpResponse;
import io.booking.booking_service.web.http.ResponseFactory;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/schedule-instances", produces = MediaType.APPLICATION_JSON_VALUE)
public class ScheduleInstanceController {

    private final ScheduleInstance validator;
    private final ScheduleInstanceService service;

    public ScheduleInstanceController(ScheduleInstance validator, ScheduleInstanceService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<ScheduleInstanceResponse>>> create(
            @Valid @RequestBody ScheduleInstanceRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createScheduleInstance] requestId={} code={}", requestId, request.getInstanceCode());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<ScheduleInstanceResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ScheduleInstanceUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateScheduleInstance] requestId={} id={}", requestId, id);
        return service.update(requestId, id, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<ScheduleInstanceResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {

        String requestId = validator.resolveRequestId(headerRequestId, null);

        log.info(
                "[getScheduleInstance] requestId={} id={}",
                requestId,
                id
        );

        return service.get(requestId, id)
                .map(response ->
                        ResponseEntity.ok(
                                ResponseFactory.ok(
                                        requestId,
                                        response
                                )
                        )
                );
    }

    @GetMapping(path = "/product/{productId}")
    public Mono<ResponseEntity<HttpResponse<List<ScheduleInstanceResponse>>>> getByProduct(
            @PathVariable UUID productId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {

        String requestId = validator.resolveRequestId(headerRequestId, null);

        log.info(
                "[getScheduleInstanceByProduct] requestId={} productId={}",
                requestId,
                productId
        );

        return service.getByProduct(requestId, productId)
                .collectList()
                .map(records ->
                        ResponseEntity.ok(
                                ResponseFactory.ok(
                                        requestId,
                                        records
                                )
                        )
                );
    }

    @GetMapping(path = "/product-variant/{productVariantId}")
    public Mono<ResponseEntity<HttpResponse<List<ScheduleInstanceResponse>>>> getByProductVariant(
            @PathVariable UUID productVariantId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {

        String requestId = validator.resolveRequestId(headerRequestId, null);

        log.info(
                "[getScheduleInstanceByProductVariant] requestId={} productVariantId={}",
                requestId,
                productVariantId
        );

        return service.getByProductVariant(
                        requestId,
                        productVariantId
                )
                .collectList()
                .map(records ->
                        ResponseEntity.ok(
                                ResponseFactory.ok(
                                        requestId,
                                        records
                                )
                        )
                );
    }
}
