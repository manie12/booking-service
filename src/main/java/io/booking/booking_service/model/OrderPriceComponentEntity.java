package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.PriceComponentType;
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
@Table("order_price_components")
public class OrderPriceComponentEntity {

    @Id
    private UUID id;

    @Column("order_item_id")
    private UUID orderItemId;

    @Column("component_type")
    private PriceComponentType componentType;

    @Column("component_name")
    private String componentName;

    @Column("source_reference")
    private String sourceReference;

    @Column("amount")
    private BigDecimal amount;

    @Column("currency_code")
    private String currencyCode;

    @Column("sort_order")
    private Integer sortOrder;

    @Column("created_at")
    private OffsetDateTime createdAt;
}