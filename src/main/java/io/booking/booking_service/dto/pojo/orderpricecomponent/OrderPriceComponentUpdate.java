package io.booking.booking_service.dto.pojo.orderpricecomponent;

import io.booking.booking_service.datatype.booking.PriceComponentType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderPriceComponentUpdate {

    private PriceComponentType componentType;
    private String componentName;
    private String sourceReference;
    private BigDecimal amount;
    private String currencyCode;
    private Integer sortOrder;
}
