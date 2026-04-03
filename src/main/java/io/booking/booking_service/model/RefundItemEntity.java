package io.booking.booking_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("refund_items")
public class RefundItemEntity {

    @Id
    private UUID id;

    @Column("refund_id")
    private UUID refundId;

    @Column("order_item_id")
    private UUID orderItemId;

    @Column("quantity_refunded")
    private Integer quantityRefunded;

    @Column("refund_amount")
    private BigDecimal refundAmount;

    @Column("tax_refund_amount")
    private BigDecimal taxRefundAmount;

    @Column("fee_deduction_amount")
    private BigDecimal feeDeductionAmount;

    @Column("notes")
    private String notes;

    @Column("created_at")
    private OffsetDateTime createdAt;
}