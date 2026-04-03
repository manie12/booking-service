package io.booking.booking_service.dto.pojo.paymenttransaction;

import io.booking.booking_service.datatype.booking.PaymentStatus;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class PaymentTransactionUpdate {

    private PaymentStatus status;
    private String providerResponseCode;
    private String providerResponseMessage;
    private String payloadJson;
    private OffsetDateTime processedAt;
}
