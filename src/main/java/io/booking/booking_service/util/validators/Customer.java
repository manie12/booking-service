package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.CustomerEntity;
import io.booking.booking_service.repository.CustomerRepository;
import io.booking.booking_service.util.SharedUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class Customer {

    private final SharedUtils sharedUtils;
    private final CustomerRepository repository;

    /** Ensure customer number is unique; pass ignoreId=null on create */
    public Mono<Void> ensureUniqueNumber(String customerNumber, UUID ignoreId) {
        if (sharedUtils.isNullOrEmptyOrBlank(customerNumber))
            return Mono.error(BookingException.of(BookingErrorType.CUSTOMER_NUMBER_REQUIRED));
        return repository.findByCustomerNumber(customerNumber).flatMap(e -> {
            if (ignoreId != null && ignoreId.equals(e.getId())) return Mono.empty();
            return Mono.error(BookingException.of(BookingErrorType.CUSTOMER_ALREADY_EXISTS));
        }).then();
    }

    /** Load customer by id, fail with NOT_FOUND if missing */
    public Mono<CustomerEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.CUSTOMER_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.CUSTOMER_NOT_FOUND)));
    }

    /** Load and assert customer is active */
    public Mono<CustomerEntity> loadActive(UUID id) {
        return load(id).flatMap(c -> {
            if (!"ACTIVE".equalsIgnoreCase(c.getStatus()))
                return Mono.error(BookingException.of(BookingErrorType.CUSTOMER_INACTIVE));
            return Mono.just(c);
        });
    }

    /** Validate required name fields */
    public Mono<Void> validateName(String firstName, String lastName) {
        if (sharedUtils.isNullOrEmptyOrBlank(firstName) || sharedUtils.isNullOrEmptyOrBlank(lastName))
            return Mono.error(BookingException.of(BookingErrorType.CUSTOMER_NAME_REQUIRED));
        return Mono.empty();
    }

    /** Validate email format (basic) */
    public Mono<Void> validateEmail(String email) {
        if (email != null && !email.isBlank() && !email.contains("@"))
            return Mono.error(BookingException.of(BookingErrorType.CUSTOMER_EMAIL_INVALID));
        return Mono.empty();
    }

    /** Validate phone is not blank when provided */
    public Mono<Void> validatePhone(String phone) {
        if (phone != null && phone.isBlank())
            return Mono.error(BookingException.of(BookingErrorType.CUSTOMER_PHONE_INVALID));
        return Mono.empty();
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
