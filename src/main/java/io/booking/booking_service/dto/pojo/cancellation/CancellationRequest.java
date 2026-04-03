package io.booking.booking_service.dto.pojo.cancellation;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CancellationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CancellationRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    private UUID orderItemId;
    private UUID bookingId;
    private UUID bookingItemId;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    private CancellationStatus status;

    @NotBlank(message = "Reason code is required")
    private String reasonCode;

    private String reasonText;
    private String policyReference;
    private Boolean refundEligible;
    private BigDecimal refundAmountEstimate;
    private ActorType actorType;
    private String actorId;
    private OffsetDateTime requestedAt;
    private OffsetDateTime completedAt;
}
