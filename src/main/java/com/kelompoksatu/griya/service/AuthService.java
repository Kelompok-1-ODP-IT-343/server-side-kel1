package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.*;
import com.kelompoksatu.griya.entity.*;
import com.kelompoksatu.griya.repository.RoleRepository;
import com.kelompoksatu.griya.repository.UserRepository;
import com.kelompoksatu.griya.repository.UserSessionRepository;
import com.kelompoksatu.griya.repository.VerificationTokenRepository;
import com.kelompoksatu.griya.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

/**
 * Service class for authentication operations
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;

    private final RoleRepository roleRepository;

    private final UserSessionRepository userSessionRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final UserRepository userRepo;

    private final VerificationTokenRepository tokenRepo;

    /**
     * Register a new user
     */
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

    /**
     * Authenticate user login
     */
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        try {
            logger.info("Processing login for identifier: {}", request.getIdentifier());

            // Find user by username or email
            User user = userService.findByUsernameOrEmail(request.getIdentifier())
                    .orElseThrow(() -> new RuntimeException("Username atau email tidak ditemukan"));

            // Check if account is locked
            if (userService.isAccountLocked(user)) {
                throw new RuntimeException("Akun terkunci karena terlalu banyak percobaan login yang gagal");
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

            // Get user role
            Role role = roleRepository.findById(user.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role tidak ditemukan"));

            // Generate JWT token
            String token = jwtUtil.generateTokenWithUserInfo(
                    user.getUsername(),
                    user.getId(),
                    role.getName()
            );

            // Create user session
            createUserSession(user.getId(), ipAddress, userAgent, token);

            logger.info("User logged in successfully: {}", user.getUsername());
            return new AuthResponse(token);

        } catch (Exception e) {
            logger.error("Login failed for identifier {}: {}", request.getIdentifier(), e.getMessage());
            throw new RuntimeException("Login gagal: " + e.getMessage());
        }
    }

    /**
     * Get user profile by token
     */
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

    /**
     * Logout user and invalidate session
     */
    public void logout(String token) {
        try {
            Integer userId = jwtUtil.extractUserId(token);

            // Delete user sessions (optional: you might want to keep sessions for audit)
            // userSessionRepository.deleteByUserId(userId);

            logger.info("User logged out successfully, userId: {}", userId);

        } catch (Exception e) {
            logger.error("Logout failed: {}", e.getMessage());
            throw new RuntimeException("Logout gagal: " + e.getMessage());
        }
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Create user session record
     */
    private void createUserSession(Integer userId, String ipAddress, String userAgent, String token) {
        try {
            UserSession session = new UserSession();
            session.setId(UUID.randomUUID().toString());
            session.setUserId(userId);
            session.setIpAddress(ipAddress);
            session.setUserAgent(userAgent);

            // Create session payload with basic info
            Map<String, Object> payload = new HashMap<>();
            payload.put("loginTime", LocalDateTime.now().toString());
            payload.put("tokenHash", token.substring(0, Math.min(20, token.length()))); // Store partial token for reference
            session.setPayload(payload.toString());

            session.setLastActivity(LocalDateTime.now());
            session.setCreatedAt(LocalDateTime.now());

            userSessionRepository.save(session);
            logger.info("User session created for userId: {}", userId);

        } catch (Exception e) {
            logger.error("Failed to create user session: {}", e.getMessage());
            // Don't throw exception here as it's not critical for login process
        }
    }

    /**
     * Clean up expired sessions
     */
    public void cleanupExpiredSessions() {
        try {
            LocalDateTime expiredBefore = LocalDateTime.now().minusDays(30); // Sessions older than 30 days
            userSessionRepository.deleteExpiredSessions(expiredBefore);
            logger.info("Expired sessions cleaned up");
        } catch (Exception e) {
            logger.error("Failed to cleanup expired sessions: {}", e.getMessage());
        }
    }

    @SneakyThrows
    @Transactional
    public String generateEmailVerificationToken(Integer userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = UUID.randomUUID().toString().replace("-", ""); // 32 char
        VerificationToken vt = new VerificationToken();
        vt.setToken(hashToken(token));
        vt.setUser(user);
        vt.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));

        tokenRepo.save(vt);
        return token;
    }

    /**
     * Verifikasi token dari link email
     */
    @SneakyThrows
    @Transactional
    public VerifyEmailResponse verifyEmail(String token) {
        String hashedToken = hashToken(token);
        VerificationToken vt = tokenRepo.findByToken(hashedToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (vt.isUsed()) {
            return new VerifyEmailResponse(true, "Email already verified", vt.getUser().getEmail());
        }
        if (vt.isExpired()) {
            throw new IllegalStateException("Token expired");
        }

        User user = vt.getUser();
        if (!user.isEmailVerified()) {
            user.setEmailVerifiedAt(LocalDateTime.now());
            user.setStatus(UserStatus.ACTIVE);
            userRepo.save(user);
        }

        vt.setUsedAt(Instant.now());
        tokenRepo.save(vt);

        return new VerifyEmailResponse(true, "Email verified successfully", user.getEmail());
    }

    private static String hashToken(String token) throws NoSuchAlgorithmException {
        return HexFormat.of()
                .formatHex(MessageDigest.getInstance("SHA-256")
                        .digest(token.getBytes(StandardCharsets.UTF_8)));
    }
}
