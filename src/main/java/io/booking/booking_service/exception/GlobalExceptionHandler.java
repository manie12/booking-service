package io.booking.booking_service.exception;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.web.http.HttpResponse;
import io.booking.booking_service.web.http.ResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<HttpResponse<Map<String, Object>>>> handleBind(
            WebExchangeBindException ex,
            ServerHttpRequest request
    ) {
        String requestId = resolveRequestId(request);

        Map<String, Object> details = validationDetails(ex.getFieldErrors());
        HttpResponse<Map<String, Object>> body =
                ResponseFactory.of(requestId, BookingErrorType.VALIDATION_ERROR, details);

        return Mono.just(ResponseEntity.ok(body));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public Mono<ResponseEntity<HttpResponse<Map<String, Object>>>> handleMethodValidation(
            HandlerMethodValidationException ex,
            ServerHttpRequest request
    ) {
        String requestId = resolveRequestId(request);

        Map<String, Object> details = Map.of(
                "message", "Method validation failed",
                "errors", List.of(ex.getMessage())
        );

        HttpResponse<Map<String, Object>> body =
                ResponseFactory.of(requestId, BookingErrorType.VALIDATION_ERROR, details);

        return Mono.just(ResponseEntity.ok(body));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<HttpResponse<Map<String, Object>>>> handleInput(
            ServerWebInputException ex,
            ServerHttpRequest request
    ) {
        String requestId = resolveRequestId(request);

        Map<String, Object> details = Map.of(
                "message", "Invalid request input",
                "errors", List.of(ex.getReason() != null ? ex.getReason() : ex.getMessage())
        );

        HttpResponse<Map<String, Object>> body =
                ResponseFactory.of(requestId, BookingErrorType.VALIDATION_ERROR, details);

        return Mono.just(ResponseEntity.ok(body));
    }

    /**
     * Business exception mapping — returns the exact CatalogErrorType code/message,
     * plus optional data when provided.
     */
    @ExceptionHandler(BookingException.class)
    public Mono<ResponseEntity<HttpResponse<Object>>> handleCatalogException(
            BookingException ex,
            ServerHttpRequest request
    ) {
        String requestId = resolveRequestId(request);

        BookingErrorType type =
                ex.getErrorType() != null ? ex.getErrorType() : BookingErrorType.GENERIC_ERROR;

        HttpResponse<Object> body = ResponseFactory.of(requestId, type, ex.getData());

        return Mono.just(ResponseEntity.ok(body));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<HttpResponse<Void>>> handleGeneric(
            Exception ex,
            ServerHttpRequest request
    ) {
        String requestId = resolveRequestId(request);

        HttpResponse<Void> body =
                ResponseFactory.of(requestId, BookingErrorType.GENERIC_ERROR);

        return Mono.just(ResponseEntity.ok(body));
    }

    private String resolveRequestId(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst("X-Request-Id");
        return (header != null && !header.isBlank()) ? header : request.getId();
    }

    private Map<String, Object> validationDetails(List<FieldError> fieldErrors) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", "Validation failed");

        List<Map<String, Object>> errors = fieldErrors.stream()
                .map(fe -> Map.of(
                        "field", fe.getField(),
                        "rejectedValue", fe.getRejectedValue(),
                        "error", fe.getDefaultMessage()
                ))
                .toList();

        payload.put("errors", errors);
        return payload;
    }
}