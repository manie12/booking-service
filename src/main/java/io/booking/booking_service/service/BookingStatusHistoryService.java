package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.bookingstatushistory.BookingStatusHistoryRequest;
import io.booking.booking_service.dto.pojo.bookingstatushistory.BookingStatusHistoryResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BookingStatusHistoryService {
    Mono<BookingStatusHistoryResponse> create(String requestId, BookingStatusHistoryRequest request);
    Mono<BookingStatusHistoryResponse> get(String requestId, UUID id);
    Flux<BookingStatusHistoryResponse> getByBooking(String requestId, UUID bookingId);
    Flux<BookingStatusHistoryResponse> getByBookingItem(String requestId, UUID bookingItemId);
}
