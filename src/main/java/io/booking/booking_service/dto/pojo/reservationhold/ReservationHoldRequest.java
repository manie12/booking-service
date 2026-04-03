package io.booking.booking_service.dto.pojo.reservationhold;

import io.booking.booking_service.datatype.booking.HoldStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ReservationHoldRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Cart ID is required")
    private UUID cartId;

    private UUID orderId;

    @NotNull(message = "Schedule instance ID is required")
    private UUID scheduleInstanceId;

    @NotNull(message = "Capacity pool ID is required")
    private UUID capacityPoolId;

    private String holdReference;

    @NotNull(message = "Quantity held is required")
    @Min(value = 1, message = "Quantity held must be at least 1")
    private Integer quantityHeld;

    private HoldStatus status;

    @NotNull(message = "Expiry time is required")
    private OffsetDateTime expiresAt;
}
