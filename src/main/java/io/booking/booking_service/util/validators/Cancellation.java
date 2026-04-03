package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.CancellationStatus;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.CancellationEntity;
import io.booking.booking_service.repository.CancellationRepository;
import io.booking.booking_service.util.SharedUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class Cancellation {

    private final SharedUtils sharedUtils;
    private final CancellationRepository repository;

    /** Load cancellation by id, fail with NOT_FOUND if missing */
    public Mono<CancellationEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.CANCELLATION_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.CANCELLATION_NOT_FOUND)));
    }

    /** Validate reason code is present */
    public Mono<Void> validateReasonRequired(String reasonCode) {
        if (sharedUtils.isNullOrEmptyOrBlank(reasonCode))
            return Mono.error(BookingException.of(BookingErrorType.CANCELLATION_REASON_REQUIRED));
        return Mono.empty();
    }

    /** Validate order id is present */
    public Mono<Void> validateOrderRequired(UUID orderId) {
        if (orderId == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_NOT_FOUND));
        return Mono.empty();
    }

    /** Validate actor type is present */
    public Mono<Void> validateActorTypeRequired(ActorType actorType) {
        if (actorType == null)
            return Mono.error(BookingException.of(BookingErrorType.CANCELLATION_REQUEST_INVALID));
        return Mono.empty();
    }

    /** Validate actor id is present */
    public Mono<Void> validateActorIdRequired(String actorId) {
        if (sharedUtils.isNullOrEmptyOrBlank(actorId))
            return Mono.error(BookingException.of(BookingErrorType.CANCELLATION_REQUEST_INVALID));
        return Mono.empty();
    }

    /** Assert cancellation has not already been completed */
    public Mono<CancellationEntity> assertNotAlreadyCompleted(CancellationEntity cancellation) {
        if (CancellationStatus.COMPLETED.equals(cancellation.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.CANCELLATION_ALREADY_COMPLETED));
        return Mono.just(cancellation);
    }

    /** Assert cancellation is eligible for refund */
    public Mono<CancellationEntity> assertRefundEligible(CancellationEntity cancellation) {
        if (Boolean.FALSE.equals(cancellation.getRefundEligible()))
            return Mono.error(BookingException.of(BookingErrorType.CANCELLATION_REFUND_NOT_ELIGIBLE));
        return Mono.just(cancellation);
    }

    /** Assert cancellation is allowed (not rejected) */
    public Mono<CancellationEntity> assertNotRejected(CancellationEntity cancellation) {
        if (CancellationStatus.REJECTED.equals(cancellation.getStatus()))
            return Mono.error(BookingException.of(BookingErrorType.CANCELLATION_NOT_ALLOWED));
        return Mono.just(cancellation);
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
