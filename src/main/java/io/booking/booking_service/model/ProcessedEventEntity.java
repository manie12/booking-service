package io.booking.booking_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Table("processed_event")
public class ProcessedEventEntity {

    @Id
    private String id;
    private OffsetDateTime processedAt;

    public ProcessedEventEntity() {
    }

    public ProcessedEventEntity(String id, OffsetDateTime processedAt) {
        this.id = id;
        this.processedAt = processedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(OffsetDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
