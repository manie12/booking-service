package io.booking.booking_service.web.http;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HttpResponse<T> {

    private String requestId;
    private String statusCode;
    private String statusMessage;
    private String customerMessage;

    private T data;

    public HttpResponse(String requestId, String statusCode, String statusMessage, String customerMessage, T data) {
        this.requestId = requestId;
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.customerMessage = customerMessage;
        this.data = data;
    }

    public static <T> HttpResponse<T> of(String requestId, String statusCode, String statusMessage, String customerMessage, T data) {
        return new HttpResponse<>(requestId, statusCode, statusMessage, customerMessage, data);
    }


}