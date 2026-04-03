package io.booking.booking_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("customers")
public class CustomerEntity {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("customer_number")
    private String customerNumber;

    @Column("external_reference")
    private String externalReference;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("full_name")
    private String fullName;

    @Column("email")
    private String email;

    @Column("phone_number")
    private String phoneNumber;

    @Column("country_code")
    private String countryCode;

    @Column("status")
    private String status;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}