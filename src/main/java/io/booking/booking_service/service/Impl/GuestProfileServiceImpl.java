package io.booking.booking_service.service.Impl;

import io.booking.booking_service.config.ReactiveTx;
import io.booking.booking_service.datatype.BookingErrorType;
import io.booking.booking_service.dto.pojo.guestprofile.GuestProfileRequest;
import io.booking.booking_service.dto.pojo.guestprofile.GuestProfileResponse;
import io.booking.booking_service.dto.pojo.guestprofile.GuestProfileUpdate;
import io.booking.booking_service.exception.BookingException;
import io.booking.booking_service.model.GuestProfileEntity;
import io.booking.booking_service.repository.GuestProfileRepository;
import io.booking.booking_service.service.GuestProfileService;
import io.booking.booking_service.util.validators.GuestProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
public class GuestProfileServiceImpl implements GuestProfileService {

    private final GuestProfileRepository repository;
    private final ReactiveTx reactiveTx;
    private final GuestProfile validator;
    private final DatabaseClient db;

    public GuestProfileServiceImpl(GuestProfileRepository repository, ReactiveTx reactiveTx,
                                   GuestProfile validator, DatabaseClient db) {
        this.repository = repository;
        this.reactiveTx = reactiveTx;
        this.validator = validator;
        this.db = db;
    }

    @Override
    public Mono<GuestProfileResponse> create(String requestId, GuestProfileRequest request) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String fullName = request.getFullName() != null
                ? request.getFullName()
                : request.getFirstName() + " " + request.getLastName();

        return validator.validateRequest(request)
                .then(validator.validateName(request.getFirstName(), request.getLastName()))
                .then(Mono.defer(() -> {
                    // Only enforce document uniqueness when both fields are provided
                    if (request.getTenantId() != null
                            && request.getDocumentType() != null
                            && request.getDocumentNumber() != null) {
                        return validator.ensureUniqueDocument(
                                request.getTenantId(), request.getDocumentType(),
                                request.getDocumentNumber(), null);
                    }
                    return Mono.empty();
                }))
                .then(Mono.defer(() -> reactiveTx.required(() -> insert(request, fullName, now))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[createGuestProfile] requestId={} name={} {}", requestId, request.getFirstName(), request.getLastName()))
                .doOnSuccess(r -> log.info("[createGuestProfile] Success requestId={} id={}", requestId, r != null ? r.getId() : null))
                .doOnError(e -> log.error("[createGuestProfile] Failed requestId={} error={}", requestId, e.getMessage(), e));
    }

    private Mono<GuestProfileEntity> insert(GuestProfileRequest r, String fullName, OffsetDateTime now) {
        var spec = db.sql("""
                INSERT INTO guest_profiles
                    (tenant_id, customer_id, first_name, last_name, full_name,
                     date_of_birth, gender, nationality_code,
                     document_type, document_number, email, phone_number,
                     special_requirements, created_at, updated_at)
                VALUES
                    (:tenantId, :customerId, :firstName, :lastName, :fullName,
                     :dateOfBirth, :gender, :nationalityCode,
                     :documentType, :documentNumber, :email, :phoneNumber,
                     :specialRequirements, :createdAt, :updatedAt)
                RETURNING *
                """)
                .bind("firstName", r.getFirstName())
                .bind("lastName", r.getLastName())
                .bind("fullName", fullName)
                .bind("gender", r.getGender() != null ? r.getGender() : "")
                .bind("nationalityCode", r.getNationalityCode() != null ? r.getNationalityCode() : "")
                .bind("documentType", r.getDocumentType() != null ? r.getDocumentType() : "")
                .bind("documentNumber", r.getDocumentNumber() != null ? r.getDocumentNumber() : "")
                .bind("email", r.getEmail() != null ? r.getEmail() : "")
                .bind("phoneNumber", r.getPhoneNumber() != null ? r.getPhoneNumber() : "")
                .bind("specialRequirements", r.getSpecialRequirements() != null ? r.getSpecialRequirements() : "")
                .bind("createdAt", now)
                .bind("updatedAt", now);

        if (r.getTenantId() != null) spec = spec.bind("tenantId", r.getTenantId());
        else spec = spec.bindNull("tenantId", UUID.class);

        if (r.getCustomerId() != null) spec = spec.bind("customerId", r.getCustomerId());
        else spec = spec.bindNull("customerId", String.class);

        if (r.getDateOfBirth() != null) spec = spec.bind("dateOfBirth", r.getDateOfBirth());
        else spec = spec.bindNull("dateOfBirth", LocalDate.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<GuestProfileResponse> update(String requestId, UUID id, GuestProfileUpdate request) {
        return validator.validateRequest(request)
                .then(validator.load(id))
                .flatMap(e -> Mono.defer(() -> {
                    String docType = request.getDocumentType() != null ? request.getDocumentType() : e.getDocumentType();
                    String docNum = request.getDocumentNumber() != null ? request.getDocumentNumber() : e.getDocumentNumber();
                    if (e.getTenantId() != null && docType != null && docNum != null
                            && (!docType.equals(e.getDocumentType()) || !docNum.equals(e.getDocumentNumber()))) {
                        return validator.ensureUniqueDocument(e.getTenantId(), docType, docNum, id).thenReturn(e);
                    }
                    return Mono.just(e);
                }))
                .flatMap(e -> Mono.defer(() -> reactiveTx.required(() -> updateProfile(e, request))))
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[updateGuestProfile] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[updateGuestProfile] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private Mono<GuestProfileEntity> updateProfile(GuestProfileEntity e, GuestProfileUpdate r) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String firstName = r.getFirstName() != null ? r.getFirstName() : e.getFirstName();
        String lastName = r.getLastName() != null ? r.getLastName() : e.getLastName();
        String fullName = r.getFullName() != null ? r.getFullName() : firstName + " " + lastName;
        LocalDate dob = r.getDateOfBirth() != null ? r.getDateOfBirth() : e.getDateOfBirth();
        String gender = r.getGender() != null ? r.getGender() : e.getGender() != null ? e.getGender() : "";
        String nationality = r.getNationalityCode() != null ? r.getNationalityCode() : e.getNationalityCode() != null ? e.getNationalityCode() : "";
        String docType = r.getDocumentType() != null ? r.getDocumentType() : e.getDocumentType() != null ? e.getDocumentType() : "";
        String docNum = r.getDocumentNumber() != null ? r.getDocumentNumber() : e.getDocumentNumber() != null ? e.getDocumentNumber() : "";
        String email = r.getEmail() != null ? r.getEmail() : e.getEmail() != null ? e.getEmail() : "";
        String phone = r.getPhoneNumber() != null ? r.getPhoneNumber() : e.getPhoneNumber() != null ? e.getPhoneNumber() : "";
        String special = r.getSpecialRequirements() != null ? r.getSpecialRequirements() : e.getSpecialRequirements() != null ? e.getSpecialRequirements() : "";

        var spec = db.sql("""
                UPDATE guest_profiles
                SET first_name           = :firstName,
                    last_name            = :lastName,
                    full_name            = :fullName,
                    date_of_birth        = :dateOfBirth,
                    gender               = :gender,
                    nationality_code     = :nationalityCode,
                    document_type        = :documentType,
                    document_number      = :documentNumber,
                    email                = :email,
                    phone_number         = :phoneNumber,
                    special_requirements = :specialRequirements,
                    updated_at           = :updatedAt
                WHERE id = :id
                RETURNING *
                """)
                .bind("firstName", firstName)
                .bind("lastName", lastName)
                .bind("fullName", fullName)
                .bind("gender", gender)
                .bind("nationalityCode", nationality)
                .bind("documentType", docType)
                .bind("documentNumber", docNum)
                .bind("email", email)
                .bind("phoneNumber", phone)
                .bind("specialRequirements", special)
                .bind("updatedAt", now)
                .bind("id", e.getId());

        if (dob != null) spec = spec.bind("dateOfBirth", dob);
        else spec = spec.bindNull("dateOfBirth", LocalDate.class);

        return spec.map((row, meta) -> mapRow(row)).one();
    }

    @Override
    public Mono<GuestProfileResponse> get(String requestId, UUID id) {
        return validator.load(id)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getGuestProfile] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[getGuestProfile] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }


    @Override
    public Flux<GuestProfileResponse> getByCustomer(String requestId, String customerId) {
        if (customerId == null)
            return Flux.error(BookingException.of(BookingErrorType.CUSTOMER_NOT_FOUND));
        return repository.findByCustomerId(customerId)
                .map(this::toResponse)
                .doOnSubscribe(s -> log.info("[getGuestProfilesByCustomer] requestId={} customerId={}", requestId, customerId))
                .doOnError(e -> log.error("[getGuestProfilesByCustomer] Failed requestId={} customerId={} error={}", requestId, customerId, e.getMessage(), e));
    }

    @Override
    public Mono<Void> delete(String requestId, UUID id) {
        return validator.load(id)
                .flatMap(e -> reactiveTx.required(() -> db.sql("""
                        DELETE FROM guest_profiles
                        WHERE id = :id
                        """)
                        .bind("id", e.getId())
                        .fetch().rowsUpdated()))
                .then()
                .doOnSubscribe(s -> log.info("[deleteGuestProfile] requestId={} id={}", requestId, id))
                .doOnError(e -> log.error("[deleteGuestProfile] Failed requestId={} id={} error={}", requestId, id, e.getMessage(), e));
    }

    private GuestProfileEntity mapRow(io.r2dbc.spi.Row row) {
        return GuestProfileEntity.builder()
                .id(row.get("id", UUID.class))
                .tenantId(row.get("tenant_id", UUID.class))
                .customerId(row.get("customer_id", String.class))
                .firstName(row.get("first_name", String.class))
                .lastName(row.get("last_name", String.class))
                .fullName(row.get("full_name", String.class))
                .dateOfBirth(row.get("date_of_birth", LocalDate.class))
                .gender(row.get("gender", String.class))
                .nationalityCode(row.get("nationality_code", String.class))
                .documentType(row.get("document_type", String.class))
                .documentNumber(row.get("document_number", String.class))
                .email(row.get("email", String.class))
                .phoneNumber(row.get("phone_number", String.class))
                .specialRequirements(row.get("special_requirements", String.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .updatedAt(row.get("updated_at", OffsetDateTime.class))
                .build();
    }

    private GuestProfileResponse toResponse(GuestProfileEntity e) {
        GuestProfileResponse r = new GuestProfileResponse();
        r.setId(e.getId());
        r.setTenantId(e.getTenantId());
        r.setCustomerId(e.getCustomerId());
        r.setFirstName(e.getFirstName());
        r.setLastName(e.getLastName());
        r.setFullName(e.getFullName());
        r.setDateOfBirth(e.getDateOfBirth());
        r.setGender(e.getGender());
        r.setNationalityCode(e.getNationalityCode());
        r.setDocumentType(e.getDocumentType());
        r.setDocumentNumber(e.getDocumentNumber());
        r.setEmail(e.getEmail());
        r.setPhoneNumber(e.getPhoneNumber());
        r.setSpecialRequirements(e.getSpecialRequirements());
        r.setCreatedAt(e.getCreatedAt());
        r.setUpdatedAt(e.getUpdatedAt());
        return r;
    }
}
