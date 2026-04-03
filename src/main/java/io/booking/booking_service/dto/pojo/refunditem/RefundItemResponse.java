package io.booking.booking_service.dto.pojo.refunditem;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class RefundItemResponse {

    private UUID id;
    private UUID refundId;
    private UUID orderItemId;
    private Integer quantityRefunded;
    private BigDecimal refundAmount;
    private BigDecimal taxRefundAmount;
    private BigDecimal feeDeductionAmount;
    private String notes;
    private OffsetDateTime createdAt;
}
