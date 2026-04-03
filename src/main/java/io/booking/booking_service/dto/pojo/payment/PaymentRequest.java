package io.booking.booking_service.dto.pojo.payment;

import io.booking.booking_service.datatype.booking.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class PaymentRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotBlank(message = "Payment reference is required")
    private String paymentReference;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String providerName;

    @NotBlank(message = "Currency code is required")
    private String currencyCode;

    @NotNull(message = "Requested amount is required")
    private BigDecimal requestedAmount;

    private BigDecimal authorizedAmount;
    private BigDecimal capturedAmount;
    private BigDecimal refundedAmount;
    private PaymentStatus status;
    private OffsetDateTime initiatedAt;
    private OffsetDateTime completedAt;
}
