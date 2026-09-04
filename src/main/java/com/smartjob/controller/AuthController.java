package com.smartjob.controller;

import com.smartjob.dto.request.LoginRequest;
import com.smartjob.dto.request.RegisterRequest;
import com.smartjob.dto.response.AuthResponse;
import com.smartjob.dto.response.ErrorResponse;
import com.smartjob.entity.User;
import com.smartjob.entity.enums.Role;
import com.smartjob.exception.DuplicateResourceException;
import com.smartjob.exception.InvalidRequestException;
import com.smartjob.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication (register + login).
 *
 * V2 Addition: V1 had no authentication — all users were anonymous.
 *
 * Endpoints:
 *   POST /api/auth/register → Create account → 201
 *   POST /api/auth/login    → Get JWT token  → 200
 *
 * NOTE: Full JWT token generation will be added in Phase 13.
 *       For now, registration and login validate credentials and return a placeholder token.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * POST /api/auth/register
     * Register a new user account.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        // Validate role
        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid role: " + request.getRole()
                + ". Must be CANDIDATE or RECRUITER");
        }

        // Create user with BCrypt-hashed password
        User user = new User(
            request.getName(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            role
        );

        User saved = userRepository.save(user);
        log.info("User registered: {} (role={})", saved.getEmail(), saved.getRole());

        // Return response (placeholder token until Phase 13 adds JWT)
        AuthResponse response = new AuthResponse(
            "jwt-token-placeholder-" + saved.getId(),
            86400000,
            saved.getRole().name(),
            saved.getName(),
            saved.getEmail()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login
     * Authenticate user and return JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidRequestException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("Invalid email or password");
        }

        log.info("User logged in: {}", user.getEmail());

        // Return response (placeholder token until Phase 13 adds JWT)
        AuthResponse response = new AuthResponse(
            "jwt-token-placeholder-" + user.getId(),
            86400000,
            user.getRole().name(),
            user.getName(),
            user.getEmail()
        );

        return ResponseEntity.ok(response);
    }
}
