package io.booking.booking_service.dto.pojo.bookingguest;

import io.booking.booking_service.datatype.booking.BookingStatus;
import io.booking.booking_service.datatype.booking.GuestType;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class BookingGuestResponse {

    private UUID id;
    private UUID bookingItemId;
    private UUID orderItemGuestId;
    private UUID guestProfileId;
    private String guestFirstName;
    private String guestLastName;
    private LocalDate guestDateOfBirth;
    private GuestType guestType;
    private BookingStatus status;
    private String notes;
    private OffsetDateTime createdAt;
}
