package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.dto.pojo.customer.CustomerRequest;
import io.booking.booking_service.dto.pojo.customer.CustomerResponse;
import io.booking.booking_service.dto.pojo.customer.CustomerUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.CustomerEntity;
import io.booking.booking_service.repository.CustomerRepository;
import io.booking.booking_service.service.CustomerService;
import io.booking.booking_service.util.validators.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;
    private final ReactiveTx reactiveTx;
    private final Customer validator;
    private final DatabaseClient db;

    public CustomerServiceImpl(CustomerRepository repository, ReactiveTx reactiveTx,
                               Customer validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<CustomerResponse> create(String requestId, CustomerRequest request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.CUSTOMER_NUMBER_REQUIRED));
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String status = request.getStatus() != null ? request.getStatus() : "ACTIVE";
        String fullName = request.getFullName() != null ? request.getFullName()
                : request.getFirstName() + " " + request.getLastName();

        return validator.ensureUniqueNumber(request.getCustomerNumber(), null)
                .then(validator.validateName(request.getFirstName(), request.getLastName()))
                .then(validator.validateEmail(request.getEmail()))
                .then(validator.validatePhone(request.getPhoneNumber()))
                .then(Mono.defer(() -> reactiveTx.required(() -> insert(request, status, fullName, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createCustomer] requestId={} customerNumber={}", requestId, request.getCustomerNumber()))
                .doOnSuccess(r -> log.info("[createCustomer] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createCustomer] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<CustomerEntity> insert(CustomerRequest r, String status, String fullName, OffsetDateTime now) {
        var spec = db.sql("""
                INSERT INTO customers
                    (tenant_id, customer_number, external_reference,
                     first_name, last_name, full_name,
                     email, phone_number, country_code,
                     status, created_at, updated_at)
                VALUES
                    (:tenantId, :customerNumber, :externalReference,
                     :firstName, :lastName, :fullName,
                     :email, :phoneNumber, :countryCode,
                     :status, :createdAt, :updatedAt)
                RETURNING *
                """)
                .bind("tenantId", r.getTenantId())
                .bind("customerNumber", r.getCustomerNumber())
                .bind("externalReference", r.getExternalReference() != null ? r.getExternalReference() : "")
                .bind("firstName", r.getFirstName())
                .bind("lastName", r.getLastName())
                .bind("fullName", fullName)
                .bind("phoneNumber", r.getPhoneNumber() != null ? r.getPhoneNumber() : "")
                .bind("countryCode", r.getCountryCode() != null ? r.getCountryCode() : "")
                .bind("status", status)
                .bind("createdAt", now)
                .bind("updatedAt", now);

        if (r.getEmail() != null) spec = spec.bind("email", r.getEmail());
        else spec = spec.bindNull("email", String.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<CustomerResponse> update(String requestId, UUID id, CustomerUpdate request) {
        if (request == null)
            return Mono.error(BookingException.of(BookingErrorType.CUSTOMER_NOT_FOUND));
        return validator.load(id)
                .flatMap(e -> validator.validateEmail(request.getEmail()).thenReturn(e))
                .flatMap(e -> validator.validatePhone(request.getPhoneNumber()).thenReturn(e))
                .flatMap(e -> Mono.defer(() -> reactiveTx.required(() -> updateCustomer(e, request))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateCustomer] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateCustomer] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<CustomerEntity> updateCustomer(CustomerEntity e, CustomerUpdate r) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String firstName = r.getFirstName() != null ? r.getFirstName() : e.getFirstName();
        String lastName = r.getLastName() != null ? r.getLastName() : e.getLastName();
        String fullName = r.getFullName() != null ? r.getFullName() : firstName + " " + lastName;
        String externalRef = r.getExternalReference() != null ? r.getExternalReference() : e.getExternalReference() != null ? e.getExternalReference() : "";
        String phone = r.getPhoneNumber() != null ? r.getPhoneNumber() : e.getPhoneNumber() != null ? e.getPhoneNumber() : "";
        String country = r.getCountryCode() != null ? r.getCountryCode() : e.getCountryCode() != null ? e.getCountryCode() : "";
        String status = r.getStatus() != null ? r.getStatus() : e.getStatus();
        String email = r.getEmail() != null ? r.getEmail() : e.getEmail();

        var spec = db.sql("""
                UPDATE customers
                SET external_reference = :externalReference,
                    first_name         = :firstName,
                    last_name          = :lastName,
                    full_name          = :fullName,
                    email              = :email,
                    phone_number       = :phoneNumber,
                    country_code       = :countryCode,
                    status             = :status,
                    updated_at         = :updatedAt
                WHERE id = :id
                RETURNING *
                """)
                .bind("externalReference", externalRef)
                .bind("firstName", firstName)
                .bind("lastName", lastName)
                .bind("fullName", fullName)
                .bind("phoneNumber", phone)
                .bind("countryCode", country)
                .bind("status", status)
                .bind("updatedAt", now)
                .bind("id", e.getId());

        // email is nullable, handle both cases
        if (email != null) spec = spec.bind("email", email);
        else spec = spec.bindNull("email", String.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<CustomerResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCustomer] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getCustomer] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    @Override
    public Mono<CustomerResponse> getByCustomerNumber(String requestId, String customerNumber) {
        if (customerNumber == null || customerNumber.isBlank())
            return Mono.error(BookingException.of(BookingErrorType.CUSTOMER_NUMBER_REQUIRED));
        return repository.findByCustomerNumber(customerNumber)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.CUSTOMER_NOT_FOUND)))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCustomerByNumber] requestId={} customerNumber={}", requestId, customerNumber))
                .doOnError(e -> log.error("[getCustomerByNumber] Failed requestId={} customerNumber={} error={}", requestId, customerNumber, e.getMessage(), e));
    }

    @Override
    public Mono<CustomerResponse> getByEmail(String requestId, String email) {
        if (email == null || email.isBlank())
            return Mono.error(BookingException.of(BookingErrorType.CUSTOMER_EMAIL_INVALID));
        return repository.findByEmail(email)
                .switchIfEmpty(Mono.error(BookingException.of(BookingErrorType.CUSTOMER_NOT_FOUND)))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getCustomerByEmail] requestId={} email={}", requestId, email))
                .doOnError(e -> log.error("[getCustomerByEmail] Failed requestId={} email={} error={}", requestId, email, e.getMessage(), e));
    }

    @Override
    public Mono<Void> deactivate(String requestId, UUID id) {
        return validator.load(id)
                .flatMap(e -> reactiveTx.required(() -> db.sql("""
                        UPDATE customers
                        SET status     = :status,
                            updated_at = :updatedAt
                        WHERE id = :id
                        RETURNING id
                        """)
                        .bind("status", "INACTIVE")
                        .bind("updatedAt", OffsetDateTime.now(ZoneOffset.UTC))
                        .bind("id", e.getId())
                        .fetch().rowsUpdated()))
                .then()
                .doOnSubscribe(s -> log.info("[deactivateCustomer] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[deactivateCustomer] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private CustomerEntity mapRow(io.r2dbc.spi.Row row) {
        return CustomerEntity.builder()
                .id(row.get("id", UUID.class))
                .tenantId(row.get("tenant_id", UUID.class))
                .customerNumber(row.get("customer_number", String.class))
                .externalReference(row.get("external_reference", String.class))
                .firstName(row.get("first_name", String.class))
                .lastName(row.get("last_name", String.class))
                .fullName(row.get("full_name", String.class))
                .email(row.get("email", String.class))
                .phoneNumber(row.get("phone_number", String.class))
                .countryCode(row.get("country_code", String.class))
                .status(row.get("status", String.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .updatedAt(row.get("updated_at", OffsetDateTime.class))
                .build();
    }

    private CustomerResponse toResponse(CustomerEntity e) {
        CustomerResponse r = new CustomerResponse();
        r.setId(e.getId());
        r.setTenantId(e.getTenantId());
        r.setCustomerNumber(e.getCustomerNumber());
        r.setExternalReference(e.getExternalReference());
        r.setFirstName(e.getFirstName());
        r.setLastName(e.getLastName());
        r.setFullName(e.getFullName());
        r.setEmail(e.getEmail());
        r.setPhoneNumber(e.getPhoneNumber());
        r.setCountryCode(e.getCountryCode());
        r.setStatus(e.getStatus());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}
