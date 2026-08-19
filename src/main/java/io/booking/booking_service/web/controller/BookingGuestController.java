package io.booking.booking_service.web.controller;

import io.booking.booking_service.dto.pojo.bookingguest.BookingGuestRequest;
import io.booking.booking_service.dto.pojo.bookingguest.BookingGuestResponse;
import io.booking.booking_service.dto.pojo.bookingguest.BookingGuestUpdate;
import io.booking.booking_service.service.BookingGuestService;
import io.booking.booking_service.util.validators.BookingGuest;
import io.booking.booking_service.web.http.HttpResponse;
import io.booking.booking_service.web.http.ResponseFactory;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/booking-guests", produces = MediaType.APPLICATION_JSON_VALUE)
public class BookingGuestController {

    private final BookingGuest validator;
    private final BookingGuestService service;

    public BookingGuestController(BookingGuest validator, BookingGuestService service) {
        this.validator = validator;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<BookingGuestResponse>>> create(
            @Valid @RequestBody BookingGuestRequest request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, request.getBookingItemId());
        log.info("[createBookingGuest] requestId={} bookingItemId={}", requestId, request.getBookingItemId());
        return service.create(requestId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<HttpResponse<BookingGuestResponse>>> update(
            @PathVariable String id,
            @Valid @RequestBody BookingGuestUpdate request,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[updateBookingGuest] requestId={} id={}", requestId, id);
        UUID keyId = UUID.fromString(id);
        return service.update(requestId, keyId, request)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<BookingGuestResponse>>> get(
            @PathVariable String id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getBookingGuest] requestId={} id={}", requestId, id);
        UUID keyId = UUID.fromString(id);
        return service.get(requestId, keyId)
                .map(r -> ResponseEntity.ok(ResponseFactory.ok(requestId, r)));
    }

    @GetMapping(path = "/booking-item/{bookingItemId}")
    public Mono<ResponseEntity<HttpResponse<Flux<BookingGuestResponse>>>> getByBookingItem(
            @PathVariable String bookingItemId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getBookingGuestByBookingItem] requestId={} bookingItemId={}", requestId, bookingItemId);
        UUID keyBookingItemId = UUID.fromString(bookingItemId);
        Flux<BookingGuestResponse> guests = service.getByBookingItem(requestId, keyBookingItemId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, guests)));
    }

    @GetMapping(path = "/guest-profile/{guestProfileId}")
    public Mono<ResponseEntity<HttpResponse<Flux<BookingGuestResponse>>>> getByGuestProfile(
            @PathVariable String guestProfileId,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[getBookingGuestByGuestProfile] requestId={} guestProfileId={}", requestId, guestProfileId);
        UUID keyGuestProfileId = UUID.fromString(guestProfileId);
        Flux<BookingGuestResponse> guests = service.getByGuestProfile(requestId, keyGuestProfileId);
        return Mono.just(ResponseEntity.ok(ResponseFactory.ok(requestId, guests)));
    }

    @DeleteMapping(path = "/{id}")
    public Mono<ResponseEntity<HttpResponse<Void>>> delete(
            @PathVariable String id,
            @RequestHeader(value = "X-Request-Id", required = false) String headerRequestId) {
        String requestId = validator.resolveRequestId(headerRequestId, null);
        log.info("[deleteBookingGuest] requestId={} id={}", requestId, id);
        UUID keyId = UUID.fromString(id);
        return service.delete(requestId, keyId)
                .thenReturn(ResponseEntity.ok(ResponseFactory.<Void>ok(requestId)));
    }
}
