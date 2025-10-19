package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.RegisterRequest;
import com.kelompoksatu.griya.dto.UserResponse;
import com.kelompoksatu.griya.entity.Role;
import com.kelompoksatu.griya.entity.User;
import com.kelompoksatu.griya.entity.UserProfile;
import com.kelompoksatu.griya.entity.UserStatus;
import com.kelompoksatu.griya.repository.RoleRepository;
import com.kelompoksatu.griya.repository.UserProfileRepository;
import com.kelompoksatu.griya.repository.UserRepository;
import jakarta.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.util.Pair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service class for user management operations */
@Service
@Transactional
public class UserService {

  private static final Logger logger = LoggerFactory.getLogger(UserService.class);

  @Value("${app.mail.verification.emailExpiredTime}")
  private Long emailVerificationExpiredTime;

  @Autowired private UserRepository userRepository;

  @Autowired private UserProfileRepository userProfileRepository;

  @Autowired private RoleRepository roleRepository;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private EmailService emailService;

  @Autowired private AuthService authService;

  /** Register a new user with comprehensive profile information */
  public Pair<User, Role> registerUser(RegisterRequest request) {
    logger.info("Attempting to register user: {}", request.getUsername());

    if (!request.isPasswordMatching()) {
      throw new ValidationException("Password dan konfirmasi password tidak cocok");
    }

    Role userRole =
        roleRepository
            .findByName("USER")
            .orElseThrow(() -> new RuntimeException("Default role 'USER' not found"));

    // Create new user
    User user = new User();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setPhone(request.getPhone());
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setRole(userRole);
    user.setStatus(UserStatus.PENDING_VERIFICATION);
    user.setFailedLoginAttempts(0);
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    user.setConsentAt(request.getConsentAt());
    try {
      User savedUser = userRepository.save(user);
      logger.info("User registered successfully with ID: {}", savedUser.getId());

      // Create user profile
      UserProfile userProfile = new UserProfile();
      userProfile.setUserId(savedUser.getId());
      userProfile.setFullName(request.getFullName());
      userProfile.setNik(request.getNik());
      userProfile.setNpwp(request.getNpwp());
      userProfile.setBirthDate(request.getBirthDate());
      userProfile.setBirthPlace(request.getBirthPlace());
      userProfile.setGender(request.getGender());
      userProfile.setMaritalStatus(request.getMaritalStatus());
      userProfile.setAddress(request.getAddress());
      userProfile.setCity(request.getCity());
      userProfile.setProvince(request.getProvince());
      userProfile.setPostalCode(request.getPostalCode());
      userProfile.setOccupation(request.getOccupation());
      userProfile.setCompanyName(request.getCompanyName());
      userProfile.setMonthlyIncome(request.getMonthlyIncome());
      userProfile.setWorkExperience(request.getWorkExperience());

      UserProfile savedProfile = userProfileRepository.save(userProfile);
      sendEmailVerification(savedUser);
      logger.info("User profile created successfully for user ID: {}", savedUser.getId());

      return Pair.of(savedUser, userRole);
    } catch (DataIntegrityViolationException ex) {
      throw ex;
    }
  }

  /** Find user by username */
  public Optional<User> findByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  /** Find user by email */
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  /** Find user by username or email */
  public Optional<User> findByUsernameOrEmail(String identifier) {
    return userRepository.findByUsernameOrEmail(identifier);
  }

  /** Get user profile by user ID */
  public UserResponse getUserProfile(Integer userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

    return convertToUserResponse(user, user.getRole());
  }

  /** Update user last login time */
  public void updateLastLogin(Integer userId) {
    userRepository.updateLastLoginAt(userId, LocalDateTime.now());
    logger.info("Updated last login time for user ID: {}", userId);
  }

  /** Increment failed login attempts */
  public void incrementFailedLoginAttempts(Integer userId) {
    User user = userRepository.findById(userId).orElse(null);
    if (user != null) {
      int attempts = user.getFailedLoginAttempts() + 1;
      userRepository.updateFailedLoginAttempts(userId, attempts);

      // Lock account after 5 failed attempts for 30 minutes
      if (attempts >= 5) {
        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(30);
        userRepository.lockUserAccount(userId, lockUntil);
        logger.warn("Account locked for user ID: {} until {}", userId, lockUntil);
      }
    }
  }

  /** Reset failed login attempts */
  public void resetFailedLoginAttempts(Integer userId) {
    userRepository.updateFailedLoginAttempts(userId, 0);
    userRepository.unlockUserAccount(userId);
  }

  /** Check if user account is locked */
  public boolean isAccountLocked(User user) {
    return user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now());
  }

  /** Check if user account is active */
  public boolean isAccountActive(User user) {
    return user.getStatus() == UserStatus.ACTIVE && !isAccountLocked(user);
  }

  /** Convert User entity to UserResponse DTO */
  public UserResponse convertToUserResponse(User user, Role role) {
    UserResponse response = new UserResponse();
    response.setId(user.getId());
    response.setUsername(user.getUsername());
    response.setEmail(user.getEmail());
    response.setPhone(user.getPhone());
    response.setRoleId(role.getId());
    response.setRoleName(role.getName());
    response.setStatus(user.getStatus());
    response.setEmailVerified(user.getEmailVerifiedAt() != null);
    response.setPhoneVerified(user.getPhoneVerifiedAt() != null);
    response.setLastLoginAt(user.getLastLoginAt());
    response.setCreatedAt(user.getCreatedAt());
    response.setUpdatedAt(user.getUpdatedAt());

    return response;
  }

  /** Verify user phone */
  public void verifyPhone(Integer userId) {
    userRepository.verifyPhone(userId, LocalDateTime.now());
    logger.info("Phone verified for user ID: {}", userId);

    // If both email and phone are verified, activate the account
    User user = userRepository.findById(userId).orElse(null);
    if (user != null && user.getEmailVerifiedAt() != null && user.getPhoneVerifiedAt() != null) {
      user.setStatus(UserStatus.ACTIVE);
      userRepository.save(user);
    }
  }

  public void sendEmailVerification(User user) {
    emailService.sendEmailVerification(
        user.getEmail(),
        authService.generateEmailVerificationToken(user, emailVerificationExpiredTime));
  }
}
