package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.HoldStatus;
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
@Table("reservation_holds")
public class ReservationHoldEntity {

    @Id
    private UUID id;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("cart_id")
    private UUID cartId;

    @Column("order_id")
    private UUID orderId;

    @Column("schedule_instance_id")
    private UUID scheduleInstanceId;

    @Column("capacity_pool_id")
    private UUID capacityPoolId;

    @Column("hold_reference")
    private String holdReference;

    @Column("quantity_held")
    private Integer quantityHeld;

    @Column("status")
    private HoldStatus status;

    @Column("expires_at")
    private OffsetDateTime expiresAt;

    @Column("consumed_at")
    private OffsetDateTime consumedAt;

    @Column("released_at")
    private OffsetDateTime releasedAt;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}