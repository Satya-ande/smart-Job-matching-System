package com.smartjob.service;

import com.smartjob.dto.request.LoginRequest;
import com.smartjob.dto.request.RegisterRequest;
import com.smartjob.dto.response.AuthResponse;
import com.smartjob.entity.User;
import com.smartjob.entity.enums.Role;
import com.smartjob.exception.DuplicateResourceException;
import com.smartjob.exception.InvalidRequestException;
import com.smartjob.repository.UserRepository;
import com.smartjob.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — Authentication & Account Tests")
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    private JwtTokenProvider jwtTokenProvider;
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
            "test-secret-key-must-be-at-least-32-chars-long-for-hmac-sha",
            86400000L
        );
        userService = new UserService(userRepository, passwordEncoder, jwtTokenProvider);

        mockUser = new User("Satya Ande", "satya@example.com", "$2a$10$encodedPassword", Role.CANDIDATE);
        mockUser.setId(1L);
    }

    @Test
    @DisplayName("register — succeeds and returns JWT response")
    void register_success() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Satya Ande");
        request.setEmail("satya@example.com");
        request.setPassword("password123");
        request.setRole("CANDIDATE");

        when(userRepository.existsByEmail("satya@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        AuthResponse response = userService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getEmail()).isEqualTo("satya@example.com");
        assertThat(response.getRole()).isEqualTo("CANDIDATE");
    }

    @Test
    @DisplayName("register — throws DuplicateResourceException if email already registered")
    void register_duplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Satya Ande");
        request.setEmail("satya@example.com");
        request.setPassword("password123");
        request.setRole("CANDIDATE");

        when(userRepository.existsByEmail("satya@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
            .isInstanceOf(DuplicateResourceException.class)
            .hasMessageContaining("Email already registered");
    }

    @Test
    @DisplayName("login — succeeds with valid credentials")
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("satya@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("satya@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);

        AuthResponse response = userService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getEmail()).isEqualTo("satya@example.com");
    }

    @Test
    @DisplayName("login — throws InvalidRequestException for invalid password")
    void login_wrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("satya@example.com");
        request.setPassword("wrongPassword");

        when(userRepository.findByEmail("satya@example.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(request))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("Invalid email or password");
    }
}
