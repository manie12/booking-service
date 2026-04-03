package io.booking.booking_service.dto.pojo.scheduleinstance;

import io.booking.booking_service.datatype.booking.ScheduleInstanceStatus;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ScheduleInstanceUpdate {

    private String instanceName;
    private OffsetDateTime startAt;
    private OffsetDateTime endAt;
    private String timezone;
    private String venueCode;
    private String resourceReference;
    private ScheduleInstanceStatus status;
}
