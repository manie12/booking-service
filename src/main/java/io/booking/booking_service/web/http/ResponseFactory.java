package io.booking.booking_service.web.http;


import io.catalog.catalog.datatype.CatalogErrorType;

import java.util.Map;

public class ResponseFactory {

    private ResponseFactory() {
    }

    public static <T> HttpResponse<T> of(String requestId, CatalogErrorType errorType, T data) {
        return new HttpResponse<>(
                requestId,
                errorType.getCode(),
                errorType.getStatusMessage(),
                errorType.getCustomerMessage(),
                data
        );
    }

    public static HttpResponse<Void> of(String requestId, CatalogErrorType errorType) {
        return of(requestId, errorType, null);
    }

    public static <T> HttpResponse<T> ok(String requestId, T data) {
        return of(requestId, CatalogErrorType.SUCCESS, data);
    }

    public static HttpResponse<Void> ok(String requestId) {
        return ok(requestId, null);
    }

    public static HttpResponse<Map<String, Object>> of(String requestId, CatalogErrorType errorType, String key, Object value) {
        return of(requestId, errorType, Map.of(key, value));
    }
}