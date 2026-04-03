package io.booking.booking_service.dto.pojo.orderitem;

import io.booking.booking_service.datatype.booking.FulfillmentType;
import io.booking.booking_service.datatype.booking.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class OrderItemUpdate {

    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private FulfillmentType fulfillmentType;
    private LocalDate serviceDate;
    private UUID scheduleInstanceId;
    private OrderStatus status;
    private String notes;
}
