package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.capacitypool.CapacityPoolRequest;
import io.booking.booking_service.dto.pojo.capacitypool.CapacityPoolResponse;
import io.booking.booking_service.dto.pojo.capacitypool.CapacityPoolUpdate;
import io.booking.booking_service.service.CapacityPoolService;
import io.booking.booking_service.util.validators.CapacityPool;
import io.booking.booking_service.web.http.HttpResponse;
import io.booking.booking_service.web.http.ResponseFactory;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(
        path = "/api/v1/capacity-pools",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class CapacityPoolController {

    private final CapacityPool validator;
    private final CapacityPoolService service;

    public CapacityPoolController(
            CapacityPool validator,
            CapacityPoolService service
    ) {
        this.validator = validator;
        this.service = service;
    }

    // ============================================================
    // CREATE CAPACITY POOL
    // ============================================================

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<CapacityPoolResponse>>> create(
            @Valid @RequestBody CapacityPoolRequest request,
            @RequestHeader(
                    value = "X-Request-Id",
                    required = false
            ) String headerRequestId
    ) {

        String requestId =
                validator.resolveRequestId(
                        headerRequestId,
                        null
                );

        log.info(
                "[createCapacityPool] requestId={} poolCode={}",
                requestId,
                request.getPoolCode()
        );

        return service
                .create(
                        requestId,
                        request
                )
                .map(response ->
                        ResponseEntity.ok(
                                ResponseFactory.ok(
                                        requestId,
                                        response
                                )
                        )
                );
    }

    // ============================================================
    // UPDATE CAPACITY POOL
    // ============================================================

    @PutMapping(
            path = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<HttpResponse<CapacityPoolResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CapacityPoolUpdate request,
            @RequestHeader(
                    value = "X-Request-Id",
                    required = false
            ) String headerRequestId
    ) {

        String requestId =
                validator.resolveRequestId(
                        headerRequestId,
                        null
                );

        log.info(
                "[updateCapacityPool] requestId={} id={}",
                requestId,
                id
        );

        return service
                .update(
                        requestId,
                        id,
                        request
                )
                .map(response ->
                        ResponseEntity.ok(
                                ResponseFactory.ok(
                                        requestId,
                                        response
                                )
                        )
                );
    }

    // ============================================================
    // GET CAPACITY POOL BY ID
    // ============================================================

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<CapacityPoolResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(
                    value = "X-Request-Id",
                    required = false
            ) String headerRequestId
    ) {

        String requestId =
                validator.resolveRequestId(
                        headerRequestId,
                        null
                );

        log.info(
                "[getCapacityPool] requestId={} id={}",
                requestId,
                id
        );

        return service
                .get(
                        requestId,
                        id
                )
                .map(response ->
                        ResponseEntity.ok(
                                ResponseFactory.ok(
                                        requestId,
                                        response
                                )
                        )
                );
    }

    // ============================================================
    // GET CAPACITY POOLS BY SCHEDULE INSTANCE
    // ============================================================

    @GetMapping(
            path = "/schedule-instance/{scheduleInstanceId}"
    )
    public Mono<ResponseEntity<HttpResponse<List<CapacityPoolResponse>>>>
    getByScheduleInstance(
            @PathVariable UUID scheduleInstanceId,
            @RequestHeader(
                    value = "X-Request-Id",
                    required = false
            ) String headerRequestId
    ) {

        String requestId =
                validator.resolveRequestId(
                        headerRequestId,
                        null
                );

        log.info(
                "[getCapacityPoolsByScheduleInstance] requestId={} scheduleInstanceId={}",
                requestId,
                scheduleInstanceId
        );

        return service
                .getByScheduleInstance(
                        requestId,
                        scheduleInstanceId
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
