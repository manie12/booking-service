package io.booking.booking_service.dto.pojo.refund;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.RefundStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class RefundUpdate {

    private RefundStatus status;
    private BigDecimal approvedAmount;
    private BigDecimal refundedAmount;
    private String reasonCode;
    private String reasonText;
    private ActorType actorType;
    private String actorId;
    private OffsetDateTime completedAt;
}
