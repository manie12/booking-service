package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.BookingStatus;
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
@Table("booking_status_history")
public class BookingStatusHistoryEntity {

    @Id
    private UUID id;

    @Column("booking_id")
    private UUID bookingId;

    @Column("booking_item_id")
    private UUID bookingItemId;

    @Column("old_status")
    private BookingStatus oldStatus;

    @Column("new_status")
    private BookingStatus newStatus;

    @Column("actor_type")
    private ActorType actorType;

    @Column("actor_id")
    private String actorId;

    @Column("reason_code")
    private String reasonCode;

    @Column("notes")
    private String notes;

    @Column("created_at")
    private OffsetDateTime createdAt;
}