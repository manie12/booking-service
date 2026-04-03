package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CancellationStatus;
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
@Table("cancellations")
public class CancellationEntity {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("order_id")
    private UUID orderId;

    @Column("order_item_id")
    private UUID orderItemId;

    @Column("booking_id")
    private UUID bookingId;

    @Column("booking_item_id")
    private UUID bookingItemId;

    @Column("customer_id")
    private UUID customerId;

    @Column("status")
    private CancellationStatus status;

    @Column("reason_code")
    private String reasonCode;

    @Column("reason_text")
    private String reasonText;

    @Column("policy_reference")
    private String policyReference;

    @Column("refund_eligible")
    private Boolean refundEligible;

    @Column("refund_amount_estimate")
    private BigDecimal refundAmountEstimate;

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