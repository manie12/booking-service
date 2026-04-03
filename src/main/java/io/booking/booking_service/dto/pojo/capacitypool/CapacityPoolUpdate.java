package io.booking.booking_service.dto.pojo.capacitypool;

import io.booking.booking_service.datatype.booking.CapacityPoolStatus;
import lombok.Data;

@Data
public class CapacityPoolUpdate {

    private String poolName;
    private Integer capacityTotal;
    private Integer capacityHeld;
    private Integer capacityBooked;
    private Integer capacityAvailable;
    private CapacityPoolStatus status;
}
