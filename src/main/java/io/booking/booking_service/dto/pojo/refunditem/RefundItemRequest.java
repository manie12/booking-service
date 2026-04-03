package io.booking.booking_service.dto.pojo.refunditem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class RefundItemRequest {

    @NotNull(message = "Refund ID is required")
    private UUID refundId;

    @NotNull(message = "Order item ID is required")
    private UUID orderItemId;

    @NotNull(message = "Quantity refunded is required")
    @Min(value = 1, message = "Quantity refunded must be at least 1")
    private Integer quantityRefunded;

    @NotNull(message = "Refund amount is required")
    private BigDecimal refundAmount;

    private BigDecimal taxRefundAmount;
    private BigDecimal feeDeductionAmount;
    private String notes;
}
