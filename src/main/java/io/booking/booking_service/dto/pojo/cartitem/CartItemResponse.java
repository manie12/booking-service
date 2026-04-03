package io.booking.booking_service.dto.pojo.cartitem;

import io.booking.booking_service.datatype.booking.FulfillmentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CartItemResponse {

    private UUID id;
    private UUID cartId;
    private UUID productId;
    private UUID productVariantId;
    private UUID offerId;
    private UUID priceRuleId;
    private UUID scheduleInstanceId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String currencyCode;
    private FulfillmentType fulfillmentType;
    private LocalDate serviceDate;
    private String notes;
    private Integer sortOrder;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
