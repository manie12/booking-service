package io.booking.booking_service.dto.pojo.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerUpdate {

    @Size(max = 200)
    private String externalReference;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Size(max = 200)
    private String fullName;

    @Email(message = "Invalid email format")
    @Size(max = 200)
    private String email;

    @Size(max = 50)
    private String phoneNumber;

    @Size(max = 10)
    private String countryCode;

    private String status;
}
