package io.booking.booking_service.dto.pojo.cartitemguest;

import io.booking.booking_service.datatype.booking.GuestType;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CartItemGuestUpdate {

    private UUID guestProfileId;
    private String guestFirstName;
    private String guestLastName;
    private LocalDate guestDateOfBirth;
    private GuestType guestType;
    private String notes;
}
