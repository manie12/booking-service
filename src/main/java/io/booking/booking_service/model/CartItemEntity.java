package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.FulfillmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("cart_items")
public class CartItemEntity {

    @Id
    private UUID id;

    @Column("cart_id")
    private UUID cartId;

    @Column("product_id")
    private UUID productId;

    @Column("product_variant_id")
    private UUID productVariantId;

    @Column("offer_id")
    private UUID offerId;

    @Column("price_rule_id")
    private UUID priceRuleId;

    @Column("schedule_instance_id")
    private UUID scheduleInstanceId;

    @Column("quantity")
    private Integer quantity;

    @Column("unit_price")
    private BigDecimal unitPrice;

    @Column("subtotal_amount")
    private BigDecimal subtotalAmount;

    @Column("discount_amount")
    private BigDecimal discountAmount;

    @Column("tax_amount")
    private BigDecimal taxAmount;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("currency_code")
    private String currencyCode;

    @Column("fulfillment_type")
    private FulfillmentType fulfillmentType;

    @Column("service_date")
    private LocalDate serviceDate;

    @Column("notes")
    private String notes;

    @Column("sort_order")
    private Integer sortOrder;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}