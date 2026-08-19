package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.bookingitem.BookingItemRequest;
import io.booking.booking_service.dto.pojo.bookingitem.BookingItemResponse;
import io.booking.booking_service.dto.pojo.bookingitem.BookingItemUpdate;
import io.booking.booking_service.service.BookingItemService;
import io.booking.booking_service.util.validators.BookingItem;
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
@RequestMapping(path = "/api/v1/booking-items", produces = MediaType.APPLICATION_JSON_VALUE)
public class BookingItemController {

    private final BookingItem validator;
    private final BookingItemService service;

    public BookingItemController(BookingItem validator, BookingItemService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<BookingItemResponse>>> create(
            @Valid @RequestBody BookingItemRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[createBookingItem] requestId={} bookingId={}", requestId, request.getBookingId());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<BookingItemResponse>>> update(
            @PathVariable UUID id,
            @Valid @RequestBody BookingItemUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateBookingItem] requestId={} id={}", requestId, id);
        return service.update(requestId, id, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<BookingItemResponse>>> get(
            @PathVariable String id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        UUID keyId = UUID.fromString(id);
        log.info("[getBookingItem] requestId={} id={}", requestId, id);
        return service.get(requestId, keyId)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/booking/{bookingId}")
    public Mono<ResponseEntity<HttpResponse<Flux<BookingItemResponse>>>> getByBooking(
            @PathVariable String bookingId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getBookingItemsByBooking] requestId={} bookingId={}", requestId, bookingId);
        UUID keyId = UUID.fromString(bookingId);
        Flux<BookingItemResponse> items = service.getByBooking(requestId, keyId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, items)));
    }

    @GetMapping(path = "/schedule-instance/{scheduleInstanceId}")
    public Mono<ResponseEntity<HttpResponse<Flux<BookingItemResponse>>>> getByScheduleInstance(
            @PathVariable String scheduleInstanceId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getBookingItemsByScheduleInstance] requestId={} scheduleInstanceId={}", requestId, scheduleInstanceId);
        UUID keyId = UUID.fromString(scheduleInstanceId);
        Flux<BookingItemResponse> items = service.getByScheduleInstance(requestId, keyId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, items)));
    }

    @GetMapping(path = "/order-item/{orderItemId}")
    public Mono<ResponseEntity<HttpResponse<Flux<BookingItemResponse>>>> getByOrderItem(
            @PathVariable String orderItemId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getBookingItemsByOrderItem] requestId={} orderItemId={}", requestId, orderItemId);
        UUID keyId = UUID.fromString(orderItemId);
        Flux<BookingItemResponse> items = service.getByOrderItem(requestId, keyId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, items)));
    }

    @PatchMapping(path = "/{id}/cancel")
    public Mono<ResponseEntity<HttpResponse<Void>>> cancel(
            @PathVariable String id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[cancelBookingItem] requestId={} id={}", requestId, id);
        UUID keyId = UUID.fromString(id);
        return service.cancel(requestId, keyId)
                .thenReturn(ResponseEntity.ok(ResponseFactory.<Void>ok(requestId)));
    }
}

