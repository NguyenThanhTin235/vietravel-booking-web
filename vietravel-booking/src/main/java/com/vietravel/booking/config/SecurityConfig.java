package com.vietravel.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf.disable())
                                .logout(logout -> logout.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/",
                                                                "/health",
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
