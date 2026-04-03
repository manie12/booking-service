package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.datatype.booking.ActorType;
import io.booking.booking_service.datatype.booking.OrderStatus;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.OrderStatusHistoryEntity;
import io.booking.booking_service.repository.OrderStatusHistoryRepository;
import io.booking.booking_service.util.SharedUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusHistory {

    private final SharedUtils sharedUtils;
    private final OrderStatusHistoryRepository repository;

    /** Load order status history entry by id, fail with NOT_FOUND if missing */
    public Mono<OrderStatusHistoryEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.ORDER_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.ORDER_NOT_FOUND)));
    }

    /** Validate order id is present */
    public Mono<Void> validateOrderRequired(UUID orderId) {
        if (orderId == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_NOT_FOUND));
        return Mono.empty();
    }

    /** Validate new status is present */
    public Mono<Void> validateNewStatusRequired(OrderStatus newStatus) {
        if (newStatus == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_REQUEST_INVALID));
        return Mono.empty();
    }

    /** Validate actor type is present */
    public Mono<Void> validateActorTypeRequired(ActorType actorType) {
        if (actorType == null)
            return Mono.error(BookingException.of(BookingErrorType.ORDER_REQUEST_INVALID));
        return Mono.empty();
    }

    /** Validate actor id is present */
    public Mono<Void> validateActorIdRequired(String actorId) {
        if (sharedUtils.isNullOrEmptyOrBlank(actorId))
            return Mono.error(BookingException.of(BookingErrorType.ORDER_REQUEST_INVALID));
        return Mono.empty();
    }

    /** Assert new status differs from old status */
    public Mono<Void> assertStatusTransition(OrderStatus oldStatus, OrderStatus newStatus) {
        if (oldStatus != null && oldStatus.equals(newStatus))
            return Mono.error(BookingException.of(BookingErrorType.ORDER_REQUEST_INVALID));
        return Mono.empty();
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
