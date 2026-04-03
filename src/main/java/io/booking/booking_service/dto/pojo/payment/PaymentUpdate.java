package io.booking.booking_service.dto.pojo.payment;

import io.booking.booking_service.datatype.booking.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class PaymentUpdate {

    private BigDecimal authorizedAmount;
    private BigDecimal capturedAmount;
    private BigDecimal refundedAmount;
    private PaymentStatus status;
    private OffsetDateTime initiatedAt;
    private OffsetDateTime completedAt;
}
