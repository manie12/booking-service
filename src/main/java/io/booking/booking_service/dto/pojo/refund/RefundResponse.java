package io.booking.booking_service.dto.pojo.refund;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.RefundStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class RefundResponse {

    private UUID id;
    private UUID tenantId;
    private UUID orderId;
    private UUID paymentId;
    private String refundReference;
    private RefundStatus status;
    private String currencyCode;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private BigDecimal refundedAmount;
    private String reasonCode;
    private String reasonText;
    private ActorType actorType;
    private String actorId;
    private OffsetDateTime requestedAt;
    private OffsetDateTime completedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
