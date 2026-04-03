package io.booking.booking_service.dto.pojo.payment;

import io.booking.booking_service.datatype.booking.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class PaymentResponse {

    private UUID id;
    private UUID tenantId;
    private UUID orderId;
    private String paymentReference;
    private String paymentMethod;
    private String providerName;
    private String currencyCode;
    private BigDecimal requestedAmount;
    private BigDecimal authorizedAmount;
    private BigDecimal capturedAmount;
    private BigDecimal refundedAmount;
    private PaymentStatus status;
    private OffsetDateTime initiatedAt;
    private OffsetDateTime completedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
