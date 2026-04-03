package io.booking.booking_service.exception;

import io.catalog.catalog.datatype.CatalogErrorType;
import lombok.Getter;

/**
 * Lightweight business exception carrying CatalogErrorType (+ optional data).
 * Allows GlobalExceptionHandler to return the exact business code/message.
 */
@Getter
public class BookingException extends RuntimeException {

    private final CatalogErrorType errorType;
    private final Object data;

    public BookingException(CatalogErrorType errorType) {
        this(errorType, null, null);
    }

    public BookingException(CatalogErrorType errorType, Object data) {
        this(errorType, data, null);
    }

    public BookingException(CatalogErrorType errorType, Object data, Throwable cause) {
        super(errorType != null ? errorType.getStatusMessage() : "booking error", cause);
        this.errorType = errorType;
        this.data = data;
    }

    public static BookingException of(CatalogErrorType type) {
        return new CatalogException(type);
    }

    public static BookingException of(CatalogErrorType type, Object data) {
        return new CatalogException(type, data);
    }
}