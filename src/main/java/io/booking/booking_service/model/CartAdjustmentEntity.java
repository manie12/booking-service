package io.booking.booking_service.model;

import io.booking.booking_service.datatype.AdjustmentType;
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
@Table("cart_adjustments")
public class CartAdjustmentEntity {

    @Id
    private UUID id;

    @Column("cart_id")
    private UUID cartId;

    @Column("cart_item_id")
    private UUID cartItemId;

    @Column("adjustment_type")
    private AdjustmentType adjustmentType;

    @Column("adjustment_name")
    private String adjustmentName;

    @Column("amount")
    private BigDecimal amount;

    @Column("currency_code")
    private String currencyCode;

    @Column("source")
    private String source;

    @Column("reason_code")
    private String reasonCode;

    @Column("created_at")
    private OffsetDateTime createdAt;
}