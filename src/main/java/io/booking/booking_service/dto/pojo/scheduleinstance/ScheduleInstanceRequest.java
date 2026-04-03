package io.booking.booking_service.dto.pojo.scheduleinstance;

import io.booking.booking_service.datatype.booking.ScheduleInstanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ScheduleInstanceRequest {

    @NotNull(message = "Tenant ID is required")
    private UUID tenantId;

    @NotNull(message = "Product ID is required")
    private UUID productId;

    private UUID productVariantId;

    @NotBlank(message = "Instance code is required")
    private String instanceCode;

    private String instanceName;

    @NotNull(message = "Start time is required")
    private OffsetDateTime startAt;

    @NotNull(message = "End time is required")
    private OffsetDateTime endAt;

    @NotBlank(message = "Timezone is required")
    private String timezone;

    private String venueCode;
    private String resourceReference;
    private ScheduleInstanceStatus status;
}
