package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.PaymentStatus;
import io.booking.booking_service.datatype.booking.PaymentTransactionType;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.PaymentTransactionEntity;
import io.booking.booking_service.repository.PaymentTransactionRepository;
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
public class PaymentTransaction {

    private final SharedUtils sharedUtils;
    private final PaymentTransactionRepository repository;

    /** Load payment transaction by id, fail with NOT_FOUND if missing */
    public Mono<PaymentTransactionEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.PAYMENT_TRANSACTION_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.PAYMENT_TRANSACTION_NOT_FOUND)));
    }

    /** Ensure transaction reference is unique; pass ignoreId=null on create */
    public Mono<Void> ensureUniqueReference(String reference, UUID ignoreId) {
        if (sharedUtils.isNullOrEmptyOrBlank(reference))
            return Mono.error(BookingException.of(BookingErrorType.PAYMENT_TRANSACTION_REQUEST_INVALID));
        return repository.findByTransactionReference(reference).flatMap(e -> {
            if (ignoreId != null && ignoreId.equals(e.getId())) return Mono.empty();
            return Mono.error(BookingException.of(BookingErrorType.PAYMENT_TRANSACTION_ALREADY_EXISTS));
        }).then();
    }

    /** Validate payment id is present */
    public Mono<Void> validatePaymentRequired(UUID paymentId) {
        if (paymentId == null)
            return Mono.error(BookingException.of(BookingErrorType.PAYMENT_NOT_FOUND));
        return Mono.empty();
    }

    /** Validate transaction type is present */
    public Mono<Void> validateTransactionTypeRequired(PaymentTransactionType transactionType) {
        if (transactionType == null)
            return Mono.error(BookingException.of(BookingErrorType.PAYMENT_TRANSACTION_TYPE_REQUIRED));
        return Mono.empty();
    }

    /** Validate amount is > 0 */
    public Mono<Void> validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            return Mono.error(BookingException.of(BookingErrorType.PAYMENT_TRANSACTION_AMOUNT_INVALID));
        return Mono.empty();
    }

    /** Validate status is not null */
    public Mono<Void> validateStatusRequired(PaymentStatus status) {
        if (status == null)
            return Mono.error(BookingException.of(BookingErrorType.PAYMENT_STATUS_INVALID));
        return Mono.empty();
    }

    /** Assert transaction belongs to the given payment */
    public Mono<PaymentTransactionEntity> assertBelongsToPayment(PaymentTransactionEntity transaction, UUID paymentId) {
        if (!paymentId.equals(transaction.getPaymentId()))
            return Mono.error(BookingException.of(BookingErrorType.PAYMENT_TRANSACTION_REQUEST_INVALID));
        return Mono.just(transaction);
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
