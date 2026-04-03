package io.booking.booking_service.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
public class SharedUtils {
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public SharedUtils(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    public boolean isNullOrEmptyOrBlank(String s) {
        return s == null || s.isEmpty() || s.isBlank();
    }

    public String toJson(Object o, boolean prettify) {
        if (o != null) {
            try {
                return prettify ? this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o) : this.objectMapper.writeValueAsString(o);
            } catch (Exception e) {
                log.error(String.format("Object to json error [ %s ]", o), e);
            }
        }
        return null;
    }

    public Boolean defaultBoolean(Boolean value, boolean defaultValue) {
        return value != null ? value : defaultValue;
    }

    public String normalizeCode(String code) {
        return code != null ? code.trim() : null;
    }

    public String normalizeText(String value) {
        return value != null ? value.trim() : null;
    }

    @SneakyThrows
    public <T> T fromJsonToObject(String json, Class<T> c) {
        return this.objectMapper.readValue(json, c);
    }

    @SneakyThrows
    public <T> T fromJsonToObject(String json, TypeReference<T> c) {
        return this.objectMapper.readValue(json, c);
    }

    @SneakyThrows
    public String formatDate(@NonNull String format, @NonNull LocalDateTime time) {
        if (!this.isNullOrEmptyOrBlank(format)) {
            return DateTimeFormatter.ofPattern(format).format(time);
        } else {
            throw new Exception(String.format("Invalid date format %s for date %s", format, time));
        }
    }

    @SneakyThrows
    public LocalDateTime toDate(@NonNull String format, @NonNull String time, @NonNull String zone) {
        return new SimpleDateFormat(format).parse(time).toInstant().atZone(ZoneId.of(zone)).toLocalDateTime();
    }

    public int calculateAge(LocalDateTime dateOfBirth) {
        LocalDateTime currentDate = LocalDateTime.now();
        return Period.between(dateOfBirth.toLocalDate(), currentDate.toLocalDate()).getYears();
    }

//    private String randomAlphaNumeric(int size) {
//        return this.randomGenerator.ints(48, 123)
//                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
//                .limit(size)
//                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
//                .toString();
//    }

//    public String randomNumeric(int size) {
//        return this.randomGenerator.ints(48, 58)
//                .filter(i -> i <= 57)
//                .limit(size)
//                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
//                .toString();
//    }

    public boolean isNumeric(String s) {
        try {
            if (!this.isNullOrEmptyOrBlank(s)) {
                double d = Double.parseDouble(s);
                log.debug(String.format("Numeric validation [ %s ]", d));
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public UUID generateTicketId() {
        return UUID.randomUUID();
    }

    /**
     * Generates a public reference ID for customers.
     * <p>
     * MVP generator: TCK-YYYY-<8chars>
     * In production, consider a DB sequence for monotonic IDs per tenant or global.
     */
    public String generatePublicId(OffsetDateTime nowUtc) {
        String year = String.valueOf(nowUtc.getYear());
        String shortId = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "TCK-" + year + "-" + shortId;
    }
//    public UUID parseUuidOrThrow(String raw, TicketErrorType errorType) {
//        try {
//            return UUID.fromString(raw);
//        } catch (Exception e) {
//            throw TicketException.of(errorType);
//        }
//    }
}
