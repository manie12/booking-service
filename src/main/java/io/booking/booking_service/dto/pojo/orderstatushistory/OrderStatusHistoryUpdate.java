package io.booking.booking_service.dto.pojo.orderstatushistory;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.OrderStatus;
import lombok.Data;

@Data
public class OrderStatusHistoryUpdate {

    private OrderStatus oldStatus;
    private OrderStatus newStatus;
    private ActorType actorType;
    private String actorId;
    private String reasonCode;
    private String notes;
}
