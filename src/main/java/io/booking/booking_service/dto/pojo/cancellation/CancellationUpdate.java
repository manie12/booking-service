package io.booking.booking_service.dto.pojo.cancellation;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CancellationStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class CancellationUpdate {

    private CancellationStatus status;
    private String reasonCode;
    private String reasonText;
    private String policyReference;
    private Boolean refundEligible;
    private BigDecimal refundAmountEstimate;
    private ActorType actorType;
    private String actorId;
    private OffsetDateTime completedAt;
}
