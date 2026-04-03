package io.booking.booking_service.dto.pojo.checkin;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CheckInResultStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CheckInResponse {

    private UUID id;
    private UUID tenantId;
    private UUID entitlementId;
    private UUID bookingId;
    private UUID bookingItemId;
    private UUID guestProfileId;
    private OffsetDateTime checkInAt;
    private String locationCode;
    private String deviceCode;
    private ActorType actorType;
    private String actorId;
    private CheckInResultStatus resultStatus;
    private String notes;
    private OffsetDateTime createdAt;
}
