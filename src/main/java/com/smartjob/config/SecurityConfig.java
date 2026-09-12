package com.smartjob.config;

import com.smartjob.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration with JWT authentication and role-based authorization.
 *
 * V2: Replaces Phase 1's permit-all config with proper JWT-based security.
 *
 * Public endpoints (no auth required):
 *   - POST /api/auth/register, /api/auth/login
 *   - GET  /api/jobs (browse jobs without login)
 *   - Swagger UI and API docs
 *   - H2 Console (dev only)
 *
 * Role-based endpoints:
 *   - RECRUITER: Create/update/delete jobs, view job applications, update status
 *   - CANDIDATE: Create/update profile, apply for jobs, view matches
 *
 * All other endpoints require authentication.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (REST API uses JWT tokens, not cookies)
            .csrf(AbstractHttpConfigurer::disable)

            // Allow frames for H2 console
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

            // Stateless sessions (JWT-based, no server-side sessions)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public: Static Web UI resources & SPA entry point
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/css/**",
                    "/js/**",
                    "/assets/**",
                    "/favicon.ico"
                ).permitAll()

                // Public: Auth endpoints
                .requestMatchers("/api/auth/**").permitAll()

                // Public: Browse jobs (GET only)
                .requestMatchers(HttpMethod.GET, "/api/jobs/**").permitAll()

                // Public: Swagger UI and API docs
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs"
                ).permitAll()

                // Public: H2 Console (dev only)
                .requestMatchers("/h2-console/**").permitAll()

                // Recruiter-only: Create/update/delete jobs
                .requestMatchers(HttpMethod.POST, "/api/jobs").hasRole("RECRUITER")
                .requestMatchers(HttpMethod.PUT, "/api/jobs/**").hasRole("RECRUITER")
                .requestMatchers(HttpMethod.DELETE, "/api/jobs/**").hasRole("RECRUITER")

                // Recruiter-only: Update application status
                .requestMatchers(HttpMethod.PATCH, "/api/applications/*/status").hasRole("RECRUITER")

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )

            // Add JWT filter before Spring Security's default auth filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
