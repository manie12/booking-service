package io.booking.booking_service.dto.pojo.cart;

import io.booking.booking_service.datatype.booking.CartStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CartResponse {

    private UUID id;
    private UUID tenantId;
    private String cartNumber;
    private UUID customerId;
    private String sessionId;
    private UUID channelId;
    private String countryCode;
    private String currencyCode;
    private CartStatus status;
    private OffsetDateTime expiresAt;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
