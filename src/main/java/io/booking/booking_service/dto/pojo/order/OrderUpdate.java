package io.booking.booking_service.dto.pojo.order;

import io.booking.booking_service.datatype.booking.OrderStatus;
import io.booking.booking_service.datatype.booking.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class OrderUpdate {

    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;
    private OffsetDateTime placedAt;
    private String notes;
}
