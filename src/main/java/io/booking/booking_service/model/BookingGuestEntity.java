package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.BookingStatus;
import io.booking.booking_service.datatype.booking.GuestType;
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
@Table("booking_guests")
public class BookingGuestEntity {

    @Id
    private UUID id;

    @Column("booking_item_id")
    private UUID bookingItemId;

    @Column("order_item_guest_id")
    private UUID orderItemGuestId;

    @Column("guest_profile_id")
    private UUID guestProfileId;

    @Column("guest_first_name")
    private String guestFirstName;

    @Column("guest_last_name")
    private String guestLastName;

    @Column("guest_date_of_birth")
    private LocalDate guestDateOfBirth;

    @Column("guest_type")
    private GuestType guestType;

    @Column("status")
    private BookingStatus status;

    @Column("notes")
    private String notes;

    @Column("created_at")
    private OffsetDateTime createdAt;
}