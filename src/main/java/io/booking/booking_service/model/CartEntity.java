package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.CartStatus;
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
@Table("carts")
public class CartEntity {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("cart_number")
    private String cartNumber;

    @Column("customer_id")
    private UUID customerId;

    @Column("session_id")
    private String sessionId;

    @Column("channel_id")
    private UUID channelId;

    @Column("country_code")
    private String countryCode;

    @Column("currency_code")
    private String currencyCode;

    @Column("status")
    private CartStatus status;

    @Column("expires_at")
    private OffsetDateTime expiresAt;

    @Column("subtotal_amount")
    private BigDecimal subtotalAmount;

    @Column("discount_amount")
    private BigDecimal discountAmount;

    @Column("tax_amount")
    private BigDecimal taxAmount;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("notes")
    private String notes;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}