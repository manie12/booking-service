package io.booking.booking_service.dto.pojo.orderitem;

import io.booking.booking_service.datatype.booking.FulfillmentType;
import io.booking.booking_service.datatype.booking.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class OrderItemResponse {

    private UUID id;
    private UUID orderId;
    private Integer lineNumber;
    private UUID productId;
    private UUID productVariantId;
    private UUID offerId;
    private UUID priceRuleId;
    private String productCodeSnapshot;
    private String productNameSnapshot;
    private String variantCodeSnapshot;
    private String variantNameSnapshot;
    private String offerCodeSnapshot;
    private String offerNameSnapshot;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String currencyCode;
    private FulfillmentType fulfillmentType;
    private LocalDate serviceDate;
    private UUID scheduleInstanceId;
    private OrderStatus status;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
