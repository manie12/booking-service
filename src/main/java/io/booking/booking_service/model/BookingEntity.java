package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.BookingStatus;
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
@Table("bookings")
public class BookingEntity {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("booking_number")
    private String bookingNumber;

    @Column("order_id")
    private UUID orderId;

    @Column("customer_id")
    private UUID customerId;

    @Column("channel_id")
    private UUID channelId;

    @Column("status")
    private BookingStatus status;

    @Column("booking_date")
    private LocalDate bookingDate;

    @Column("currency_code")
    private String currencyCode;

    @Column("total_booked_quantity")
    private Integer totalBookedQuantity;

    @Column("notes")
    private String notes;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}