package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.FulfillmentType;
import io.booking.booking_service.datatype.booking.OrderStatus;
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
@Table("order_items")
public class OrderItemEntity {

    @Id
    private UUID id;

    @Column("order_id")
    private UUID orderId;

    @Column("line_number")
    private Integer lineNumber;

    @Column("product_id")
    private UUID productId;

    @Column("product_variant_id")
    private UUID productVariantId;

    @Column("offer_id")
    private UUID offerId;

    @Column("price_rule_id")
    private UUID priceRuleId;

    @Column("product_code_snapshot")
    private String productCodeSnapshot;

    @Column("product_name_snapshot")
    private String productNameSnapshot;

    @Column("variant_code_snapshot")
    private String variantCodeSnapshot;

    @Column("variant_name_snapshot")
    private String variantNameSnapshot;

    @Column("offer_code_snapshot")
    private String offerCodeSnapshot;

    @Column("offer_name_snapshot")
    private String offerNameSnapshot;

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

    @Column("schedule_instance_id")
    private UUID scheduleInstanceId;

    @Column("status")
    private OrderStatus status;

    @Column("notes")
    private String notes;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}