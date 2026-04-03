package io.booking.booking_service.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flyway configuration for reactive (R2DBC) applications.
 * <p>
 * R2DBC does not provide a JDBC DataSource, so Flyway's auto-configuration
 * cannot find one and migrations are silently skipped.
 * This configuration explicitly builds a Flyway instance using the JDBC
 * connection details defined under {@code spring.flyway.*} in application.yml.
 */
@Slf4j
@Configuration
public class FlywayConfig {

    @Bean
    public Flyway flyway(
            @Value("${spring.flyway.url}") String url,
            @Value("${spring.flyway.user}") String user,
            @Value("${spring.flyway.password}") String password,
            @Value("${spring.flyway.schemas:booking}") String schemas,
            @Value("${spring.flyway.locations:classpath:db/migration}") String locations,
            @Value("${spring.flyway.baseline-on-migrate:true}") boolean baselineOnMigrate,
            @Value("${spring.flyway.clean-on-start:false}") boolean cleanOnStart
    ) {
        log.info("[FlywayConfig] Configuring Flyway: url={}, schemas={}, locations={}, baselineOnMigrate={}, cleanOnStart={}",
                url, schemas, locations, baselineOnMigrate, cleanOnStart);

        Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .schemas(schemas.split(","))
                .locations(locations.split(","))
                .baselineOnMigrate(baselineOnMigrate)
                .cleanDisabled(!cleanOnStart)
                .load();

        if (cleanOnStart) {
            log.warn("[FlywayConfig] clean-on-start=true — dropping all objects in schemas: {}", schemas);
            flyway.clean();
            log.info("[FlywayConfig] Schema cleaned successfully");
        }

        log.info("[FlywayConfig] Running Flyway migrations...");
        flyway.migrate();
        log.info("[FlywayConfig] Flyway migrations completed successfully");

        return flyway;
    }
}
