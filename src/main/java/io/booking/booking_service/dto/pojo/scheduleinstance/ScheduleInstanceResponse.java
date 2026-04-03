package io.booking.booking_service.dto.pojo.scheduleinstance;

import io.booking.booking_service.datatype.booking.ScheduleInstanceStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ScheduleInstanceResponse {

    private UUID id;
    private UUID tenantId;
    private UUID productId;
    private UUID productVariantId;
    private String instanceCode;
    private String instanceName;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private String timezone;
    private String venueCode;
    private String resourceReference;
    private ScheduleInstanceStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
