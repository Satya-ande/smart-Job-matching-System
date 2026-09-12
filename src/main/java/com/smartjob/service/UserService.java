package com.smartjob.service;

import com.smartjob.dto.request.LoginRequest;
import com.smartjob.dto.request.RegisterRequest;
import com.smartjob.dto.response.AuthResponse;
import com.smartjob.entity.User;
import com.smartjob.entity.enums.Role;
import com.smartjob.exception.DuplicateResourceException;
import com.smartjob.exception.InvalidRequestException;
import com.smartjob.exception.ResourceNotFoundException;
import com.smartjob.repository.UserRepository;
import com.smartjob.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling user account registration, authentication, and user lookups.
 *
 * Implements N-tier architecture separating business logic from REST controllers.
 */
@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Register a new user with BCrypt hashed password and return JWT response.
     */
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid role: " + request.getRole()
                + ". Must be CANDIDATE or RECRUITER");
        }

        User user = new User(
            request.getName(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            role
        );

        User saved = userRepository.save(user);
        log.info("User registered: {} (role={})", saved.getEmail(), saved.getRole());

        String token = jwtTokenProvider.generateToken(
            saved.getId(), saved.getEmail(), saved.getRole().name()
        );

        return new AuthResponse(
            token,
            jwtTokenProvider.getExpirationMs(),
            saved.getRole().name(),
            saved.getName(),
            saved.getEmail()
        );
    }

    /**
     * Authenticate user credentials and return JWT response.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("Invalid email or password");
        }

        log.info("User logged in: {}", user.getEmail());

        String token = jwtTokenProvider.generateToken(
            user.getId(), user.getEmail(), user.getRole().name()
        );

        return new AuthResponse(
            token,
            jwtTokenProvider.getExpirationMs(),
            user.getRole().name(),
            user.getName(),
            user.getEmail()
        );
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}
