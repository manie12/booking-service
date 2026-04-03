package io.booking.booking_service.dto.pojo.cartitemguest;

import io.booking.booking_service.datatype.booking.GuestType;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CartItemGuestResponse {

    private UUID id;
    private UUID cartItemId;
    private UUID guestProfileId;
    private String guestFirstName;
    private String guestLastName;
    private LocalDate guestDateOfBirth;
    private GuestType guestType;
    private String notes;
    private OffsetDateTime createdAt;
}
