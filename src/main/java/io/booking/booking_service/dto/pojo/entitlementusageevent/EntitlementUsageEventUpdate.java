package io.booking.booking_service.dto.pojo.entitlementusageevent;

import io.booking.booking_service.datatype.booking.UsageEventType;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class EntitlementUsageEventUpdate {

    private UsageEventType eventType;
    private Integer usageDelta;
    private OffsetDateTime eventAt;
    private String locationCode;
    private String referenceCode;
    private String payloadJson;
}
