package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.customer.CustomerRequest;
import io.booking.booking_service.dto.pojo.customer.CustomerResponse;
import io.booking.booking_service.dto.pojo.customer.CustomerUpdate;
import io.booking.booking_service.service.CustomerService;
import io.booking.booking_service.util.validators.Customer;
import io.booking.booking_service.web.http.HttpResponse;
import io.booking.booking_service.web.http.ResponseFactory;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/customers", produces = MediaType.APPLICATION_JSON_VALUE)
public class CustomerController {

    private final Customer validator;
    private final CustomerService service;

    public CustomerController(Customer validator, CustomerService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<ResponseEntity<HttpResponse<CustomerResponse>>> create(
            @Valid @RequestBody CustomerRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        log.info("[createCustomer] requestId={} request={}", headerRequestId, request.getCustomerNumber());
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createCustomer] requestId={} customerNumber={}", requestId, request.getCustomerNumber());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<CustomerResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateCustomer] requestId={} id={}", requestId, id);
        return service.update(requestId, id, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<CustomerResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCustomer] requestId={} id={}", requestId, id);
        return service.get(requestId, id)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/number/{customerNumber}")
    public Mono<ResponseEntity<HttpResponse<CustomerResponse>>> getByCustomerNumber(
            @PathVariable String customerNumber,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCustomerByNumber] requestId={} customerNumber={}", requestId, customerNumber);
        return service.getByCustomerNumber(requestId, customerNumber)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/email/{email}")
    public Mono<ResponseEntity<HttpResponse<CustomerResponse>>> getByEmail(
            @PathVariable String email,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getCustomerByEmail] requestId={} email={}", requestId, email);
        return service.getByEmail(requestId, email)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PatchMapping(path = "/{id}/deactivate")
    public Mono<ResponseEntity<HttpResponse<Void>>> deactivate(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[deactivateCustomer] requestId={} id={}", requestId, id);
        return service.deactivate(requestId, id)
                .thenReturn(ResponseEntity.ok(ResponseFactory.<Void>ok(requestId)));
    }
}
