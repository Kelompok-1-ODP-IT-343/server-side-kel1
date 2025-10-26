package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.RegisterRequest;
import com.kelompoksatu.griya.dto.UpdateUserRequest;
import com.kelompoksatu.griya.dto.UserResponse;
import com.kelompoksatu.griya.entity.Role;
import com.kelompoksatu.griya.entity.User;
import com.kelompoksatu.griya.entity.UserProfile;
import com.kelompoksatu.griya.entity.UserStatus;
import com.kelompoksatu.griya.mapper.UserMapper;
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
  @Autowired private UserMapper userMapper;

  // ========================================
  // USER REGISTRATION AND CREATION
  // ========================================

  /** Register a new user with comprehensive profile information */
  public Pair<User, Role> registerUser(RegisterRequest request) {
    logger.info("Attempting to register user: {}", request.getUsername());

    validateRegistrationRequest(request);
    Role userRole = getDefaultUserRole();

    try {
      User savedUser = createUserEntity(request, userRole);
      UserProfile savedProfile = createUserProfile(request, savedUser.getId());
      sendEmailVerification(savedUser);

      logger.info("User profile created successfully for user ID: {}", savedUser.getId());
      return Pair.of(savedUser, userRole);
    } catch (DataIntegrityViolationException ex) {
      throw ex;
    }
  }

  // ========================================
  // USER QUERY METHODS
  // ========================================

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
    User user = validateAndGetUser(userId);
    return convertToUserResponse(user, user.getRole());
  }

  // ========================================
  // USER UPDATE OPERATIONS
  // ========================================

  /** Update user information */
  public UserResponse updateUser(Integer userId, UpdateUserRequest request) {
    logger.info("Attempting to update user with ID: {}", userId);

    User existingUser = validateAndGetUser(userId);
    validateUserUpdateRequest(request, existingUser);

    // Update user fields using MapStruct
    userMapper.updateUserFromRequest(request, existingUser);

    User updatedUser = userRepository.save(existingUser);
    logger.info("User updated successfully with ID: {}", userId);

    return userMapper.toResponse(updatedUser);
  }

  /** Update user profile information */
  public UserResponse updateUserProfile(Integer userId, UpdateUserRequest request) {
    logger.info("Attempting to update user profile with ID: {}", userId);

    UserProfile existingProfile = validateAndGetUserProfile(userId);
    validateProfileUpdateRequest(request, existingProfile);

    // Update profile fields using MapStruct
    userMapper.updateUserProfileFromRequest(request, existingProfile);
    updateProfileEnumFields(request, existingProfile);

    UserProfile updatedProfile = userProfileRepository.save(existingProfile);
    logger.info("User profile updated successfully for user ID: {}", userId);

    User user = validateAndGetUser(userId);
    return userMapper.toResponse(user);
  }

  /** Update both user and profile information */
  public UserResponse updateUserComplete(Integer userId, UpdateUserRequest request) {
    logger.info("Attempting to update user and profile with ID: {}", userId);

    UserResponse result = null;

    // Update user information if any user account fields are provided
    if (hasUserAccountFields(request)) {
      result = updateUser(userId, request);
    }

    // Update profile information if any profile fields are provided
    if (hasProfileFields(request)) {
      result = updateUserProfile(userId, request);
    }

    // If no fields are provided, return current user info
    if (result == null) {
      User user = validateAndGetUser(userId);
      result = userMapper.toResponse(user);
    }

    logger.info("User and profile updated successfully with ID: {}", userId);
    return result;
  }

  // ========================================
  // AUTHENTICATION AND SECURITY OPERATIONS
  // ========================================

  /** Update user last login time */
  public void updateLastLogin(Integer userId) {
    userRepository.updateLastLoginAt(userId, LocalDateTime.now());
    logger.info("Updated last login time for user ID: {}", userId);
  }

  /** Increment failed login attempts */
  public void incrementFailedLoginAttempts(User user) {
    if (user != null) {
      int attempts = user.getFailedLoginAttempts() + 1;
      userRepository.updateFailedLoginAttempts(user.getId(), attempts);

      // Lock account after 5 failed attempts for 30 minutes
      if (attempts >= 5) {
        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(30);
        userRepository.lockUserAccount(user.getId(), lockUntil);
        logger.warn("Account locked for user ID: {} until {}", user.getId(), lockUntil);
      }
    }
  }

  /** 1. Reset failed login attempts 2. Reset last login time 3. Unlock account */
  public void resetLogin(Integer userId) {
    userRepository.unlockResetAndSetLastLogin(userId);
  }

  /** Check if user account is locked */
  public boolean isAccountLocked(User user) {
    return user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now());
  }

  /** Check if user account is active */
  public boolean isAccountActive(User user) {
    return user.getStatus() == UserStatus.ACTIVE && !isAccountLocked(user);
  }

  // ========================================
  // VERIFICATION OPERATIONS
  // ========================================

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

  // ========================================
  // UTILITY AND CONVERSION METHODS
  // ========================================

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

  // ========================================
  // PRIVATE VALIDATION METHODS
  // ========================================

  private void validateRegistrationRequest(RegisterRequest request) {
    if (!request.isPasswordMatching()) {
      throw new ValidationException("Password dan konfirmasi password tidak cocok");
    }
  }

  private User validateAndGetUser(Integer userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
  }

  private UserProfile validateAndGetUserProfile(Integer userId) {
    return userProfileRepository
        .findByUserId(userId)
        .orElseThrow(() -> new RuntimeException("User profile not found for user ID: " + userId));
  }

  private void validateUserUpdateRequest(UpdateUserRequest request, User existingUser) {
    // Check for unique constraints if updating username, email, or phone
    if (request.getUsername() != null
        && !request.getUsername().equals(existingUser.getUsername())) {
      if (userRepository.existsByUsername(request.getUsername())) {
        throw new ValidationException("Username already exists: " + request.getUsername());
      }
    }

    if (request.getEmail() != null && !request.getEmail().equals(existingUser.getEmail())) {
      if (userRepository.existsByEmail(request.getEmail())) {
        throw new ValidationException("Email already exists: " + request.getEmail());
      }
    }

    if (request.getPhone() != null && !request.getPhone().equals(existingUser.getPhone())) {
      if (userRepository.existsByPhone(request.getPhone())) {
        throw new ValidationException("Phone number already exists: " + request.getPhone());
      }
    }
  }

  private void validateProfileUpdateRequest(
      UpdateUserRequest request, UserProfile existingProfile) {
    // Check for unique constraints if updating NIK or NPWP
    if (request.getNik() != null && !request.getNik().equals(existingProfile.getNik())) {
      if (userProfileRepository.existsByNik(request.getNik())) {
        throw new ValidationException("NIK already exists: " + request.getNik());
      }
    }

    if (request.getNpwp() != null && !request.getNpwp().equals(existingProfile.getNpwp())) {
      if (userProfileRepository.existsByNpwp(request.getNpwp())) {
        throw new ValidationException("NPWP already exists: " + request.getNpwp());
      }
    }
  }

  // ========================================
  // PRIVATE BUSINESS LOGIC METHODS
  // ========================================

  private Role getDefaultUserRole() {
    return roleRepository
        .findByName("USER")
        .orElseThrow(() -> new RuntimeException("Default role 'USER' not found"));
  }

  private User createUserEntity(RegisterRequest request, Role userRole) {
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

    User savedUser = userRepository.save(user);
    logger.info("User registered successfully with ID: {}", savedUser.getId());
    return savedUser;
  }

  private UserProfile createUserProfile(RegisterRequest request, Integer userId) {
    UserProfile userProfile = new UserProfile();
    userProfile.setUserId(userId);
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

    return userProfileRepository.save(userProfile);
  }

  private void updateProfileEnumFields(UpdateUserRequest request, UserProfile existingProfile) {
    if (request.getMaritalStatusEnum() != null) {
      existingProfile.setMaritalStatus(request.getMaritalStatusEnum());
    }
    if (request.getGenderEnum() != null) {
      existingProfile.setGender(request.getGenderEnum());
    }
  }

  // ========================================
  // PRIVATE UTILITY METHODS
  // ========================================

  /** Check if request has user account fields */
  private boolean hasUserAccountFields(UpdateUserRequest request) {
    return request.getUsername() != null
        || request.getEmail() != null
        || request.getPhone() != null
        || request.getStatus() != null;
  }

  /** Check if request has profile fields */
  private boolean hasProfileFields(UpdateUserRequest request) {
    return request.getFullName() != null
        || request.getNik() != null
        || request.getNpwp() != null
        || request.getBirthDate() != null
        || request.getBirthPlace() != null
        || request.getGender() != null
        || request.getMaritalStatus() != null
        || request.getAddress() != null
        || request.getCity() != null
        || request.getProvince() != null
        || request.getPostalCode() != null
        || request.getOccupation() != null
        || request.getCompanyName() != null
        || request.getMonthlyIncome() != null
        || request.getWorkExperience() != null;
  }
}
