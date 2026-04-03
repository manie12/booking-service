package io.booking.booking_service.dto.pojo.reservationhold;

import io.booking.booking_service.datatype.booking.HoldStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ReservationHoldResponse {

    private UUID id;
    private UUID tenantId;
    private UUID cartId;
    private UUID orderId;
    private UUID scheduleInstanceId;
    private UUID capacityPoolId;
    private String holdReference;
    private Integer quantityHeld;
    private HoldStatus status;
    private OffsetDateTime expiresAt;
    private OffsetDateTime consumedAt;
    private OffsetDateTime releasedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
