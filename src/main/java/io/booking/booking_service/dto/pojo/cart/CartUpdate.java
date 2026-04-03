package io.booking.booking_service.dto.pojo.cart;

import io.booking.booking_service.datatype.booking.CartStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class CartUpdate {

    private CartStatus status;
    private OffsetDateTime expiresAt;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String notes;
}
