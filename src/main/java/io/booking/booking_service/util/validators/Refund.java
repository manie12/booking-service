package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.RefundStatus;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.RefundEntity;
import io.booking.booking_service.repository.RefundRepository;
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
public class Refund {

    private final SharedUtils sharedUtils;
    private final RefundRepository repository;

    /** Load refund by id, fail with NOT_FOUND if missing */
    public Mono<RefundEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.REFUND_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.REFUND_NOT_FOUND)));
    }

    /** Ensure refund reference is unique; pass ignoreId=null on create */
    public Mono<Void> ensureUniqueReference(String refundReference, UUID ignoreId) {
        if (sharedUtils.isNullOrEmptyOrBlank(refundReference))
            return Mono.error(BookingException.of(BookingErrorType.REFUND_REQUEST_INVALID));
        return repository.findByRefundReference(refundReference).flatMap(e -> {
            if (ignoreId != null && ignoreId.equals(e.getId())) return Mono.empty();
            return Mono.error(BookingException.of(BookingErrorType.REFUND_ALREADY_EXISTS));
        }).then();
    }

    /** Validate order is linked */
    public Mono<Void> validateOrderRequired(UUID orderId) {
        if (orderId == null)
            return Mono.error(BookingException.of(BookingErrorType.REFUND_ORDER_REQUIRED));
        return Mono.empty();
    }

    /** Validate requested amount is > 0 */
    public Mono<Void> validateRequestedAmount(BigDecimal requestedAmount) {
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0)
            return Mono.error(BookingException.of(BookingErrorType.REFUND_AMOUNT_INVALID));
        return Mono.empty();
    }

    /** Validate refund amount does not exceed paid amount */
    public Mono<Void> validateDoesNotExceedPaid(BigDecimal refundAmount, BigDecimal paidAmount) {
        if (refundAmount != null && paidAmount != null && refundAmount.compareTo(paidAmount) > 0)
            return Mono.error(BookingException.of(BookingErrorType.REFUND_EXCEEDS_PAID_AMOUNT));
        return Mono.empty();
    }

    /** Assert refund has not already been completed */
    public Mono<RefundEntity> assertNotAlreadyCompleted(RefundEntity refund) {
        if (RefundStatus.COMPLETED.equals(refund.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.REFUND_ALREADY_COMPLETED));
        return Mono.just(refund);
    }

    /** Validate status is not null */
    public Mono<Void> validateStatusRequired(RefundStatus status) {
        if (status == null)
            return Mono.error(BookingException.of(BookingErrorType.REFUND_STATUS_INVALID));
        return Mono.empty();
    }

    /** Validate actor type is present */
    public Mono<Void> validateActorTypeRequired(ActorType actorType) {
        if (actorType == null)
            return Mono.error(BookingException.of(BookingErrorType.REFUND_REQUEST_INVALID));
        return Mono.empty();
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
