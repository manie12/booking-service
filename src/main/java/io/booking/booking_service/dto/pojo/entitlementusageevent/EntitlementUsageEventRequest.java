package io.booking.booking_service.dto.pojo.entitlementusageevent;

import io.booking.booking_service.datatype.booking.UsageEventType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class EntitlementUsageEventRequest {

    @NotNull(message = "Entitlement ID is required")
    private UUID entitlementId;

    @NotNull(message = "Event type is required")
    private UsageEventType eventType;

    private Integer usageDelta;
    private OffsetDateTime eventAt;
    private String locationCode;
    private String referenceCode;
    private String payloadJson;
}
