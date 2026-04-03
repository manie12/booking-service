package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.PaymentStatus;
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
@Table("payments")
public class PaymentEntity {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("order_id")
    private UUID orderId;

    @Column("payment_reference")
    private String paymentReference;

    @Column("payment_method")
    private String paymentMethod;

    @Column("provider_name")
    private String providerName;

    @Column("currency_code")
    private String currencyCode;

    @Column("requested_amount")
    private BigDecimal requestedAmount;

    @Column("authorized_amount")
    private BigDecimal authorizedAmount;

    @Column("captured_amount")
    private BigDecimal capturedAmount;

    @Column("refunded_amount")
    private BigDecimal refundedAmount;

    @Column("status")
    private PaymentStatus status;

    @Column("initiated_at")
    private OffsetDateTime initiatedAt;

    @Column("completed_at")
    private OffsetDateTime completedAt;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}