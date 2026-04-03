package io.booking.booking_service.dto.pojo.debezium;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DebeziumEnvelope<T> {

    @JsonProperty("before")
    private T before;

    @JsonProperty("after")
    private T after;

    /**
     * c = create, u = update, d = delete, r = snapshot read
     */
    @JsonProperty("op")
    private String op;

    @JsonProperty("ts_ms")
    private Long tsMs;

    public boolean isCreateOrSnapshot() {
        return "c".equals(op) || "r".equals(op);
    }
}
