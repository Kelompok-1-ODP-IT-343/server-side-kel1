package com.kelompoksatu.griya.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.kelompoksatu.griya.dto.*;
import com.kelompoksatu.griya.entity.*;
import com.kelompoksatu.griya.repository.RoleRepository;
import com.kelompoksatu.griya.repository.UserRepository;
import com.kelompoksatu.griya.repository.UserSessionRepository;
import com.kelompoksatu.griya.repository.VerificationTokenRepository;
import com.kelompoksatu.griya.util.JwtUtil;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

/** Unit tests for AuthService class Following Spring Boot testing best practices with Mockito */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

  @Mock private UserService userService;

  @Mock private RoleRepository roleRepository;

  @Mock private UserSessionRepository userSessionRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtUtil jwtUtil;

  @Mock private UserRepository userRepo;

  @Mock private VerificationTokenRepository tokenRepo;

  @Mock private EmailService emailService;

  @InjectMocks private AuthService authService;

  private User testUser;
  private Role testRole;
  private RegisterRequest registerRequest;
  private LoginRequest loginRequest;
  private UserSession testSession;

  @BeforeEach
  void setUp() {
    // Setup test role
    testRole = new Role();
    testRole.setId(1);
    testRole.setName("USER");

    // Setup test user
    testUser = new User();
    testUser.setId(1);
    testUser.setUsername("testuser");
    testUser.setEmail("test@example.com");
    testUser.setPasswordHash("encodedPassword");
    testUser.setRole(testRole);
    testUser.setStatus(UserStatus.ACTIVE);
    testUser.setFailedLoginAttempts(0);
    testUser.setEmailVerifiedAt(LocalDateTime.now());
    testUser.setCreatedAt(LocalDateTime.now());
    testUser.setUpdatedAt(LocalDateTime.now());

    // Setup register request
    registerRequest = new RegisterRequest();
    registerRequest.setUsername("testuser");
    registerRequest.setEmail("test@example.com");
    registerRequest.setPassword("password123");
    registerRequest.setConfirmPassword("password123");
    registerRequest.setPhone("08123456789");
    registerRequest.setFullName("Test User");
    registerRequest.setNik("1234567890123456");
    registerRequest.setNpwp("1234567890123456");
    registerRequest.setBirthDate(LocalDate.now().minusYears(25));
    registerRequest.setBirthPlace("Jakarta");
    registerRequest.setGender(Gender.MALE);
    registerRequest.setMaritalStatus(MaritalStatus.SINGLE);
    registerRequest.setAddress("Test Address");
    registerRequest.setCity("Jakarta");
    registerRequest.setProvince("DKI Jakarta");
    registerRequest.setPostalCode("12345");
    registerRequest.setOccupation("Developer");
    registerRequest.setCompanyName("Test Company");
    registerRequest.setMonthlyIncome(new BigDecimal(10000000L));
    registerRequest.setWorkExperience(5);

    // Setup login request
    loginRequest = new LoginRequest();
    loginRequest.setIdentifier("testuser");
    loginRequest.setPassword("password123");

    // Setup test session
    testSession = new UserSession();
    testSession.setId(UUID.randomUUID().toString());
    testSession.setUserId(1);
    testSession.setIpAddress("127.0.0.1");
    testSession.setUserAgent("Test Agent");
    testSession.setStatus(SessionStatus.ACTIVE);
    testSession.setRefreshToken("hashedRefreshToken");
    testSession.setLastActivity(LocalDateTime.now());
    testSession.setCreatedAt(LocalDateTime.now());

    // Set up @Value fields that are not injected in unit tests
    ReflectionTestUtils.setField(authService, "forgotPasswordExpiredTime", 15L);
  }

  @Test
  @DisplayName("Should register user successfully with valid request")
  void shouldRegisterUserSuccessfully() {
    // Given
    UserResponse userResponse = new UserResponse();
    userResponse.setId(1);
    userResponse.setUsername("testuser");
    userResponse.setEmail("test@example.com");
    userResponse.setRoleName("USER");
    userResponse.setStatus(UserStatus.PENDING_VERIFICATION);

    when(userService.registerUser(registerRequest)).thenReturn(Pair.of(testUser, testRole));
    when(userService.convertToUserResponse(testUser, testRole)).thenReturn(userResponse);

    // When
    RegisterResponse result = authService.register(registerRequest);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getUser()).isEqualTo(userResponse);
    verify(userService).registerUser(registerRequest);
    verify(userService).convertToUserResponse(testUser, testRole);
  }

  @Test
  @DisplayName("Should throw exception when password confirmation does not match")
  void shouldThrowExceptionWhenPasswordConfirmationDoesNotMatch() {
    // Given
    registerRequest.setConfirmPassword("differentPassword");

    // When & Then
    assertThatThrownBy(() -> authService.register(registerRequest))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Password dan konfirmasi password tidak cocok");

    verify(userService, never()).registerUser(any());
  }

  @Test
  @DisplayName("Should login user successfully with valid credentials")
  void shouldLoginUserSuccessfully() {
    // Given
    String ipAddress = "127.0.0.1";
    String userAgent = "Test Agent";
    String accessToken = "accessToken";
    String refreshToken = "refreshToken";

    when(userService.findByUsernameOrEmail(loginRequest.getIdentifier()))
        .thenReturn(Optional.of(testUser));
    when(userService.isAccountLocked(testUser)).thenReturn(false);
    when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash()))
        .thenReturn(true);
    when(userService.isAccountActive(testUser)).thenReturn(true);
    when(jwtUtil.generateRefreshToken(
            testUser.getUsername(), testUser.getId(), testUser.getRole().getName()))
        .thenReturn(refreshToken);
    when(jwtUtil.generateAccessToken(
            testUser.getUsername(), testUser.getId(), testUser.getRole().getName()))
        .thenReturn(accessToken);

    // When
    AuthResponse result = authService.login(loginRequest, ipAddress, userAgent);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getToken()).isEqualTo(accessToken);
    assertThat(result.getRefreshToken()).isEqualTo(refreshToken);

    verify(userService).findByUsernameOrEmail(loginRequest.getIdentifier());
    verify(userService).isAccountLocked(testUser);
    verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPasswordHash());
    verify(userService).isAccountActive(testUser);
    verify(userService).resetFailedLoginAttempts(testUser.getId());
    verify(userService).updateLastLogin(testUser.getId());
    verify(jwtUtil)
        .generateRefreshToken(
            testUser.getUsername(), testUser.getId(), testUser.getRole().getName());
    verify(jwtUtil)
        .generateAccessToken(
            testUser.getUsername(), testUser.getId(), testUser.getRole().getName());
    verify(userSessionRepository).save(any(UserSession.class));
  }

  @Test
  @DisplayName("Should throw exception when user not found")
  void shouldThrowExceptionWhenUserNotFound() {
    // Given
    when(userService.findByUsernameOrEmail(loginRequest.getIdentifier()))
        .thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> authService.login(loginRequest, "127.0.0.1", "Test Agent"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Login gagal: Username atau email tidak ditemukan");

    verify(userService).findByUsernameOrEmail(loginRequest.getIdentifier());
    verify(userService, never()).isAccountLocked(any());
  }

  @Test
  @DisplayName("Should throw exception when account is locked")
  void shouldThrowExceptionWhenAccountIsLocked() {
    // Given
    when(userService.findByUsernameOrEmail(loginRequest.getIdentifier()))
        .thenReturn(Optional.of(testUser));
    when(userService.isAccountLocked(testUser)).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> authService.login(loginRequest, "127.0.0.1", "Test Agent"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Login gagal: Akun terkunci karena terlalu banyak percobaan login yang gagal");

    verify(userService).findByUsernameOrEmail(loginRequest.getIdentifier());
    verify(userService).isAccountLocked(testUser);
    verify(passwordEncoder, never()).matches(any(), any());
  }

  @Test
  @DisplayName("Should throw exception when password is incorrect")
  void shouldThrowExceptionWhenPasswordIsIncorrect() {
    // Given
    when(userService.findByUsernameOrEmail(loginRequest.getIdentifier()))
        .thenReturn(Optional.of(testUser));
    when(userService.isAccountLocked(testUser)).thenReturn(false);
    when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash()))
        .thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> authService.login(loginRequest, "127.0.0.1", "Test Agent"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Login gagal: Password salah");

    verify(userService).findByUsernameOrEmail(loginRequest.getIdentifier());
    verify(userService).isAccountLocked(testUser);
    verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPasswordHash());
    verify(userService).incrementFailedLoginAttempts(testUser.getId());
  }

  @Test
  @DisplayName("Should throw exception when account is not active")
  void shouldThrowExceptionWhenAccountIsNotActive() {
    // Given
    when(userService.findByUsernameOrEmail(loginRequest.getIdentifier()))
        .thenReturn(Optional.of(testUser));
    when(userService.isAccountLocked(testUser)).thenReturn(false);
    when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPasswordHash()))
        .thenReturn(true);
    when(userService.isAccountActive(testUser)).thenReturn(false);

    // When & Then
    assertThatThrownBy(() -> authService.login(loginRequest, "127.0.0.1", "Test Agent"))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Login gagal: Akun belum aktif atau telah dinonaktifkan");

    verify(userService).findByUsernameOrEmail(loginRequest.getIdentifier());
    verify(userService).isAccountLocked(testUser);
    verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPasswordHash());
    verify(userService).isAccountActive(testUser);
    verify(userService, never()).resetFailedLoginAttempts(any());
  }

  @Test
  @DisplayName("Should get user profile successfully")
  void shouldGetUserProfileSuccessfully() {
    // Given
    String token = "validToken";
    String username = "testuser";
    Integer userId = 1;
    UserResponse userResponse = new UserResponse();
    userResponse.setId(userId);
    userResponse.setUsername(username);

    when(jwtUtil.extractUsername(token)).thenReturn(username);
    when(jwtUtil.extractUserId(token)).thenReturn(userId);
    when(userService.getUserProfile(userId)).thenReturn(userResponse);

    // When
    UserResponse result = authService.getProfile(token);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(userId);
    assertThat(result.getUsername()).isEqualTo(username);

    verify(jwtUtil).extractUsername(token);
    verify(jwtUtil).extractUserId(token);
    verify(userService).getUserProfile(userId);
  }

  @Test
  @DisplayName("Should logout user successfully")
  void shouldLogoutUserSuccessfully() {
    // Given
    String refreshToken = "refreshToken";
    String hashedToken = "hashedToken";

    when(jwtUtil.hashToken(refreshToken)).thenReturn(hashedToken);
    when(userSessionRepository.findActiveByRefreshToken(hashedToken))
        .thenReturn(Optional.of(testSession));

    // When
    authService.logout(refreshToken);

    // Then
    verify(jwtUtil).hashToken(refreshToken);
    verify(userSessionRepository).findActiveByRefreshToken(hashedToken);
    verify(userSessionRepository).save(testSession);
    assertThat(testSession.getStatus()).isEqualTo(SessionStatus.REVOKED);
  }

  @Test
  @DisplayName("Should handle logout when session not found")
  void shouldHandleLogoutWhenSessionNotFound() {
    // Given
    String refreshToken = "refreshToken";
    String hashedToken = "hashedToken";

    when(jwtUtil.hashToken(refreshToken)).thenReturn(hashedToken);
    when(userSessionRepository.findActiveByRefreshToken(hashedToken)).thenReturn(Optional.empty());

    // When
    authService.logout(refreshToken);

    // Then
    verify(jwtUtil).hashToken(refreshToken);
    verify(userSessionRepository).findActiveByRefreshToken(hashedToken);
    verify(userSessionRepository, never()).save(any());
  }

  @Test
  @DisplayName("Should validate token successfully")
  void shouldValidateTokenSuccessfully() {
    // Given
    String token = "validToken";
    when(jwtUtil.validateToken(token)).thenReturn(true);

    // When
    boolean result = authService.validateToken(token);

    // Then
    assertThat(result).isTrue();
    verify(jwtUtil).validateToken(token);
  }

  @Test
  @DisplayName("Should return false for invalid token")
  void shouldReturnFalseForInvalidToken() {
    // Given
    String token = "invalidToken";
    when(jwtUtil.validateToken(token)).thenReturn(false);

    // When
    boolean result = authService.validateToken(token);

    // Then
    assertThat(result).isFalse();
    verify(jwtUtil).validateToken(token);
  }

  @Test
  @DisplayName("Should generate email verification token successfully")
  void shouldGenerateEmailVerificationTokenSuccessfully() {
    // Given
    Integer userId = 1;
    long durationInMinutes = 30;
    String token = "generatedToken";

    when(tokenRepo.save(any(VerificationToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    String result = authService.generateEmailVerificationToken(testUser, durationInMinutes);

    // Then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(32); // UUID without dashes
    verify(tokenRepo).save(any(VerificationToken.class));
  }

  @Test
  @DisplayName("Should verify email successfully")
  void shouldVerifyEmailSuccessfully() throws NoSuchAlgorithmException {
    // Given
    String token = "validToken";
    String hashedToken = calculateHash(token);

    // Create a user that is not email verified
    User unverifiedUser = new User();
    unverifiedUser.setId(1);
    unverifiedUser.setUsername("testuser");
    unverifiedUser.setEmail("test@example.com");
    unverifiedUser.setPasswordHash("encodedPassword");
    unverifiedUser.setRole(testRole);
    unverifiedUser.setStatus(UserStatus.PENDING_VERIFICATION);
    unverifiedUser.setFailedLoginAttempts(0);
    unverifiedUser.setEmailVerifiedAt(null); // Not verified yet
    unverifiedUser.setCreatedAt(LocalDateTime.now());
    unverifiedUser.setUpdatedAt(LocalDateTime.now());

    VerificationToken verificationToken = new VerificationToken();
    verificationToken.setToken(hashedToken);
    verificationToken.setUser(unverifiedUser);
    verificationToken.setExpiresAt(Instant.now().plusSeconds(3600));
    verificationToken.setUsedAt(null);

    when(tokenRepo.findByToken(hashedToken)).thenReturn(Optional.of(verificationToken));
    when(userRepo.save(any(User.class))).thenReturn(unverifiedUser);

    // When
    VerifyEmailResponse result = authService.verifyEmail(token);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.success()).isTrue();
    assertThat(result.message()).isEqualTo("Email verified successfully");
    assertThat(result.email()).isEqualTo(unverifiedUser.getEmail());

    verify(tokenRepo).findByToken(hashedToken);
    verify(userRepo).save(unverifiedUser);
    verify(tokenRepo).save(verificationToken);
  }

  @Test
  @DisplayName("Should throw exception for invalid verification token")
  void shouldThrowExceptionForInvalidVerificationToken() throws NoSuchAlgorithmException {
    // Given
    String token = "invalidToken";
    String hashedToken = calculateHash(token);

    when(tokenRepo.findByToken(hashedToken)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> authService.verifyEmail(token))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid token");

    verify(tokenRepo).findByToken(hashedToken);
    verify(userRepo, never()).save(any());
  }

  @Test
  @DisplayName("Should refresh token successfully")
  void shouldRefreshTokenSuccessfully() {
    // Given
    TokenRefreshRequest request = new TokenRefreshRequest();
    request.setRefreshToken("oldRefreshToken");

    String username = "testuser";
    String newAccessToken = "newAccessToken";
    String newRefreshToken = "newRefreshToken";

    when(userSessionRepository.findActiveByRefreshToken("oldRefreshToken"))
        .thenReturn(Optional.of(testSession));
    when(jwtUtil.extractUsername("oldRefreshToken")).thenReturn(username);
    when(userRepo.findByUsernameWithRole(username)).thenReturn(Optional.of(testUser));
    when(jwtUtil.generateAccessToken(username, testUser.getId(), testUser.getRole().getName()))
        .thenReturn(newAccessToken);
    when(jwtUtil.generateRefreshToken(username, testUser.getId(), testUser.getRole().getName()))
        .thenReturn(newRefreshToken);
    when(jwtUtil.hashToken(newRefreshToken)).thenReturn("hashedNewRefreshToken");

    // When
    AuthResponse result = authService.refreshToken(request);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getToken()).isEqualTo(newAccessToken);
    assertThat(result.getRefreshToken()).isEqualTo(newRefreshToken);

    verify(userSessionRepository).findActiveByRefreshToken("oldRefreshToken");
    verify(jwtUtil).extractUsername("oldRefreshToken");
    verify(userRepo).findByUsernameWithRole(username);
    verify(userSessionRepository, times(2)).save(any(UserSession.class));
  }

  @Test
  @DisplayName("Should throw exception when refresh token not found")
  void shouldThrowExceptionWhenRefreshTokenNotFound() {
    // Given
    TokenRefreshRequest request = new TokenRefreshRequest();
    request.setRefreshToken("invalidRefreshToken");

    when(userSessionRepository.findActiveByRefreshToken("invalidRefreshToken"))
        .thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> authService.refreshToken(request))
        .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
        .hasMessage("Refresh token not found or revoked");

    verify(userSessionRepository).findActiveByRefreshToken("invalidRefreshToken");
    verify(jwtUtil, never()).extractUsername(any());
  }

  @Test
  @DisplayName("Should throw exception when user not found during token refresh")
  void shouldThrowExceptionWhenUserNotFoundDuringTokenRefresh() {
    // Given
    TokenRefreshRequest request = new TokenRefreshRequest();
    request.setRefreshToken("validRefreshToken");
    String username = "nonexistentuser";

    when(userSessionRepository.findActiveByRefreshToken("validRefreshToken"))
        .thenReturn(Optional.of(testSession));
    when(jwtUtil.extractUsername("validRefreshToken")).thenReturn(username);
    when(userRepo.findByUsernameWithRole(username)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> authService.refreshToken(request))
        .isInstanceOf(UsernameNotFoundException.class)
        .hasMessage("User not found");

    verify(userSessionRepository).findActiveByRefreshToken("validRefreshToken");
    verify(jwtUtil).extractUsername("validRefreshToken");
    verify(userRepo).findByUsernameWithRole(username);
  }

  @Test
  @DisplayName("Should send forgot password email successfully")
  void shouldSendForgotPasswordEmailSuccessfully() {
    // Given
    ForgotPasswordRequest request = new ForgotPasswordRequest();
    request.setEmail(testUser.getEmail());
    String token = "forgotPasswordToken";

    when(userRepo.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
    when(userRepo.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
    when(tokenRepo.save(any(VerificationToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // When
    authService.forgotPassword(request);

    // Then
    verify(userRepo).findByEmail(request.getEmail());
    verify(emailService).sendEmailForgotPassword(eq(request.getEmail()), anyString(), anyLong());
    verify(tokenRepo).save(any(VerificationToken.class));
  }

  @Test
  @DisplayName("Should throw exception when email not found for forgot password")
  void shouldThrowExceptionWhenEmailNotFoundForForgotPassword() {
    // Given
    ForgotPasswordRequest request = new ForgotPasswordRequest();
    request.setEmail("nonexistent@example.com");

    when(userRepo.findByEmail(request.getEmail())).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> authService.forgotPassword(request))
        .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
        .hasMessage("Email not found");

    verify(userRepo).findByEmail(request.getEmail());
    verify(emailService, never()).sendEmailForgotPassword(any(), any(), eq(15L));
  }

  @Test
  @DisplayName("Should reset password successfully")
  void shouldResetPasswordSuccessfully() throws NoSuchAlgorithmException {
    // Given
    String token = "resetToken";
    String newPassword = "newPassword123";
    String hashedToken = calculateHash(token);
    VerificationToken verificationToken = new VerificationToken();
    verificationToken.setToken(hashedToken);
    verificationToken.setUser(testUser);
    verificationToken.setExpiresAt(Instant.now().plusSeconds(3600));
    verificationToken.setUsedAt(null);

    when(tokenRepo.findByToken(hashedToken)).thenReturn(Optional.of(verificationToken));
    when(passwordEncoder.matches(newPassword, testUser.getPasswordHash())).thenReturn(false);
    when(passwordEncoder.encode(newPassword)).thenReturn("encodedPassword");
    when(userRepo.save(any(User.class))).thenReturn(testUser);

    // When
    authService.resetPassword(token, newPassword);

    // Then
    verify(tokenRepo).findByToken(hashedToken);
    verify(passwordEncoder).matches(newPassword, testUser.getPasswordHash());
    verify(passwordEncoder).encode(newPassword);
    verify(userRepo).save(testUser);
    verify(tokenRepo).save(verificationToken);
  }

  @Test
  @DisplayName("Should throw exception when new password is same as old password")
  void shouldThrowExceptionWhenNewPasswordIsSameAsOldPassword() throws NoSuchAlgorithmException {
    // Given
    String token = "resetToken";
    String newPassword = "samePassword";
    String hashedToken = calculateHash(token);
    VerificationToken verificationToken = new VerificationToken();
    verificationToken.setToken(hashedToken);
    verificationToken.setUser(testUser);
    verificationToken.setExpiresAt(Instant.now().plusSeconds(3600));
    verificationToken.setUsedAt(null);

    when(tokenRepo.findByToken(hashedToken)).thenReturn(Optional.of(verificationToken));
    when(passwordEncoder.matches(newPassword, testUser.getPasswordHash())).thenReturn(true);

    // When & Then
    assertThatThrownBy(() -> authService.resetPassword(token, newPassword))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("New password cannot be the same as old password");

    verify(tokenRepo).findByToken(hashedToken);
    verify(passwordEncoder).matches(newPassword, testUser.getPasswordHash());
    verify(userRepo, never()).save(any());
  }

  @Test
  @DisplayName("Should cleanup expired sessions successfully")
  void shouldCleanupExpiredSessionsSuccessfully() {
    // Given
    LocalDateTime expiredBefore = LocalDateTime.now().minusDays(30);

    // When
    authService.cleanupExpiredSessions();

    // Then
    verify(userSessionRepository).deleteExpiredSessions(any(LocalDateTime.class));
  }

  /**
   * Helper method to calculate SHA-256 hash of a token This matches the hashToken method in
   * AuthService
   */
  private String calculateHash(String token) throws NoSuchAlgorithmException {
    return HexFormat.of()
        .formatHex(
            MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
  }
}
