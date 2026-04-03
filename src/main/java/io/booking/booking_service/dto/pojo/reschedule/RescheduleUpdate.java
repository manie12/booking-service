package io.booking.booking_service.dto.pojo.reschedule;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CancellationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class RescheduleUpdate {

    private UUID newScheduleInstanceId;
    private OffsetDateTime newStartAt;
    private BigDecimal rescheduleFeeAmount;
    private String reasonCode;
    private String reasonText;
    private ActorType actorType;
    private String actorId;
    private CancellationStatus status;
    private OffsetDateTime completedAt;
}
