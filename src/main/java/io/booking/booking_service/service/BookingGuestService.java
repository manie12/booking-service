package io.booking.booking_service.service;

import io.booking.booking_service.dto.pojo.bookingguest.BookingGuestRequest;
import io.booking.booking_service.dto.pojo.bookingguest.BookingGuestResponse;
import io.booking.booking_service.dto.pojo.bookingguest.BookingGuestUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BookingGuestService {
    Mono<BookingGuestResponse> create(String requestId, BookingGuestRequest request);
    Mono<BookingGuestResponse> update(String requestId, UUID id, BookingGuestUpdate request);
    Mono<BookingGuestResponse> get(String requestId, UUID id);
    Flux<BookingGuestResponse> getByBookingItem(String requestId, UUID bookingItemId);
    Flux<BookingGuestResponse> getByGuestProfile(String requestId, UUID guestProfileId);
    Mono<Void> delete(String requestId, UUID id);
}
