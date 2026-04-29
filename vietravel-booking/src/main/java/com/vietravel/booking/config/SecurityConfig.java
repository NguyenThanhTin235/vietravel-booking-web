package com.vietravel.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

        private final JwtAuthFilter jwtAuthFilter;

        public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
                this.jwtAuthFilter = jwtAuthFilter;
        }

        @Bean
        @Order(1)
        SecurityFilterChain bookingHistorySecurity(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/customer/bookings/**", "/customer/bookings")
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

                return http.build();
        }

        @Bean
        @Order(2)
        SecurityFilterChain paymentSecurity(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/payment/**")
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

                return http.build();
        }

        @Bean
        @Order(3)
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .logout(logout -> logout.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/",
                                                                "/health",
                                                                "/tour/**",
                                                                "/payment/**",
                                                                "/my-bookings",
                                                                "/customer/bookings",
                                                                "/customer/bookings/**",
                                                                "/auth/**",
                                                                "/logout",
                                                                "/login/**",
                                                                "/css/**", "/js/**", "/images/**", "/vendor/**",
                                                                "/favicon.ico")
                                                .permitAll()

                                                // ===== API =====
                                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/api/staff/**").hasAnyRole("STAFF", "ADMIN")
                                                .requestMatchers("/api/customer/**").hasAnyRole("CUSTOMER", "ADMIN")

                                                // ===== VIEW =====
                                                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/staff", "/staff/**").hasAnyRole("STAFF", "ADMIN")
                                                .requestMatchers("/customer", "/customer/**")
                                                .hasAnyRole("CUSTOMER", "ADMIN")

                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

}
