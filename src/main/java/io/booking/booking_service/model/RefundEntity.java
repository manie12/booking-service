package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.RefundStatus;
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
@Table("refunds")
public class RefundEntity {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("order_id")
    private UUID orderId;

    @Column("payment_id")
    private UUID paymentId;

    @Column("refund_reference")
    private String refundReference;

    @Column("status")
    private RefundStatus status;

    @Column("currency_code")
    private String currencyCode;

    @Column("requested_amount")
    private BigDecimal requestedAmount;

    @Column("approved_amount")
    private BigDecimal approvedAmount;

    @Column("refunded_amount")
    private BigDecimal refundedAmount;

    @Column("reason_code")
    private String reasonCode;

    @Column("reason_text")
    private String reasonText;

    @Column("actor_type")
    private ActorType actorType;

    @Column("actor_id")
    private String actorId;

    @Column("requested_at")
    private OffsetDateTime requestedAt;

    @Column("completed_at")
    private OffsetDateTime completedAt;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}