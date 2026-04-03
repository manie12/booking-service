package io.booking.booking_service.dto.pojo.entitlement;

import io.booking.booking_service.datatype.booking.EntitlementStatus;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class EntitlementUpdate {

    private EntitlementStatus status;
    private OffsetDateTime validFrom;
    private OffsetDateTime validTo;
    private Integer usageLimit;
    private Integer usageCount;
    private String notes;
}
