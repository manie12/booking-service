package io.booking.booking_service.dto.pojo.reschedule;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CancellationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class RescheduleRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Booking ID is required")
    private UUID bookingId;

    private UUID bookingItemId;

    @NotNull(message = "Old schedule instance ID is required")
    private UUID oldScheduleInstanceId;

    @NotNull(message = "New schedule instance ID is required")
    private UUID newScheduleInstanceId;

    private OffsetDateTime oldStartAt;
    private OffsetDateTime newStartAt;
    private BigDecimal rescheduleFeeAmount;
    private String currencyCode;
    private String reasonCode;
    private String reasonText;
    private ActorType actorType;
    private String actorId;
    private CancellationStatus status;
    private OffsetDateTime requestedAt;
    private OffsetDateTime completedAt;
}
