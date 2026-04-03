package io.booking.booking_service.dto.pojo.orderpricecomponent;

import io.booking.booking_service.datatype.booking.PriceComponentType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OrderPriceComponentRequest {

    @NotNull(message = "Order item ID is required")
    private UUID orderItemId;

    @NotNull(message = "Component type is required")
    private PriceComponentType componentType;

    private String componentName;
    private String sourceReference;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    private String currencyCode;
    private Integer sortOrder;
}
