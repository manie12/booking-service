package io.booking.booking_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("guest_profiles")
public class GuestProfileEntity {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("customer_id")
    private UUID customerId;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("full_name")
    private String fullName;

    @Column("date_of_birth")
    private LocalDate dateOfBirth;

    @Column("gender")
    private String gender;

    @Column("nationality_code")
    private String nationalityCode;

    @Column("document_type")
    private String documentType;

    @Column("document_number")
    private String documentNumber;

    @Column("email")
    private String email;

    @Column("phone_number")
    private String phoneNumber;

    @Column("special_requirements")
    private String specialRequirements;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}