package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.BookingStatus;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.BookingItemEntity;
import io.booking.booking_service.repository.BookingItemRepository;
import io.booking.booking_service.util.SharedUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingItem {

    private final SharedUtils sharedUtils;
    private final BookingItemRepository repository;

    /** Load booking item by id, fail with NOT_FOUND if missing */
    public Mono<BookingItemEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.BOOKING_ITEM_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.BOOKING_ITEM_NOT_FOUND)));
    }

    /** Validate booking id is present */
    public Mono<Void> validateBookingRequired(UUID bookingId) {
        if (bookingId == null)
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_NOT_FOUND));
        return Mono.empty();
    }

    /** Validate schedule instance is present */
    public Mono<Void> validateScheduleInstanceRequired(UUID scheduleInstanceId) {
        if (scheduleInstanceId == null)
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_ITEM_SCHEDULE_REQUIRED));
        return Mono.empty();
    }

    /** Validate quantity is >= 1 */
    public Mono<Void> validateQuantity(Integer quantity) {
        if (quantity == null || quantity < 1)
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_ITEM_QUANTITY_INVALID));
        return Mono.empty();
    }

    /** Validate time window: startAt must be before endAt */
    public Mono<Void> validateTimeWindow(OffsetDateTime startAt, OffsetDateTime endAt) {
        if (startAt != null && endAt != null && !startAt.isBefore(endAt))
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_ITEM_WINDOW_INVALID));
        return Mono.empty();
    }

    /** Validate status is not null */
    public Mono<Void> validateStatusRequired(BookingStatus status) {
        if (status == null)
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_ITEM_STATUS_INVALID));
        return Mono.empty();
    }

    /** Assert booking item is not cancelled */
    public Mono<BookingItemEntity> assertNotCancelled(BookingItemEntity item) {
        if (BookingStatus.CANCELLED.equals(item.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_ALREADY_CANCELLED));
        return Mono.just(item);
    }

    /** Assert booking item is not completed */
    public Mono<BookingItemEntity> assertNotCompleted(BookingItemEntity item) {
        if (BookingStatus.COMPLETED.equals(item.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_ALREADY_COMPLETED));
        return Mono.just(item);
    }

    /** Assert booking item belongs to the given booking */
    public Mono<BookingItemEntity> assertBelongsToBooking(BookingItemEntity item, UUID bookingId) {
        if (!bookingId.equals(item.getBookingId()))
            return Mono.error(BookingException.of(BookingErrorType.BOOKING_ITEM_REQUEST_INVALID));
        return Mono.just(item);
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
