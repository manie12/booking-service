package io.booking.booking_service.dto.pojo.cancellation;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CancellationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CancellationResponse {

    private UUID id;
    private UUID tenantId;
    private UUID orderId;
    private UUID orderItemId;
    private UUID bookingId;
    private UUID bookingItemId;
    private UUID customerId;
    private CancellationStatus status;
    private String reasonCode;
    private String reasonText;
    private String policyReference;
    private Boolean refundEligible;
    private BigDecimal refundAmountEstimate;
    private ActorType actorType;
    private String actorId;
    private OffsetDateTime requestedAt;
    private OffsetDateTime completedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
