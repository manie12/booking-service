package io.booking.booking_service.dto.pojo.paymenttransaction;

import io.booking.booking_service.datatype.booking.PaymentStatus;
import io.booking.booking_service.datatype.booking.PaymentTransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class PaymentTransactionRequest {

    @NotNull(message = "Payment ID is required")
    private UUID paymentId;

    private String transactionReference;
    private String providerTransactionId;

    @NotNull(message = "Transaction type is required")
    private PaymentTransactionType transactionType;

    private PaymentStatus status;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private String currencyCode;
    private String providerResponseCode;
    private String providerResponseMessage;
    private String payloadJson;
    private OffsetDateTime processedAt;
}
