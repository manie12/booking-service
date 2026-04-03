package io.booking.booking_service.dto.pojo.cartitem;

import io.booking.booking_service.datatype.booking.FulfillmentType;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CartItemUpdate {

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private BigDecimal unitPrice;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private FulfillmentType fulfillmentType;
    private LocalDate serviceDate;
    private String notes;
    private Integer sortOrder;
}
