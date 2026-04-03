package io.booking.booking_service.model;

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
@Table("booking_items")
public class BookingItemEntity {

    @Id
    private UUID id;

    @Column("booking_id")
    private UUID bookingId;

    @Column("order_item_id")
    private UUID orderItemId;

    @Column("schedule_instance_id")
    private UUID scheduleInstanceId;

    @Column("capacity_pool_id")
    private UUID capacityPoolId;

    @Column("product_id")
    private UUID productId;

    @Column("product_variant_id")
    private UUID productVariantId;

    @Column("offer_id")
    private UUID offerId;

    @Column("quantity")
    private Integer quantity;

    @Column("unit_count")
    private Integer unitCount;

    @Column("start_at")
    private OffsetDateTime startAt;

    @Column("end_at")
    private OffsetDateTime endAt;

    @Column("status")
    private BookingStatus status;

    @Column("notes")
    private String notes;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}