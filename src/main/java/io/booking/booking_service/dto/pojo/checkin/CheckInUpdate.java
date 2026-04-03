package io.booking.booking_service.dto.pojo.checkin;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CheckInResultStatus;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CheckInUpdate {

    private OffsetDateTime checkInAt;
    private String locationCode;
    private String deviceCode;
    private ActorType actorType;
    private String actorId;
    private CheckInResultStatus resultStatus;
    private String notes;
}
