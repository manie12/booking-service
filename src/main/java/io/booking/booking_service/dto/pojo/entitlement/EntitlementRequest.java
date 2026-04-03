package io.booking.booking_service.dto.pojo.entitlement;

import io.booking.booking_service.datatype.booking.EntitlementStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class EntitlementRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotBlank(message = "Entitlement number is required")
    private String entitlementNumber;

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Order item ID is required")
    private UUID orderItemId;

    private UUID bookingId;
    private UUID bookingItemId;
    private UUID guestProfileId;

    @NotBlank(message = "Entitlement type is required")
    private String entitlementType;

    private EntitlementStatus status;
    private OffsetDateTime issuedAt;
    private OffsetDateTime validFrom;
    private OffsetDateTime validTo;
    private Integer usageLimit;
    private Integer usageCount;
    private String notes;
}
