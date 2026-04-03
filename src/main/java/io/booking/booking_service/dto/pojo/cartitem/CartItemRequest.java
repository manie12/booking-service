package io.booking.booking_service.dto.pojo.cartitem;
import io.booking.booking_service.datatype.booking.FulfillmentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
@Data
public class CartItemRequest {
    @NotNull(message = "Cart ID is required")
    private UUID cartId;
    @NotNull(message = "Product ID is required")
    private UUID productId;
    private UUID productVariantId;
    private UUID offerId;
    private UUID priceRuleId;
    private UUID scheduleInstanceId;
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecpackage io.booking.booking_service.dto.pojo.cartitecoimport io.booking.booking_service.datatype.booking.Finimport jakarta.validation.constraints.Min;
import jakarta.validatiriimport jakarta.validation.constraints.Nottrimport jakarta.validation.constraints.Size}
EOFimport lombok.Data;
import java.math.BigDevaimport java.math.B iimport java.time.LocalDate;toimport java.util.UUID;
@Dabo@Data
public class Ca.dpublyp    @NotNull(message = "Cart po    private UUID cartId;
    @NotNull(messag
i    @NotNull(message = te    private UUID productId;
    priimport java.ut    private UUID productVas     private UUID offerId;
    priID    private UUID priceRurt    private UUID scheduleInsId    @NotNull(message = "Quantity is;
    @Min(value = 1, message = "Quantity must ic    private Integer quantity;
    private BigDecimal unitPrnt    private BigDecimal unitPig    private BigDecimal subtotalAig    private BigDecimal discountAmountig    private BigDecimal taxAmount;
   ig    private BigDecpackage io.booigimport jakarta.validatiriimport jakarta.validation.constraints.Nottrimport jakarta.validation.constraints.Size}
EOFimport lombok.Data;
import java.math.BigDevaimport ivEOFimport lombok.Data;
import java.math.BigDevaimport java.math.B iimport java.time.LocalDate;toimport java.ut "import java.math.BigDem@Dabo@Data
public class Ca.dpublyp    @NotNull(message = "Cart po    private UUID cartId;
    bopublic clvi    @NotNull(messag
i    @NotNull(message = te    private UUID productId;
   n;i    @NotNull(messa;    priimport java.ut    private UUID productVas    Da    priID    private UUID priceRurt    private UUID scheduleInsId    @Notan    @Min(value = 1, message = "Quantity must ic    private Integer quantity;
    private BigDecimal e     private BigDecimal unitPrnt    private BigDecimal unitPig    private Bite   ig    private BigDecpackage io.booigimport jakarta.validatiriimport jakarta.validation.constraints.Nottrimport jakarta.validation.constraints.Size}
EOFimport lombo  EOFimport lombok.Data;
import java.math.BigDevaimport iBASE=/Users/manietempah/Workspace/booking-service/src/main/java/io/booking/booking_service/dto/pojo
# CartItemGuest
cat > "$BASE/cartitemguest/CartItemGuestRequest.java" << 'EOF'
package io.booking.booking_service.dto.pojo.cartitemguest;
import io.booking.booking_service.datatype.booking.GuestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;
@Data
public class CartItemGuestRequest {
    @NotNull(message = "Cart item ID is required")
    private UUID cartItemId;
    private UUID guestProfileId;
    @NotBlank(message = "Guest first name is required")
    private String guestFirstName;
    @NotBlank(message = "Guest last name is required")
    private String guestLastName;
    private LocalDate guestDateOfBirth;
    @NotNull(message = "Guest type is required")
    private GuestType guestType;
    private String notes;
}
