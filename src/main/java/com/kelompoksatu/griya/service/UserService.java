package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.*;
import com.kelompoksatu.griya.entity.Developer;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  @Transactional
  public Pair<User, Role> registerUser(RegisterRequest request) {
    logger.info("Attempting to register user: {}", request.getUsername());

    validateRegistrationRequest(request);
    Role userRole = getDefaultUserRole();

    try {
      User savedUser = createUserEntity(request, userRole);
      UserProfile savedProfile = createUserProfile(request, savedUser.getId());

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
    UserProfile userProfile = userProfileRepository.findByUserId(userId).orElse(null);
    return convertToUserResponse(user, user.getRole(), userProfile);
  }

  // ========================================
  // USER UPDATE OPERATIONS
  // ========================================

  /** Update both user and profile information */
  @Transactional
  public UserResponse updateUserAndProfile(Integer userId, UpdateUserRequest request) {
    logger.info("Updating user and profile with ID: {}", userId);

    User user = validateAndGetUser(userId);
    request = sanitizeUpdateRequest(request);
    UserProfile profile =
        userProfileRepository
            .findByUserId(userId)
            .orElseGet(() -> new UserProfile(userId)); // if profile doesn’t exist yet

    // 1️⃣ Apply user account updates (if fields are present)
    if (hasUserAccountFields(request)) {
      userMapper.updateUserFromRequest(request, user);
      var requestedStatus = request.getUserStatusEnum();
      if (requestedStatus != null) {
        user.setStatus(requestedStatus);
      }
    }

    // 2️⃣ Apply profile updates (if fields are present)
    if (hasProfileFields(request)) {
      userMapper.updateUserProfileFromRequest(request, profile);
      updateProfileEnumFields(request, profile);
    }

    // 3️⃣ Save only what’s modified
    userRepository.save(user);
    userProfileRepository.save(profile);

    logger.info("User & profile updated successfully with ID: {}", userId);

    // 4️⃣ Return unified response
    return userMapper.toResponse(user, profile);
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
  public UserResponse convertToUserResponse(User user, Role role, UserProfile userProfile) {
    UserResponse response = new UserResponse();

    // Basic user information
    response.setId(user.getId());
    response.setUsername(user.getUsername());
    response.setEmail(user.getEmail());
    response.setPhone(user.getPhone());
    response.setStatus(user.getStatus());
    response.setEmailVerified(user.getEmailVerifiedAt() != null);
    response.setPhoneVerified(user.getPhoneVerifiedAt() != null);
    response.setLastLoginAt(user.getLastLoginAt());
    response.setCreatedAt(user.getCreatedAt());
    response.setUpdatedAt(user.getUpdatedAt());

    // Role information
    if (role != null) {
      response.setRoleId(role.getId());
      response.setRoleName(role.getName());
    }

    boolean isDeveloper = user.getDeveloper() != null;
    response.setDeveloper(isDeveloper);

    if (userProfile != null) {
      response.setFullName(userProfile.getFullName());
      response.setNik(userProfile.getNik());
      response.setNpwp(userProfile.getNpwp());
      response.setBirthDate(userProfile.getBirthDate());
      response.setBirthPlace(userProfile.getBirthPlace());
      response.setGender(userProfile.getGender());
      response.setMaritalStatus(userProfile.getMaritalStatus());
      response.setAddress(userProfile.getAddress());
      response.setCity(userProfile.getCity());
      response.setProvince(userProfile.getProvince());
      response.setPostalCode(userProfile.getPostalCode());
      response.setOccupation(userProfile.getOccupation());
      response.setCompanyName(userProfile.getCompanyName());
      response.setMonthlyIncome(userProfile.getMonthlyIncome());
      response.setWorkExperience(userProfile.getWorkExperience());
    } else if (isDeveloper) {
      Developer developer = user.getDeveloper();
      if (developer != null) {
        response.setFullName(developer.getCompanyName());
        response.setAddress(developer.getAddress());
        response.setCompanyName(developer.getCompanyName());
        response.setOccupation("-");
      }
    }

    return response;
  }

  public PaginatedResponse<UserResponse> getAllUsers(PaginationRequest paginationRequest) {
    Pageable pageable = paginationRequest.toPageable();
    Page<UserResponse> userPage = userRepository.findAllUserResponseByRoleId(2, pageable);
    return PaginatedResponse.of(userPage);
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

  @Transactional
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

  @Transactional
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
    return (request.getUsername() != null && !request.getUsername().trim().isEmpty())
        || (request.getEmail() != null && !request.getEmail().trim().isEmpty())
        || (request.getPhone() != null && !request.getPhone().trim().isEmpty())
        || (request.getStatus() != null && !request.getStatus().trim().isEmpty());
  }

  /** Check if request has profile fields */
  private boolean hasProfileFields(UpdateUserRequest request) {
    return (request.getFullName() != null && !request.getFullName().trim().isEmpty())
        || (request.getNik() != null && !request.getNik().trim().isEmpty())
        || (request.getNpwp() != null && !request.getNpwp().trim().isEmpty())
        || request.getBirthDate() != null
        || (request.getBirthPlace() != null && !request.getBirthPlace().trim().isEmpty())
        || (request.getGender() != null && !request.getGender().trim().isEmpty())
        || (request.getMaritalStatus() != null && !request.getMaritalStatus().trim().isEmpty())
        || (request.getAddress() != null && !request.getAddress().trim().isEmpty())
        || (request.getDistrict() != null && !request.getDistrict().trim().isEmpty())
        || (request.getSubDistrict() != null && !request.getSubDistrict().trim().isEmpty())
        || (request.getCity() != null && !request.getCity().trim().isEmpty())
        || (request.getProvince() != null && !request.getProvince().trim().isEmpty())
        || (request.getPostalCode() != null && !request.getPostalCode().trim().isEmpty())
        || (request.getOccupation() != null && !request.getOccupation().trim().isEmpty())
        || (request.getCompanyName() != null && !request.getCompanyName().trim().isEmpty())
        || (request.getCompanyAddress() != null && !request.getCompanyAddress().trim().isEmpty())
        || (request.getCompanyCity() != null && !request.getCompanyCity().trim().isEmpty())
        || (request.getCompanyProvince() != null && !request.getCompanyProvince().trim().isEmpty())
        || (request.getCompanyPostalCode() != null
            && !request.getCompanyPostalCode().trim().isEmpty())
        || (request.getCompanyDistrict() != null && !request.getCompanyDistrict().trim().isEmpty())
        || (request.getCompanySubdistrict() != null
            && !request.getCompanySubdistrict().trim().isEmpty())
        || request.getMonthlyIncome() != null
        || request.getWorkExperience() != null;
  }

  private UpdateUserRequest sanitizeUpdateRequest(UpdateUserRequest request) {
    if (request == null) return null;
    request.setUsername(normalize(request.getUsername()));
    request.setEmail(normalize(request.getEmail()));
    request.setPhone(normalize(request.getPhone()));
    request.setStatus(normalize(request.getStatus()));
    request.setFullName(normalize(request.getFullName()));
    request.setNik(normalize(request.getNik()));
    request.setNpwp(normalize(request.getNpwp()));
    request.setBirthPlace(normalize(request.getBirthPlace()));
    request.setGender(normalize(request.getGender()));
    request.setMaritalStatus(normalize(request.getMaritalStatus()));
    request.setAddress(normalize(request.getAddress()));
    request.setDistrict(normalize(request.getDistrict()));
    request.setSubDistrict(normalize(request.getSubDistrict()));
    request.setCity(normalize(request.getCity()));
    request.setProvince(normalize(request.getProvince()));
    request.setPostalCode(normalize(request.getPostalCode()));
    request.setOccupation(normalize(request.getOccupation()));
    request.setCompanyName(normalize(request.getCompanyName()));
    request.setCompanyAddress(normalize(request.getCompanyAddress()));
    request.setCompanyCity(normalize(request.getCompanyCity()));
    request.setCompanyProvince(normalize(request.getCompanyProvince()));
    request.setCompanyPostalCode(normalize(request.getCompanyPostalCode()));
    request.setCompanyDistrict(normalize(request.getCompanyDistrict()));
    request.setCompanySubdistrict(normalize(request.getCompanySubdistrict()));
    return request;
  }

  private String normalize(String value) {
    if (value == null) return null;
    String t = value.trim();
    return t.isEmpty() ? null : t;
  }
}
