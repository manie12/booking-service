package io.booking.booking_service.dto.pojo.bookingguest;

import io.booking.booking_service.datatype.booking.BookingStatus;
import io.booking.booking_service.datatype.booking.GuestType;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingGuestUpdate {

    private String guestFirstName;
    private String guestLastName;
    private LocalDate guestDateOfBirth;
    private GuestType guestType;
    private BookingStatus status;
    private String notes;
}
