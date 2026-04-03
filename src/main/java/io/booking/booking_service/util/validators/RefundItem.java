package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.RefundItemEntity;
import io.booking.booking_service.repository.RefundItemRepository;
import io.booking.booking_service.util.SharedUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundItem {

    private final SharedUtils sharedUtils;
    private final RefundItemRepository repository;

    /** Load refund item by id, fail with NOT_FOUND if missing */
    public Mono<RefundItemEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.REFUND_ITEM_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.REFUND_ITEM_NOT_FOUND)));
    }

    /** Validate refund id is present */
    public Mono<Void> validateRefundRequired(UUID refundId) {
        if (refundId == null)
            return Mono.error(BookingException.of(BookingErrorType.REFUND_NOT_FOUND));
        return Mono.empty();
    }

    /** Validate order item id is present */
    public Mono<Void> validateOrderItemRequired(UUID orderItemId) {
        if (orderItemId == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_ITEM_NOT_FOUND));
        return Mono.empty();
    }

    /** Validate quantity refunded is >= 1 */
    public Mono<Void> validateQuantityRefunded(Integer quantityRefunded) {
        if (quantityRefunded == null || quantityRefunded < 1)
            return Mono.error(BookingException.of(BookingErrorType.REFUND_ITEM_QUANTITY_INVALID));
        return Mono.empty();
    }

    /** Validate refund amount is >= 0 */
    public Mono<Void> validateRefundAmount(BigDecimal refundAmount) {
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) < 0)
            return Mono.error(BookingException.of(BookingErrorType.REFUND_ITEM_AMOUNT_INVALID));
        return Mono.empty();
    }

    /** Assert refund item belongs to the given refund */
    public Mono<RefundItemEntity> assertBelongsToRefund(RefundItemEntity item, UUID refundId) {
        if (!refundId.equals(item.getRefundId()))
            return Mono.error(BookingException.of(BookingErrorType.REFUND_ITEM_REQUEST_INVALID));
        return Mono.just(item);
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
