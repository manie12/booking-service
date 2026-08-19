package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.bookingstatushistory.BookingStatusHistoryRequest;
import io.booking.booking_service.dto.pojo.bookingstatushistory.BookingStatusHistoryResponse;
import io.booking.booking_service.service.BookingStatusHistoryService;
import io.booking.booking_service.util.validators.BookingStatusHistoryValidator;
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
@RequestMapping(path = "/api/v1/booking-status-history", produces = MediaType.APPLICATION_JSON_VALUE)
public class BookingStatusHistoryController {

    private final BookingStatusHistoryValidator validator;
    private final BookingStatusHistoryService service;

    public BookingStatusHistoryController(BookingStatusHistoryValidator validator, BookingStatusHistoryService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<BookingStatusHistoryResponse>>> create(
            @Valid @RequestBody BookingStatusHistoryRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createBookingStatusHistory] requestId={} bookingId={}", requestId, request.getBookingId());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<BookingStatusHistoryResponse>>> get(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getBookingStatusHistory] requestId={} id={}", requestId, id);
        return service.get(requestId, id)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/booking/{bookingId}")
    public Mono<ResponseEntity<HttpResponse<Flux<BookingStatusHistoryResponse>>>> getByBooking(
            @PathVariable UUID bookingId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getBookingStatusHistoryByBooking] requestId={} bookingId={}", requestId, bookingId);
        Flux<BookingStatusHistoryResponse> records = service.getByBooking(requestId, bookingId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }

    @GetMapping(path = "/booking-item/{bookingItemId}")
    public Mono<ResponseEntity<HttpResponse<Flux<BookingStatusHistoryResponse>>>> getByBookingItem(
            @PathVariable UUID bookingItemId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getBookingStatusHistoryByBookingItem] requestId={} bookingItemId={}", requestId, bookingItemId);
        Flux<BookingStatusHistoryResponse> records = service.getByBookingItem(requestId, bookingItemId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, records)));
    }
}
