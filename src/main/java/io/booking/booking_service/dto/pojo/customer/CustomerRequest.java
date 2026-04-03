package io.booking.booking_service.dto.pojo.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CustomerRequest {

    @NotBlank(message = "Tenant ID is required")
    private UUID tenantId;

    @NotBlank(message = "Customer number is required")
    @Size(max = 100)
    private String customerNumber;

    @Size(max = 200)
    private String externalReference;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
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
