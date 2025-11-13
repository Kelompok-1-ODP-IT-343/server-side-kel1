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
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service class for authentication operations */
@Service
@Transactional
public class AuthService {

  private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

  @Value("${app.mail.verification.forgotPasswordExpiredTime}")
  private Long forgotPasswordExpiredTime;

  @Autowired private UserService userService;
  @Autowired private DeveloperService developerService;
  @Autowired private RoleRepository roleRepository;
  @Autowired private UserSessionRepository userSessionRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private UserRepository userRepo;
  @Autowired private VerificationTokenRepository tokenRepo;
  @Autowired private EmailService emailService;
  @Autowired private OtpService otpService;

  // ========================================
  // REGISTRATION OPERATIONS
  // ========================================

  /** Register a new user - sends OTP instead of returning tokens immediately */
  @Transactional
  public OtpResponse register(RegisterRequest request) {
    logger.info("Processing registration for username: {}", request.getUsername());

    validatePasswordConfirmation(request.getPassword(), request.getConfirmPassword());

    // Register user through UserService
    Pair<User, Role> result = userService.registerUser(request);
    User user = result.getFirst();
    Role role = result.getSecond();

    // Send OTP to user's phone number instead of returning user data immediately
    OtpResponse otpResponse = otpService.sendOtp(user.getPhone(), "registration");

    logger.info("User registered successfully and OTP sent: {}", user.getUsername());
    return otpResponse;
  }

  /** Register a new developer (admin only) */
  public RegisterDeveloperResponse registerDeveloper(RegisterDeveloperRequest request) {
    logger.info("Processing developer registration for username: {}", request.getUsername());

    try {
      validateDeveloperRegistrationRequest(request);
      Role developerRole = validateAndGetDeveloperRole();

      User savedUser = createDeveloperUser(request, developerRole);
      DeveloperResponse developerResponse = createDeveloperProfile(request, savedUser);
      UserResponse userResponse = userService.convertToUserResponse(savedUser, null, null);

      logger.info("Developer registered successfully: {}", savedUser.getUsername());
      return new RegisterDeveloperResponse(userResponse, developerResponse);

    } catch (IllegalArgumentException e) {
      logger.error("Validation error during developer registration: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      logger.error("Failed to register developer: {}", e.getMessage());
      throw new RuntimeException("Failed to register developer: " + e.getMessage());
    }
  }

  // ========================================
  // AUTHENTICATION OPERATIONS
  // ========================================

  /** Authenticate user login - sends OTP instead of returning tokens immediately */
  public OtpResponse login(LoginRequest request, String ipAddress, String userAgent) {
    try {
      logger.info("Processing login for identifier: {}", request.getIdentifier());

      User user = validateLoginCredentials(request);
      validateAccountStatus(user);

      // Reset failed login attempts on successful login
      userService.resetLogin(user.getId());

      // Send OTP to user's phone number instead of returning tokens immediately
      OtpResponse otpResponse = otpService.sendOtp(user.getPhone(), "login");

      if (otpResponse.isSuccess()) {
        logger.info("OTP sent successfully for login to user: {}", user.getUsername());
      } else {
        String errorType =
            otpResponse.getErrorDetail() != null
                ? String.valueOf(otpResponse.getErrorDetail().getErrorType())
                : "UNKNOWN";
        String detail =
            otpResponse.getErrorDetail() != null
                ? otpResponse.getErrorDetail().getMessage()
                : otpResponse.getMessage();
        logger.warn(
            "Failed to send OTP for login to user: {} - type: {}, detail: {}",
            user.getUsername(),
            errorType,
            detail);
      }
      return otpResponse;

    } catch (Exception e) {
      logger.error("Login failed for identifier {}: {}", request.getIdentifier(), e.getMessage());
      throw new RuntimeException("Login gagal: " + e.getMessage());
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

  // ========================================
  // TOKEN OPERATIONS
  // ========================================

  /** Validate JWT token */
  public boolean validateToken(String token) {
    try {
      return jwtUtil.validateToken(token);
    } catch (Exception e) {
      logger.error("Token validation failed: {}", e.getMessage());
      return false;
    }
  }

  /** Refresh access token */
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

    updateUserSession(session, user, newRefreshToken, request);

    logger.info("Access token refreshed for user: {}", username);
    return new AuthResponse(newAccessToken, newRefreshToken);
  }

  // ========================================
  // EMAIL VERIFICATION OPERATIONS
  // ========================================

  /** Generate email verification token */
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

  /** Verify email token from link */
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

  // ========================================
  // PASSWORD RESET OPERATIONS
  // ========================================

  /** Send forgot password email */
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

  /** Reset password with token */
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

  // ========================================
  // USER PROFILE OPERATIONS
  // ========================================

  /** Get user profile by token */
  public UserResponse getProfile(String token) {
    try {
      String username = jwtUtil.extractUsername(token);
      Integer userId = jwtUtil.extractUserId(token);

      return userService.getUserProfile(userId);

    } catch (Exception e) {
      logger.error("Failed to get user profile: {}", e.getMessage());
      throw new RuntimeException("Gagal mengambil profil user: " + e.getMessage());
    }
  }

  // ========================================
  // OTP VERIFICATION OPERATIONS
  // ========================================

  /** Verify OTP for login and return tokens */
  public AuthResponse verifyLoginOtp(
      String identifier, String otp, String purpose, String ipAddress, String userAgent) {
    try {
      logger.info(
          "Processing OTP verification for identifier: {} with purpose: {}", identifier, purpose);

      // Find user by identifier (username or email)
      User user =
          userService
              .findByUsernameOrEmail(identifier)
              .orElseThrow(
                  () -> new RuntimeException("User tidak ditemukan dengan identifier tersebut"));

      // Verify OTP using user's phone number
      boolean isOtpValid = otpService.verifyOtp(user.getPhone(), otp, purpose);

      if (!isOtpValid) {
        throw new RuntimeException("OTP tidak valid atau sudah kedaluwarsa");
      }

      // Generate tokens and create session
      String refreshToken =
          jwtUtil.generateRefreshToken(user.getUsername(), user.getId(), user.getRole().getName());
      String accessToken =
          jwtUtil.generateAccessToken(user.getUsername(), user.getId(), user.getRole().getName());

      createUserSession(user.getId(), ipAddress, userAgent, refreshToken);

      logger.info(
          "OTP verified successfully and tokens generated for user: {}", user.getUsername());
      return new AuthResponse(accessToken, refreshToken);

    } catch (Exception e) {
      logger.error("OTP verification failed for identifier {}: {}", identifier, e.getMessage());
      throw new RuntimeException("Verifikasi OTP gagal: " + e.getMessage());
    }
  }

  /** Verify OTP for registration and return user data */
  public RegisterResponse verifyRegistrationOtp(String identifier, String otp, String purpose) {
    try {
      logger.info(
          "Processing registration OTP verification for identifier: {} with purpose: {}",
          identifier,
          purpose);

      // Find user by identifier (username or email) first
      User user =
          userService
              .findByUsernameOrEmail(identifier)
              .orElseThrow(
                  () -> new RuntimeException("User tidak ditemukan dengan identifier tersebut"));

      // Get phone number from user for OTP verification
      String phone = user.getPhone();
      if (phone == null || phone.isEmpty()) {
        throw new RuntimeException("Nomor telepon user tidak ditemukan");
      }

      // Verify OTP using OtpService with phone number
      boolean isOtpValid = otpService.verifyOtp(phone, otp, purpose);

      if (!isOtpValid) {
        throw new RuntimeException("OTP tidak valid atau sudah kedaluwarsa");
      }

      // Activate user account after successful OTP verification
      if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
        user.setStatus(UserStatus.ACTIVE);
        user.setPhoneVerifiedAt(LocalDateTime.now());
        userRepo.save(user);
      }

      UserResponse userResponse = userService.convertToUserResponse(user, user.getRole(), null);

      logger.info("Registration OTP verified successfully for user: {}", user.getUsername());
      return new RegisterResponse(userResponse);

    } catch (Exception e) {
      logger.error(
          "Registration OTP verification failed for identifier {}: {}", identifier, e.getMessage());
      throw new RuntimeException("Verifikasi OTP registrasi gagal: " + e.getMessage());
    }
  }

  /** Mask phone number for logging (show only first 2 and last 4 digits) */
  private String maskPhoneNumber(String phone) {
    if (phone == null || phone.length() < 6) {
      return "****";
    }

    String cleanPhone = phone.replaceAll("[^0-9]", "");
    if (cleanPhone.length() < 6) {
      return "****";
    }

    return cleanPhone.substring(0, 2) + "****" + cleanPhone.substring(cleanPhone.length() - 4);
  }

  // ========================================
  // SESSION MANAGEMENT OPERATIONS
  // ========================================

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

  // ========================================
  // PRIVATE VALIDATION METHODS
  // ========================================

  private void validatePasswordConfirmation(String password, String confirmPassword) {
    if (!password.equals(confirmPassword)) {
      throw new RuntimeException("Password dan konfirmasi password tidak cocok");
    }
  }

  private void validateDeveloperRegistrationRequest(RegisterDeveloperRequest request) {
    if (!request.isPasswordMatching()) {
      throw new IllegalArgumentException("Password validation failed");
    }

    if (userRepo.existsByUsername(request.getUsername())) {
      throw new IllegalArgumentException("Username already exists: " + request.getUsername());
    }

    if (userRepo.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email already exists: " + request.getEmail());
    }

    if (userRepo.existsByPhone(request.getPhone())) {
      throw new IllegalArgumentException("Phone number already exists: " + request.getPhone());
    }
  }

  private Role validateAndGetDeveloperRole() {
    return roleRepository
        .findByName("DEVELOPER")
        .orElseThrow(() -> new RuntimeException("DEVELOPER role not found"));
  }

  private User validateLoginCredentials(LoginRequest request) {
    User user =
        userService
            .findByUsernameOrEmail(request.getIdentifier())
            .orElseThrow(() -> new RuntimeException("Username atau email tidak ditemukan"));

    if (userService.isAccountLocked(user)) {
      throw new RuntimeException("Akun terkunci karena terlalu banyak percobaan login yang gagal");
    }

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      userService.incrementFailedLoginAttempts(user);
      throw new RuntimeException("Password salah");
    }

    return user;
  }

  private void validateAccountStatus(User user) {
    if (!userService.isAccountActive(user)) {
      throw new RuntimeException("Akun belum aktif atau telah dinonaktifkan");
    }
  }

  // ========================================
  // PRIVATE BUSINESS LOGIC METHODS
  // ========================================

  private User createDeveloperUser(RegisterDeveloperRequest request, Role developerRole) {
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
    return savedUser;
  }

  private DeveloperResponse createDeveloperProfile(
      RegisterDeveloperRequest request, User savedUser) {
    CreateDeveloperRequest developerRequest = new CreateDeveloperRequest();
    setDeveloperRequestFields(developerRequest, request);

    return developerService.createDeveloper(developerRequest, savedUser);
  }

  private void setDeveloperRequestFields(
      CreateDeveloperRequest developerRequest, RegisterDeveloperRequest request) {
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
  }

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

  private void updateUserSession(
      UserSession session, User user, String newRefreshToken, TokenRefreshRequest request) {
    session.setUserId(user.getId());
    session.setRefreshToken(jwtUtil.hashToken(newRefreshToken));
    session.setIpAddress(request.getIpAddress());
    session.setUserAgent(request.getUserAgent());
    session.setLastActivity(LocalDateTime.now());
    session.setStatus(SessionStatus.ACTIVE);
    userSessionRepository.save(session);
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

  // ========================================
  // PRIVATE UTILITY METHODS
  // ========================================

  private static String hashToken(String token) throws NoSuchAlgorithmException {
    return HexFormat.of()
        .formatHex(
            MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
  }
}
