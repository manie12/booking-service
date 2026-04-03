package io.booking.booking_service.util.validators;

import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.GuestProfileEntity;
import io.booking.booking_service.repository.GuestProfileRepository;
import io.booking.booking_service.util.SharedUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestProfile {

    private final SharedUtils sharedUtils;
    private final GuestProfileRepository repository;

    /** Load guest profile by id */
    public Mono<GuestProfileEntity> load(UUID id) {
        if (id == null) return Mono.error(BookingException.of(BookingErrorType.GUEST_PROFILE_NOT_FOUND));
        return repository.findById(id)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.GUEST_PROFILE_NOT_FOUND)));
    }

    /** Ensure no duplicate document (type + number) within tenant; ignoreId for updates */
    public Mono<Void> ensureUniqueDocument(UUID tenantId, String documentType, String documentNumber, UUID ignoreId) {
        if (sharedUtils.isNullOrEmptyOrBlank(documentType) || sharedUtils.isNullOrEmptyOrBlank(documentNumber))
            return Mono.error(BookingException.of(BookingErrorType.GUEST_PROFILE_DOCUMENT_REQUIRED));
        return repository.findByTenantIdAndDocumentTypeAndDocumentNumber(tenantId, documentType, documentNumber)
                .flatMap(e -> {
                    if (ignoreId != null && ignoreId.equals(e.getId())) return Mono.empty();
                    return Mono.error(BookingException.of(BookingErrorType.GUEST_PROFILE_ALREADY_EXISTS));
                }).then();
    }

    /** Validate required name fields */
    public Mono<Void> validateName(String firstName, String lastName) {
        if (sharedUtils.isNullOrEmptyOrBlank(firstName) || sharedUtils.isNullOrEmptyOrBlank(lastName))
            return Mono.error(BookingException.of(BookingErrorType.GUEST_PROFILE_NAME_REQUIRED));
        return Mono.empty();
    }

    /** Validate request body is not null */
    public Mono<Void> validateRequest(Object request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.GUEST_PROFILE_REQUEST_REQUIRED));
        return Mono.empty();
    }

    public String resolveRequestId(String header, String payload) {
        if (!sharedUtils.isNullOrEmptyOrBlank(payload)) return payload;
        if (!sharedUtils.isNullOrEmptyOrBlank(header)) return header;
        return UUID.randomUUID().toString();
    }
}
