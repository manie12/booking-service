package io.booking.booking_service.dto.pojo.bookingstatushistory;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class BookingStatusHistoryRequest {

    @NotNull(message = "Booking ID is required")
    private UUID bookingId;

    private UUID bookingItemId;
    private BookingStatus oldStatus;

    @NotNull(message = "New status is required")
    private BookingStatus newStatus;

    private ActorType actorType;
    private String actorId;
    private String reasonCode;
    private String notes;
}
