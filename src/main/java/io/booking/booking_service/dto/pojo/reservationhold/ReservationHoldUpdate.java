package io.booking.booking_service.dto.pojo.reservationhold;

import io.booking.booking_service.datatype.booking.HoldStatus;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class ReservationHoldUpdate {

    private HoldStatus status;
    private OffsetDateTime expiresAt;
    private OffsetDateTime consumedAt;
    private OffsetDateTime releasedAt;
}
