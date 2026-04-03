package io.booking.booking_service.dto.pojo.paymenttransaction;

import io.booking.booking_service.datatype.booking.PaymentStatus;
import io.booking.booking_service.datatype.booking.PaymentTransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class PaymentTransactionResponse {

    private UUID id;
    private UUID paymentId;
    private String transactionReference;
    private String providerTransactionId;
    private PaymentTransactionType transactionType;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currencyCode;
    private String providerResponseCode;
    private String providerResponseMessage;
    private String payloadJson;
    private OffsetDateTime processedAt;
    private OffsetDateTime createdAt;
}
