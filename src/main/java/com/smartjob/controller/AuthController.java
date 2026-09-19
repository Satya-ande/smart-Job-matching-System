package com.smartjob.controller;

import com.smartjob.dto.request.LoginRequest;
import com.smartjob.dto.request.RegisterRequest;
import com.smartjob.dto.response.AuthResponse;
import com.smartjob.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final com.smartjob.service.UserService userService;

    public AuthController(com.smartjob.service.UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /api/auth/register
     * Register a new user account and return a JWT token.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login
     * Authenticate user and return JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/auth/me
     * Return current authenticated user details extracted from JWT claims.
     * The principal is the User entity set by JwtAuthenticationFilter.
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // JwtAuthenticationFilter stores the User entity as the principal
        Object principal = auth.getPrincipal();
        User user;
        if (principal instanceof User u) {
            user = u;
        } else {
            // Fallback: look up by email string
            user = userService.findByEmail(auth.getName());
        }

        AuthResponse response = new AuthResponse(
            null,
            0,
            user.getRole().name(),
            user.getName(),
            user.getEmail()
        );
        response.setId(user.getId());
        return ResponseEntity.ok(response);
    }
}
