package io.booking.booking_service.dto.pojo.orderstatushistory;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderStatusHistoryRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    private OrderStatus oldStatus;

    @NotNull(message = "New status is required")
    private OrderStatus newStatus;

    private ActorType actorType;
    private String actorId;
    private String reasonCode;
    private String notes;
}
