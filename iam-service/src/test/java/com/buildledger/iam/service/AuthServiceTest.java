package com.buildledger.iam.service;

import com.buildledger.iam.dto.request.LoginRequest;
import com.buildledger.iam.dto.response.LoginResponse;
import com.buildledger.iam.entity.AccountStatus;
import com.buildledger.iam.entity.User;
import com.buildledger.iam.entity.UserRole;
import com.buildledger.iam.exception.AuthenticationFailedException;
import com.buildledger.iam.exception.AccountLockedException;
import com.buildledger.iam.repository.*;
import com.buildledger.iam.security.JwtTokenProvider;
import com.buildledger.iam.util.PasswordGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private RateLimitingService rateLimitingService;
    @Mock private AuditService auditService;
    @Mock private EmailService emailService;
    @Mock private PasswordGenerator passwordGenerator;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@buildledger.com")
                .password("hashed_password")
                .role(UserRole.PROJECT_MANAGER)
                .status(AccountStatus.ACTIVE)
                .failedLoginAttempts(0)
                .accountLocked(false)
                .forcePasswordChange(false)
                .build();

        ReflectionTestUtils.setField(authService, "resetTokenExpiryMinutes", 15);
    }

    @Test
    void login_withValidCredentials_shouldReturnTokens() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@buildledger.com");
        request.setPassword("correctPassword");

        when(userRepository.findByEmail("test@buildledger.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("correctPassword", "hashed_password")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(testUser)).thenReturn("access.token.here");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        LoginResponse response = authService.login(request, "127.0.0.1");

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access.token.here");
        assertThat(response.getUser().getEmail()).isEqualTo("test@buildledger.com");
        assertThat(response.isForcePasswordChange()).isFalse();
    }

    @Test
    void login_withInvalidPassword_shouldThrowAuthException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@buildledger.com");
        request.setPassword("wrongPassword");

        when(userRepository.findByEmail("test@buildledger.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "hashed_password")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void login_withLockedAccount_shouldThrowAccountLockedException() {
        testUser.setAccountLocked(true);
        testUser.setStatus(AccountStatus.LOCKED);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@buildledger.com");
        request.setPassword("anyPassword");

        when(userRepository.findByEmail("test@buildledger.com")).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(AccountLockedException.class);
    }

    @Test
    void login_afterFiveFailedAttempts_shouldLockAccount() {
        testUser.setFailedLoginAttempts(4);

        LoginRequest request = new LoginRequest();
        request.setEmail("test@buildledger.com");
        request.setPassword("wrongPassword");

        when(userRepository.findByEmail("test@buildledger.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(AccountLockedException.class)
                .hasMessageContaining("locked");

        assertThat(testUser.getAccountLocked()).isTrue();
        assertThat(testUser.getStatus()).isEqualTo(AccountStatus.LOCKED);
    }

    @Test
    void login_withUnknownEmail_shouldThrowAuthException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@buildledger.com");
        request.setPassword("anyPassword");

        when(userRepository.findByEmail("unknown@buildledger.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(AuthenticationFailedException.class);
    }
}
