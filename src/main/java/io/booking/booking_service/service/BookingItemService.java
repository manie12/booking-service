package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.bookingitem.BookingItemRequest;
import io.booking.booking_service.dto.pojo.bookingitem.BookingItemResponse;
import io.booking.booking_service.dto.pojo.bookingitem.BookingItemUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BookingItemService {
    Mono<BookingItemResponse> create(String requestId, BookingItemRequest request);
    Mono<BookingItemResponse> update(String requestId, UUID id, BookingItemUpdate request);
    Mono<BookingItemResponse> get(String requestId, UUID id);
    Flux<BookingItemResponse> getByBooking(String requestId, UUID bookingId);
    Flux<BookingItemResponse> getByScheduleInstance(String requestId, UUID scheduleInstanceId);
    Flux<BookingItemResponse> getByOrderItem(String requestId, UUID orderItemId);
    Mono<Void> cancel(String requestId, UUID id);
}
