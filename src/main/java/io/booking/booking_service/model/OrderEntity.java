package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.OrderStatus;
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
@Table("orders")
public class OrderEntity {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("order_number")
    private String orderNumber;

    @Column("customer_id")
    private UUID customerId;

    @Column("cart_id")
    private UUID cartId;

    @Column("channel_id")
    private UUID channelId;

    @Column("country_code")
    private String countryCode;

    @Column("currency_code")
    private String currencyCode;

    @Column("status")
    private OrderStatus status;

    @Column("payment_status")
    private PaymentStatus paymentStatus;

    @Column("subtotal_amount")
    private BigDecimal subtotalAmount;

    @Column("discount_amount")
    private BigDecimal discountAmount;

    @Column("tax_amount")
    private BigDecimal taxAmount;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("paid_amount")
    private BigDecimal paidAmount;

    @Column("balance_amount")
    private BigDecimal balanceAmount;

    @Column("idempotency_key")
    private String idempotencyKey;

    @Column("placed_at")
    private OffsetDateTime placedAt;

    @Column("notes")
    private String notes;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}