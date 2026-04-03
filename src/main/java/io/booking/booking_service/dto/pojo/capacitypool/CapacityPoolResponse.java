package io.booking.booking_service.dto.pojo.capacitypool;

import io.booking.booking_service.datatype.booking.CapacityPoolStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CapacityPoolResponse {

    private UUID id;
    private UUID scheduleInstanceId;
    private String poolCode;
    private String poolName;
    private Integer capacityTotal;
    private Integer capacityHeld;
    private Integer capacityBooked;
    private Integer capacityAvailable;
    private CapacityPoolStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
