package com.smartjob.controller;

import com.smartjob.dto.request.LoginRequest;
import com.smartjob.dto.request.RegisterRequest;
import com.smartjob.dto.response.AuthResponse;
import com.smartjob.entity.User;
import com.smartjob.entity.enums.Role;
import com.smartjob.exception.DuplicateResourceException;
import com.smartjob.exception.InvalidRequestException;
import com.smartjob.repository.UserRepository;
import com.smartjob.security.JwtTokenProvider;
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
 *   POST /api/auth/register → Create account + get JWT → 201
 *   POST /api/auth/login    → Authenticate + get JWT   → 200
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * POST /api/auth/register
     * Register a new user account and return a JWT token.
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

        // Generate real JWT token
        String token = jwtTokenProvider.generateToken(
            saved.getId(), saved.getEmail(), saved.getRole().name()
        );

        AuthResponse response = new AuthResponse(
            token,
            jwtTokenProvider.getExpirationMs(),
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

        // Generate real JWT token
        String token = jwtTokenProvider.generateToken(
            user.getId(), user.getEmail(), user.getRole().name()
        );

        AuthResponse response = new AuthResponse(
            token,
            jwtTokenProvider.getExpirationMs(),
            user.getRole().name(),
            user.getName(),
            user.getEmail()
        );

        return ResponseEntity.ok(response);
    }
}
