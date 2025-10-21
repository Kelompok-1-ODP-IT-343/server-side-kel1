package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.*;
import com.kelompoksatu.griya.entity.*;
import com.kelompoksatu.griya.repository.RoleRepository;
import com.kelompoksatu.griya.repository.UserRepository;
import com.kelompoksatu.griya.repository.UserSessionRepository;
import com.kelompoksatu.griya.repository.VerificationTokenRepository;
import com.kelompoksatu.griya.util.JwtUtil;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service class for authentication operations */
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  @Value("${app.mail.verification.forgotPasswordExpiredTime}")
  private Long forgotPasswordExpiredTime;

  private final UserService userService;

  private final DeveloperService developerService;

  private final RoleRepository roleRepository;

  private final UserSessionRepository userSessionRepository;

  private final PasswordEncoder passwordEncoder;

  private final JwtUtil jwtUtil;

  private final UserRepository userRepo;

  private final VerificationTokenRepository tokenRepo;

  private final EmailService emailService;

  /** Register a new user */
  public RegisterResponse register(RegisterRequest request) {

    logger.info("Processing registration for username: {}", request.getUsername());

    // Validate password confirmation
    if (!request.getPassword().equals(request.getConfirmPassword())) {
      throw new RuntimeException("Password dan konfirmasi password tidak cocok");
    }

    // Register user through UserService
    Pair<User, Role> result = userService.registerUser(request);
    User user = result.getFirst();
    Role role = result.getSecond();

    UserResponse userResponse = userService.convertToUserResponse(user, role);

    logger.info("User registered successfully: {}", user.getUsername());
    return new RegisterResponse(userResponse);
  }

  /** Register a new developer (admin only) */
  public RegisterDeveloperResponse registerDeveloper(RegisterDeveloperRequest request) {
    logger.info("Processing developer registration for username: {}", request.getUsername());

    try {
      // Validate password
      if (!request.isPasswordMatching()) {
        throw new IllegalArgumentException("Password validation failed");
      }

      // Check if username already exists
      if (userRepo.existsByUsername(request.getUsername())) {
        throw new IllegalArgumentException("Username already exists: " + request.getUsername());
      }

      // Check if email already exists
      if (userRepo.existsByEmail(request.getEmail())) {
        throw new IllegalArgumentException("Email already exists: " + request.getEmail());
      }

      // Check if phone already exists
      if (userRepo.existsByPhone(request.getPhone())) {
        throw new IllegalArgumentException("Phone number already exists: " + request.getPhone());
      }

      // Get DEVELOPER role
      Role developerRole =
          roleRepository
              .findByName("DEVELOPER")
              .orElseThrow(() -> new RuntimeException("DEVELOPER role not found"));

      // Create user account first
      User user = new User();
      user.setUsername(request.getUsername());
      user.setEmail(request.getEmail());
      user.setPhone(request.getPhone());
      user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
      user.setRole(developerRole);
      user.setStatus(UserStatus.ACTIVE); // Admin registration - set as active
      user.setFailedLoginAttempts(0);
      user.setCreatedAt(LocalDateTime.now());
      user.setUpdatedAt(LocalDateTime.now());
      user.setConsentAt(request.getConsentAt());

      User savedUser = userRepo.save(user);
      logger.info("Developer user account created successfully with ID: {}", savedUser.getId());

      // Create developer profile
      CreateDeveloperRequest developerRequest = new CreateDeveloperRequest();
      developerRequest.setCompanyName(request.getCompanyName());
      developerRequest.setCompanyCode(request.getCompanyCode());
      developerRequest.setBusinessLicense(request.getBusinessLicense());
      developerRequest.setDeveloperLicense(request.getDeveloperLicense());
      developerRequest.setContactPerson(request.getContactPerson());
      developerRequest.setPhone(request.getPhone());
      developerRequest.setEmail(request.getEmail());
      developerRequest.setWebsite(request.getWebsite());
      developerRequest.setAddress(request.getAddress());
      developerRequest.setCity(request.getCity());
      developerRequest.setProvince(request.getProvince());
      developerRequest.setPostalCode(request.getPostalCode());
      developerRequest.setEstablishedYear(request.getEstablishedYear());
      developerRequest.setDescription(request.getDescription());
      developerRequest.setSpecialization(request.getSpecialization());
      developerRequest.setIsPartner(request.getIsPartner());
      developerRequest.setPartnershipLevel(request.getPartnershipLevel());
      developerRequest.setCommissionRate(request.getCommissionRate());

      DeveloperResponse developerResponse =
          developerService.createDeveloper(developerRequest, savedUser);

      // Convert to UserResponse for consistency
      UserResponse userResponse = userService.convertToUserResponse(savedUser, developerRole);

      logger.info("Developer registered successfully: {}", savedUser.getUsername());
      return new RegisterDeveloperResponse(userResponse, developerResponse);

    } catch (IllegalArgumentException e) {
      logger.error("Validation error during developer registration: {}", e.getMessage());
      throw e; // Re-throw validation errors as-is
    } catch (Exception e) {
      logger.error("Failed to register developer: {}", e.getMessage());
      throw new RuntimeException("Failed to register developer: " + e.getMessage());
    }
  }

  /** Authenticate user login */
  public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
    try {
      logger.info("Processing login for identifier: {}", request.getIdentifier());

      // Find user by username or email
      User user =
          userService
              .findByUsernameOrEmail(request.getIdentifier())
              .orElseThrow(() -> new RuntimeException("Username atau email tidak ditemukan"));

      // Check if account is locked
      if (userService.isAccountLocked(user)) {
        throw new RuntimeException(
            "Akun terkunci karena terlalu banyak percobaan login yang gagal");
      }

      // Verify password
      if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
        userService.incrementFailedLoginAttempts(user.getId());
        throw new RuntimeException("Password salah");
      }

      // Check if account is active
      if (!userService.isAccountActive(user)) {
        throw new RuntimeException("Akun belum aktif atau telah dinonaktifkan");
      }

      // Reset failed login attempts on successful login
      userService.resetFailedLoginAttempts(user.getId());

      // Update last login time
      userService.updateLastLogin(user.getId());

      // Generate JWT token
      String refreshToken =
          jwtUtil.generateRefreshToken(user.getUsername(), user.getId(), user.getRole().getName());
      // Generate JWT token
      String accessToken =
          jwtUtil.generateAccessToken(user.getUsername(), user.getId(), user.getRole().getName());

      // Create user session
      createUserSession(user.getId(), ipAddress, userAgent, refreshToken);

      logger.info("User logged in successfully: {}", user.getUsername());
      return new AuthResponse(accessToken, refreshToken);

    } catch (Exception e) {
      logger.error("Login failed for identifier {}: {}", request.getIdentifier(), e.getMessage());
      throw new RuntimeException("Login gagal: " + e.getMessage());
    }
  }

  /** Get user profile by token */
  public UserResponse getProfile(String token) {
    try {
      // Extract username from token
      String username = jwtUtil.extractUsername(token);
      Integer userId = jwtUtil.extractUserId(token);

      // Get user profile
      return userService.getUserProfile(userId);

    } catch (Exception e) {
      logger.error("Failed to get user profile: {}", e.getMessage());
      throw new RuntimeException("Gagal mengambil profil user: " + e.getMessage());
    }
  }

  /** Logout user and invalidate session */
  public void logout(String refreshToken) {
    try {
      userSessionRepository
          .findActiveByRefreshToken(jwtUtil.hashToken(refreshToken))
          .ifPresent(
              session -> {
                session.setStatus(SessionStatus.REVOKED);
                session.setLastActivity(LocalDateTime.now());
                userSessionRepository.save(session);
                logger.info("User session revoked for token: {}", refreshToken);
              });
    } catch (Exception e) {
      logger.error("Logout failed: {}", e.getMessage());
      throw new RuntimeException("Logout gagal: " + e.getMessage());
    }
  }

  /** Validate JWT token */
  public boolean validateToken(String token) {
    try {
      return jwtUtil.validateToken(token);
    } catch (Exception e) {
      logger.error("Token validation failed: {}", e.getMessage());
      return false;
    }
  }

  /** Create user session record */
  private void createUserSession(
      Integer userId, String ipAddress, String userAgent, String refreshToken) {
    try {
      UserSession session = new UserSession();
      session.setId(UUID.randomUUID().toString());
      session.setUserId(userId);
      session.setIpAddress(ipAddress);
      session.setUserAgent(userAgent);

      // Create session payload with basic info
      Map<String, Object> payload = new HashMap<>();
      payload.put("loginTime", LocalDateTime.now().toString());
      session.setPayload(payload.toString());

      session.setLastActivity(LocalDateTime.now());
      session.setCreatedAt(LocalDateTime.now());

      session.setRefreshToken(jwtUtil.hashToken(refreshToken));
      session.setStatus(SessionStatus.ACTIVE);
      userSessionRepository.save(session);
      logger.info("User session created for userId: {}", userId);

    } catch (Exception e) {
      logger.error("Failed to create user session: {}", e.getMessage());
      // Don't throw exception here as it's not critical for login process
    }
  }

  /** Clean up expired sessions */
  public void cleanupExpiredSessions() {
    try {
      LocalDateTime expiredBefore =
          LocalDateTime.now().minusDays(30); // Sessions older than 30 days
      userSessionRepository.deleteExpiredSessions(expiredBefore);
      logger.info("Expired sessions cleaned up");
    } catch (Exception e) {
      logger.error("Failed to cleanup expired sessions: {}", e.getMessage());
    }
  }

  @SneakyThrows
  @Transactional
  public String generateEmailVerificationToken(User user, long durationInMinutes) {

    String token = UUID.randomUUID().toString().replace("-", ""); // 32 char
    VerificationToken vt = new VerificationToken();
    vt.setToken(hashToken(token));
    vt.setUser(user);
    vt.setExpiresAt(Instant.now().plus(durationInMinutes, ChronoUnit.MINUTES));

    tokenRepo.save(vt);
    return token;
  }

  /** Verifikasi token dari link email */
  @SneakyThrows
  @Transactional
  public VerifyEmailResponse verifyEmail(String token) {
    String hashedToken = hashToken(token);
    VerificationToken vt =
        tokenRepo
            .findByToken(hashedToken)
            .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
    invalidateToken(vt);
    User user = vt.getUser();
    if (!user.isEmailVerified()) {
      user.setEmailVerifiedAt(LocalDateTime.now());
      user.setStatus(UserStatus.ACTIVE);
      userRepo.save(user);
    }
    return new VerifyEmailResponse(true, "Email verified successfully", user.getEmail());
  }

  private static String hashToken(String token) throws NoSuchAlgorithmException {
    return HexFormat.of()
        .formatHex(
            MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
  }

  @Transactional
  public AuthResponse refreshToken(TokenRefreshRequest request) {
    String oldRefreshToken = request.getRefreshToken();

    // Validate JWT structure and expiration
    jwtUtil.validateToken(oldRefreshToken);

    // Find active session in DB
    UserSession session =
        userSessionRepository
            .findActiveByRefreshToken(jwtUtil.hashToken(oldRefreshToken))
            .orElseThrow(
                () ->
                    new AuthenticationCredentialsNotFoundException(
                        "Refresh token not found or revoked"));

    String username = jwtUtil.extractUsername(oldRefreshToken);

    User user = session.getUser();

    // Generate new tokens
    String newAccessToken =
        jwtUtil.generateAccessToken(user.getUsername(), user.getId(), user.getRole().getName());
    String newRefreshToken =
        jwtUtil.generateRefreshToken(user.getUsername(), user.getId(), user.getRole().getName());

    session.setUserId(user.getId());
    session.setRefreshToken(jwtUtil.hashToken(newRefreshToken));
    session.setIpAddress(request.getIpAddress());
    session.setUserAgent(request.getUserAgent());
    session.setLastActivity(LocalDateTime.now());
    session.setStatus(SessionStatus.ACTIVE);
    userSessionRepository.save(session);

    logger.info("Access token refreshed for user: {}", username);

    return new AuthResponse(newAccessToken, newRefreshToken);
  }

  public void forgotPassword(@Valid ForgotPasswordRequest request) {
    User user =
        userRepo
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Email not found"));
    emailService.sendEmailForgotPassword(
        request.getEmail(),
        generateEmailVerificationToken(user, forgotPasswordExpiredTime),
        forgotPasswordExpiredTime);
  }

  @SneakyThrows
  @Transactional
  public void resetPassword(String tokenValue, String newPassword) {
    VerificationToken token =
        tokenRepo
            .findByToken(hashToken(tokenValue))
            .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
    invalidateToken(token);
    User user = token.getUser();
    if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
      throw new IllegalArgumentException("New password cannot be the same as old password");
    }
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepo.save(user);
  }

  private void invalidateToken(VerificationToken vt) {
    if (vt.isExpired()) {
      throw new IllegalStateException("Token expired");
    }
    if (vt.isUsed()) {
      throw new IllegalStateException("Token used");
    }
    vt.setUsedAt(Instant.now());
    tokenRepo.save(vt);
  }
}
