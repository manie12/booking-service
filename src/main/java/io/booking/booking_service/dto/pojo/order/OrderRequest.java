package io.booking.booking_service.dto.pojo.order;

import io.booking.booking_service.datatype.booking.OrderStatus;
import io.booking.booking_service.datatype.booking.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class OrderRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotBlank(message = "Order number is required")
    @Size(max = 100)
    private String orderNumber;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull(message = "Cart ID is required")
    private UUID cartId;

    private UUID channelId;

    @Size(max = 10)
    private String countryCode;

    @NotBlank(message = "Currency code is required")
    @Size(max = 10)
    private String currencyCode;

    private OrderStatus status;
    private PaymentStatus paymentStatus;

    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    private OffsetDateTime placedAt;
    private String notes;
}
