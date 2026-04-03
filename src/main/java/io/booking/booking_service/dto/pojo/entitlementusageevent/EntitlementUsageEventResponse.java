package io.booking.booking_service.dto.pojo.entitlementusageevent;

import io.booking.booking_service.datatype.booking.UsageEventType;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class EntitlementUsageEventResponse {

    private UUID id;
    private UUID entitlementId;
    private UsageEventType eventType;
    private Integer usageDelta;
    private OffsetDateTime eventAt;
    private String locationCode;
    private String referenceCode;
    private String payloadJson;
    private OffsetDateTime createdAt;
}
