package io.booking.booking_service.dto.pojo.refunditem;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundItemUpdate {

    private Integer quantityRefunded;
    private BigDecimal refundAmount;
    private BigDecimal taxRefundAmount;
    private BigDecimal feeDeductionAmount;
    private String notes;
}
