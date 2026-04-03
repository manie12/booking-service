package io.booking.booking_service.dto.pojo.bookingstatushistory;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.BookingStatus;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class BookingStatusHistoryResponse {

    private UUID id;
    private UUID bookingId;
    private UUID bookingItemId;
    private BookingStatus oldStatus;
    private BookingStatus newStatus;
    private ActorType actorType;
    private String actorId;
    private String reasonCode;
    private String notes;
    private OffsetDateTime createdAt;
}
