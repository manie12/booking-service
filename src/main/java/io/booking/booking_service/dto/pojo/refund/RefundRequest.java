package io.booking.booking_service.dto.pojo.refund;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.RefundStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class RefundRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Payment ID is required")
    private UUID paymentId;

    @NotBlank(message = "Refund reference is required")
    private String refundReference;

    private RefundStatus status;

    @NotBlank(message = "Currency code is required")
    private String currencyCode;

    @NotNull(message = "Requested amount is required")
    private BigDecimal requestedAmount;

    private BigDecimal approvedAmount;
    private BigDecimal refundedAmount;
    private String reasonCode;
    private String reasonText;
    private ActorType actorType;
    private String actorId;
    private OffsetDateTime requestedAt;
    private OffsetDateTime completedAt;
}
