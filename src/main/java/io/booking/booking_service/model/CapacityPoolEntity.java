package io.booking.booking_service.model;

import io.booking.booking_service.datatype.booking.CapacityPoolStatus;
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
@Table("capacity_pools")
public class CapacityPoolEntity {

    @Id
    private UUID id;

    @Column("schedule_instance_id")
    private UUID scheduleInstanceId;

    @Column("pool_code")
    private String poolCode;

    @Column("pool_name")
    private String poolName;

    @Column("capacity_total")
    private Integer capacityTotal;

    @Column("capacity_held")
    private Integer capacityHeld;

    @Column("capacity_booked")
    private Integer capacityBooked;

    @Column("capacity_available")
    private Integer capacityAvailable;

    @Column("status")
    private CapacityPoolStatus status;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}