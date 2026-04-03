package io.booking.booking_service.dto.pojo.bookingstatushistory;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.BookingStatus;
import lombok.Data;

@Data
public class BookingStatusHistoryUpdate {

    private BookingStatus oldStatus;
    private BookingStatus newStatus;
    private ActorType actorType;
    private String actorId;
    private String reasonCode;
    private String notes;
}
