package com.vietravel.booking;

import com.vietravel.booking.domain.entity.tour.Destination;
import com.vietravel.booking.domain.entity.tour.DestinationType;
import com.vietravel.booking.domain.entity.tour.TourCategory;
import com.vietravel.booking.domain.repository.tour.DestinationRepository;
import com.vietravel.booking.domain.repository.tour.TourCategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication
@ConfigurationPropertiesScan
public class VietravelBookingApplication {

    private static final Logger log = LoggerFactory.getLogger(VietravelBookingApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(VietravelBookingApplication.class, args);
    }

    /**
     * Test kết nối MySQL khi application start
     */
    @Bean
    CommandLineRunner testDatabaseConnection(DataSource dataSource) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                log.info("✅ KẾT NỐI MYSQL THÀNH CÔNG");
                log.info("👉 Database URL: {}", connection.getMetaData().getURL());
                log.info("👉 Database Product: {}", connection.getMetaData().getDatabaseProductName());
                log.info("👉 Database Version: {}", connection.getMetaData().getDatabaseProductVersion());
            } catch (Exception e) {
                log.error("❌ KHÔNG KẾT NỐI ĐƯỢC MYSQL");
                log.error("👉 Lý do: {}", e.getMessage());
                throw e;
            }
        };
    }

    @Bean
    CommandLineRunner seedStartLocations(
            DestinationRepository destinationRepository,
            TourCategoryRepository tourCategoryRepository) {
        return args -> {
            TourCategory category = tourCategoryRepository.findBySlug("diem-khoi-hanh")
                    .orElseGet(() -> {
                        TourCategory c = new TourCategory();
                        c.setName("Điểm khởi hành");
                        c.setSlug("diem-khoi-hanh");
                        c.setSortOrder(0);
                        c.setIsActive(true);
                        return tourCategoryRepository.save(c);
                    });

            ensureDestination(destinationRepository, category, "Hà Nội", "ha-noi", 1);
            ensureDestination(destinationRepository, category, "Đà Nẵng", "da-nang", 2);
            ensureDestination(destinationRepository, category, "Hồ Chí Minh", "ho-chi-minh", 3);
        };
    }

    @Bean
    CommandLineRunner seedTourCategoryParents(TourCategoryRepository tourCategoryRepository) {
        return args -> {
            ensureTourCategory(tourCategoryRepository, "Trong nước", "trong-nuoc", 0);
            ensureTourCategory(tourCategoryRepository, "Nước ngoài", "nuoc-ngoai", 1);
        };
    }

    @Bean
    CommandLineRunner ensureStartLocationColumn(DataSource dataSource) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                String schema = connection.getCatalog();
                if (schema == null || schema.isBlank()) {
                    return;
                }

                boolean columnExists = exists(connection,
                        "select 1 from information_schema.COLUMNS where TABLE_SCHEMA = ? and TABLE_NAME = 'tours' and COLUMN_NAME = 'start_location_id' limit 1",
                        schema);

                if (!columnExists) {
                    try (Statement st = connection.createStatement()) {
                        st.executeUpdate("ALTER TABLE tours ADD COLUMN start_location_id BIGINT NULL");
                        log.info("✅ Added column tours.start_location_id");
                    }
                }

                boolean indexExists = exists(connection,
                        "select 1 from information_schema.STATISTICS where TABLE_SCHEMA = ? and TABLE_NAME = 'tours' and INDEX_NAME = 'idx_tours_start_location' limit 1",
                        schema);
                if (!indexExists) {
                    try (Statement st = connection.createStatement()) {
                        st.executeUpdate("CREATE INDEX idx_tours_start_location ON tours(start_location_id)");
                        log.info("✅ Added index idx_tours_start_location");
                    }
                }

                boolean fkExists = exists(connection,
                        "select 1 from information_schema.KEY_COLUMN_USAGE where TABLE_SCHEMA = ? and TABLE_NAME = 'tours' and COLUMN_NAME = 'start_location_id' and REFERENCED_TABLE_NAME = 'destination' limit 1",
                        schema);
                if (!fkExists) {
                    try (Statement st = connection.createStatement()) {
                        st.executeUpdate(
                                "ALTER TABLE tours ADD CONSTRAINT fk_tours_start_location FOREIGN KEY (start_location_id) REFERENCES destination(id) ON DELETE SET NULL ON UPDATE CASCADE");
                        log.info("✅ Added foreign key fk_tours_start_location");
                    }
                }
            } catch (Exception e) {
                log.error("❌ Không thể kiểm tra/khởi tạo start_location_id: {}", e.getMessage());
            }
        };
    }

    private boolean exists(Connection connection, String sql, String schema) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, schema);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            log.error("❌ Lỗi kiểm tra schema: {}", e.getMessage());
            return false;
        }
    }

    private void ensureDestination(
            DestinationRepository destinationRepository,
            TourCategory category,
            String name,
            String slug,
            int sortOrder) {
        if (destinationRepository.findBySlug(slug).isPresent()) {
            return;
        }
        Destination d = new Destination();
        d.setName(name);
        d.setSlug(slug);
        d.setType(DestinationType.CITY);
        d.setCategory(category);
        d.setSortOrder(sortOrder);
        d.setIsActive(true);
        destinationRepository.save(d);
        log.info("✅ Seeded start location: {}", name);
    }

    private void ensureTourCategory(
            TourCategoryRepository repo,
            String name,
            String slug,
            int sortOrder) {
        if (repo.findBySlug(slug).isPresent()) {
            return;
        }
        TourCategory c = new TourCategory();
        c.setName(name);
        c.setSlug(slug);
        c.setSortOrder(sortOrder);
        c.setIsActive(true);
        repo.save(c);
        log.info("✅ Seeded tour category: {}", name);
    }

    @Bean
    @Order(0)
    SecurityFilterChain publicApiSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        "/api/tour-categories/**",
                        "/api/tour-lines/**",
                        "/api/admin/tour-categories/**",
                        "/api/admin/destinations/**")
                .csrf(csrf -> csrf.ignoringRequestMatchers(
                        "/api/tour-categories", "/api/tour-categories/**",
                        "/api/tour-lines", "/api/tour-lines/**",
                        "/api/admin/tour-categories", "/api/admin/tour-categories/**",
                        "/api/admin/destinations", "/api/admin/destinations/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/tour-categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/tour-categories", "/api/tour-categories/**",
                                "/api/tour-lines", "/api/tour-lines/**",
                                "/api/admin/destinations", "/api/admin/destinations/**")
                        .permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
