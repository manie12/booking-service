package io.booking.booking_service.dto.pojo.capacitypool;

import io.booking.booking_service.datatype.booking.CapacityPoolStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CapacityPoolRequest {

    @NotNull(message = "Schedule instance ID is required")
    private UUID scheduleInstanceId;

    @NotBlank(message = "Pool code is required")
    private String poolCode;

    private String poolName;

    @NotNull(message = "Total capacity is required")
    @Min(value = 1, message = "Total capacity must be at least 1")
    private Integer capacityTotal;

    private Integer capacityHeld;
    private Integer capacityBooked;
    private Integer capacityAvailable;
    private CapacityPoolStatus status;
}
