package io.booking.booking_service.dto.pojo.entitlement;

import io.booking.booking_service.datatype.booking.EntitlementStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class EntitlementResponse {

    private UUID id;
    private UUID tenantId;
    private String entitlementNumber;
    private UUID orderId;
    private UUID orderItemId;
    private UUID bookingId;
    private UUID bookingItemId;
    private UUID guestProfileId;
    private String entitlementType;
    private EntitlementStatus status;
    private OffsetDateTime issuedAt;
    private OffsetDateTime validFrom;
    private OffsetDateTime validTo;
    private Integer usageLimit;
    private Integer usageCount;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
