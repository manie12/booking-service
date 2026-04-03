package io.booking.booking_service.dto.pojo.order;

import io.booking.booking_service.datatype.booking.OrderStatus;
import io.booking.booking_service.datatype.booking.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class OrderResponse {

    private UUID id;
    private UUID tenantId;
    private String orderNumber;
    private UUID customerId;
    private UUID cartId;
    private UUID channelId;
    private String countryCode;
    private String currencyCode;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;
    private String idempotencyKey;
    private OffsetDateTime placedAt;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
