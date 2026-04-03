package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.PaymentStatus;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.PaymentEntity;
import io.booking.booking_service.repository.PaymentRepository;
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
public class Payment {

    private final SharedUtils sharedUtils;
    private final PaymentRepository repository;

    /** Load payment by id, fail with NOT_FOUND if missing */
    public Mono<PaymentEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.PAYMENT_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.PAYMENT_NOT_FOUND)));
    }

    /** Ensure payment reference is unique; pass ignoreId=null on create */
    public Mono<Void> ensureUniqueReference(String reference, UUID ignoreId) {
        if (sharedUtils.isNullOrEmptyOrBlank(reference))
            return Mono.error(BookingException.of(BookingErrorType.PAYMENT_REFERENCE_REQUIRED));
        return repository.findByPaymentReference(reference).flatMap(e -> {
            if (ignoreId != null && ignoreId.equals(e.getId())) return Mono.empty();
            return Mono.error(BookingException.of(BookingErrorType.PAYMENT_ALREADY_EXISTS));
        }).then();
    }

    /** Validate order is linked */
    public Mono<Void> validateOrderRequired(UUID orderId) {
        if (orderId == null)
            return Mono.error(BookingException.of(BookingErrorType.PAYMENT_ORDER_REQUIRED));
        return Mono.empty();
    }

    /** Validate payment method is present */
    public Mono<Void> validateMethodRequired(String paymentMethod) {
        if (sharedUtils.isNullOrEmptyOrBlank(paymentMethod))
            return Mono.error(BookingException.of(BookingErrorType.PAYMENT_METHOD_REQUIRED));
        return Mono.empty();
    }

    /** Validate requested amount is > 0 */
    public Mono<Void> validateRequestedAmount(BigDecimal requestedAmount) {
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0)
            return Mono.error(BookingException.of(BookingErrorType.PAYMENT_AMOUNT_INVALID));
        return Mono.empty();
    }

    /** Assert payment has not already been captured */
    public Mono<PaymentEntity> assertNotAlreadyCaptured(PaymentEntity payment) {
        if (PaymentStatus.CAPTURED.equals(payment.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.PAYMENT_ALREADY_CAPTURED));
        return Mono.just(payment);
    }

    /** Assert payment has not already been refunded */
    public Mono<PaymentEntity> assertNotAlreadyRefunded(PaymentEntity payment) {
        if (PaymentStatus.REFUNDED.equals(payment.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.PAYMENT_ALREADY_REFUNDED));
        return Mono.just(payment);
    }

    /** Validate status is not null */
    public Mono<Void> validateStatusRequired(PaymentStatus status) {
        if (status == null)
            return Mono.error(BookingException.of(BookingErrorType.PAYMENT_STATUS_INVALID));
        return Mono.empty();
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
