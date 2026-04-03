package io.booking.booking_service.exception;

import io.booking.booking_service.datatype.BookingErrorType;
import lombok.Getter;

/**
 * Lightweight business exception carrying CatalogErrorType (+ optional data).
 * Allows GlobalExceptionHandler to return the exact business code/message.
 */
@Getter
public class BookingException extends RuntimeException {

    private final BookingErrorType errorType;
    private final Object data;

    public BookingException(BookingErrorType errorType) {
        this(errorType, null, null);
    }

    public BookingException(BookingErrorType errorType, Object data) {
        this(errorType, data, null);
    }

    public BookingException(BookingErrorType errorType, Object data, Throwable cause) {
        super(errorType != null ? errorType.getStatusMessage() : "booking error", cause);
        this.errorType = errorType;
        this.data = data;
    }

    public static BookingException of(BookingErrorType type) {
        return new BookingException(type);
    }

    public static BookingException of(BookingErrorType type, Object data) {
        return new BookingException(type, data);
    }
}