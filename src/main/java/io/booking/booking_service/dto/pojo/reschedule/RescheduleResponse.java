package io.booking.booking_service.dto.pojo.reschedule;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CancellationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class RescheduleResponse {

    private UUID id;
    private UUID tenantId;
    private UUID bookingId;
    private UUID bookingItemId;
    private UUID oldScheduleInstanceId;
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
    private OffsetDateTime createdAt;
}
