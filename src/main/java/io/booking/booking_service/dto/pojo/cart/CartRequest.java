package io.booking.booking_service.dto.pojo.cart;

import io.booking.booking_service.datatype.booking.CartStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CartRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotBlank(message = "Cart number is required")
    @Size(max = 100)
    private String cartNumber;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @Size(max = 100)
    private String sessionId;

    private UUID channelId;

    @Size(max = 10)
    private String countryCode;

    @NotBlank(message = "Currency code is required")
    @Size(max = 10)
    private String currencyCode;

    private CartStatus status;

    private OffsetDateTime expiresAt;

    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

    private String notes;
}
