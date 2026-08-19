package io.booking.booking_service.dto.pojo.guestprofile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class GuestProfileRequest {

    private UUID tenantId;

    @NotBlank(message = "Customer ID is required")
    @Size(max = 100)
    private String customerId;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @Size(max = 200)
    private String fullName;

    private LocalDate dateOfBirth;

    @Size(max = 20)
    private String gender;

    @Size(max = 10)
    private String nationalityCode;

    @Size(max = 50)
    private String documentType;

    @Size(max = 100)
    private String documentNumber;

    @Size(max = 200)
    private String email;

    @Size(max = 50)
    private String phoneNumber;

    private String specialRequirements;
}
