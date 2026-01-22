package com.vietravel.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
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
}
