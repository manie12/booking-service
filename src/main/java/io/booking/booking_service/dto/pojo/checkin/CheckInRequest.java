package io.booking.booking_service.dto.pojo.checkin;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CheckInResultStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CheckInRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Entitlement ID is required")
    private UUID entitlementId;

    private UUID bookingId;
    private UUID bookingItemId;
    private UUID guestProfileId;
    private OffsetDateTime checkInAt;

    @NotBlank(message = "Location code is required")
    private String locationCode;

    @NotBlank(message = "Device code is required")
    private String deviceCode;

    private ActorType actorType;
    private String actorId;
    private CheckInResultStatus resultStatus;
    private String notes;
}
