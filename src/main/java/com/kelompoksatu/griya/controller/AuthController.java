package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.*;
import com.kelompoksatu.griya.service.AuthService;
import com.kelompoksatu.griya.service.OtpService;
import com.kelompoksatu.griya.service.RedisService;
import com.kelompoksatu.griya.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for authentication operations API Version: v1 */
@Tag(name = "Authentication", description = "Authentication and user management operations")
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AuthService authService;
  private final JwtUtil jwtUtil;
  private final OtpService otpService;
  private final RedisService redisService;

  /** Register a new user POST /api/v1/auth/register - Send OTP for verification */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<OtpResponse>> register(
      @Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {

    log.info("Registration attempt for username: {}", request.getUsername());

    try {
      OtpResponse otpResponse = authService.register(request);

      if (otpResponse.isSuccess()) {
        ApiResponse<OtpResponse> response =
            ApiResponse.success(
                otpResponse,
                "Registrasi berhasil, OTP telah dikirim ke WhatsApp Anda",
                httpRequest.getRequestURI());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
      } else {
        // Jika OtpResponse.success = false, kembalikan error dengan detail
        ApiResponse<OtpResponse> response =
            ApiResponse.error(
                otpResponse.getMessage() != null ? otpResponse.getMessage() : "Registrasi gagal",
                httpRequest.getRequestURI());
        response.setData(otpResponse); // Include the error details in data

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }

    } catch (IllegalArgumentException e) {
      log.error("Validation error during registration: {}", e.getMessage());

      OtpResponse otpResponse =
          OtpResponse.error(
              OtpErrorType.DUPLICATE_USER, "Registration validation failed: " + e.getMessage());

      ApiResponse<OtpResponse> response =
          ApiResponse.error(e.getMessage(), httpRequest.getRequestURI());
      response.setData(otpResponse);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

    } catch (RuntimeException e) {
      log.error("Runtime error during registration: {}", e.getMessage());

      OtpResponse otpResponse;
      if (e.getMessage().contains("phone")) {
        otpResponse =
            OtpResponse.error(
                OtpErrorType.INVALID_PHONE_NUMBER,
                "Phone number validation failed: " + e.getMessage());
      } else if (e.getMessage().contains("OTP") || e.getMessage().contains("WhatsApp")) {
        otpResponse =
            OtpResponse.error(
                OtpErrorType.OTP_SERVICE_ERROR, "OTP service error: " + e.getMessage());
      } else {
        otpResponse =
            OtpResponse.error(
                OtpErrorType.SYSTEM_ERROR, "System error during registration: " + e.getMessage());
      }

      ApiResponse<OtpResponse> response =
          ApiResponse.error(
              "Registrasi gagal: " + otpResponse.getErrorDetail().getMessage(),
              httpRequest.getRequestURI());
      response.setData(otpResponse);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

    } catch (Exception e) {
      log.error("Unexpected error during registration: {}", e.getMessage(), e);

      OtpResponse otpResponse =
          OtpResponse.error(
              OtpErrorType.SYSTEM_ERROR, "Unexpected error during registration: " + e.getMessage());

      ApiResponse<OtpResponse> response =
          ApiResponse.error(
              "Terjadi kesalahan sistem. Silakan coba lagi.", httpRequest.getRequestURI());
      response.setData(otpResponse);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Verify OTP for login and get authentication tokens POST /api/v1/auth/verify-login-otp */
  @PostMapping("/verify-login-otp")
  public ResponseEntity<ApiResponse<AuthResponse>> verifyLoginOtp(
      @Valid @RequestBody VerifyOtpRequest request, HttpServletRequest httpRequest) {

    log.info(
        "Login OTP verification for identifier: {} with purpose: {}",
        request.getIdentifier(),
        request.getPurpose());

    try {
      // Get client IP address
      String ipAddress = getClientIpAddress(httpRequest);
      String userAgent = httpRequest.getHeader("User-Agent");

      AuthResponse authResponse =
          authService.verifyLoginOtp(
              request.getIdentifier(),
              request.getOtp(),
              request.getPurpose(),
              ipAddress,
              userAgent);

      ApiResponse<AuthResponse> response =
          ApiResponse.success(authResponse, "Login berhasil", httpRequest.getRequestURI());

      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      log.error("Invalid login OTP verification request: {}", e.getMessage());

      ApiResponse<AuthResponse> response =
          ApiResponse.error(e.getMessage(), httpRequest.getRequestURI());

      return ResponseEntity.badRequest().body(response);

    } catch (Exception e) {
      log.error("Error verifying login OTP: {}", e.getMessage(), e);

      ApiResponse<AuthResponse> response =
          ApiResponse.error(
              "Gagal memverifikasi OTP login. Silakan coba lagi.", httpRequest.getRequestURI());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Verify OTP for registration and activate account POST /api/v1/auth/verify-registration-otp */
  @PostMapping("/verify-registration-otp")
  public ResponseEntity<ApiResponse<?>> verifyRegistrationOtp(
      @Valid @RequestBody VerifyOtpRequest request, HttpServletRequest httpRequest) {

    log.info(
        "Registration OTP verification for identifier: {} with purpose: {}",
        request.getIdentifier(),
        request.getPurpose());

    try {
      RegisterResponse registerResponse =
          authService.verifyRegistrationOtp(
              request.getIdentifier(), request.getOtp(), request.getPurpose());

      ApiResponse<RegisterResponse> response =
          ApiResponse.success(
              registerResponse, "Registrasi berhasil diverifikasi", httpRequest.getRequestURI());

      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      log.error("Invalid registration OTP verification request: {}", e.getMessage());

      ApiResponse<RegisterResponse> response =
          ApiResponse.error(e.getMessage(), httpRequest.getRequestURI());

      return ResponseEntity.badRequest().body(response);

    } catch (Exception e) {
      log.error("Error verifying registration OTP: {}", e.getMessage(), e);

      ApiResponse<RegisterResponse> response =
          ApiResponse.error(
              "Gagal memverifikasi OTP registrasi. Silakan coba lagi.",
              httpRequest.getRequestURI());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Register a new developer (admin only) POST /api/v1/auth/register/developer */
  @Operation(
      summary = "Register a new developer",
      description =
          "Register a new developer with both user account and developer profile information. This endpoint is intended for admin use only.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Developer registered successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad request - validation errors or duplicate data",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class)))
      })
  @PostMapping("/register/developer")
  public ResponseEntity<ApiResponse<RegisterDeveloperResponse>> registerDeveloper(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description =
                  "Developer registration request containing user account and developer profile information",
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = RegisterDeveloperRequest.class)))
          @Valid
          @RequestBody
          RegisterDeveloperRequest request,
      HttpServletRequest httpRequest) {

    log.info("Developer registration attempt for username: {}", request.getUsername());

    try {
      RegisterDeveloperResponse developerResponse = authService.registerDeveloper(request);

      ApiResponse<RegisterDeveloperResponse> response =
          ApiResponse.success(
              developerResponse, "Developer registered successfully", httpRequest.getRequestURI());

      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (IllegalArgumentException e) {
      log.error("Validation error during developer registration: {}", e.getMessage());
      ApiResponse<RegisterDeveloperResponse> response =
          ApiResponse.error(e.getMessage(), httpRequest.getRequestURI());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (Exception e) {
      log.error("Failed to register developer: {}", e.getMessage());
      ApiResponse<RegisterDeveloperResponse> response =
          ApiResponse.error(
              "Failed to register developer: " + e.getMessage(), httpRequest.getRequestURI());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** User login POST /api/v1/auth/login - Send OTP instead of immediate token */
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<OtpResponse>> login(
      @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {

    try {
      log.info("Login attempt for identifier: {}", request.getIdentifier());

      // Get client IP address
      String ipAddress = getClientIpAddress(httpRequest);
      String userAgent = httpRequest.getHeader("User-Agent");

      OtpResponse otpResponse = authService.login(request, ipAddress, userAgent);

      if (otpResponse.isSuccess()) {
        ApiResponse<OtpResponse> response =
            ApiResponse.success(
                otpResponse, "OTP telah dikirim ke WhatsApp Anda", httpRequest.getRequestURI());

        return ResponseEntity.ok(response);
      } else {
        // Jika OtpResponse.success = false, kembalikan error dengan detail
        ApiResponse<OtpResponse> response =
            ApiResponse.error(
                otpResponse.getMessage() != null ? otpResponse.getMessage() : "Login gagal",
                httpRequest.getRequestURI());
        response.setData(otpResponse); // Include the error details in data

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }

    } catch (IllegalArgumentException e) {
      log.error("Invalid login request: {}", e.getMessage());

      OtpResponse otpResponse =
          OtpResponse.error(
              OtpErrorType.USER_NOT_FOUND, "Login validation failed: " + e.getMessage());

      ApiResponse<OtpResponse> response =
          ApiResponse.error(e.getMessage(), httpRequest.getRequestURI());
      response.setData(otpResponse);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

    } catch (RuntimeException e) {
      log.error("Runtime error during login: {}", e.getMessage());

      OtpResponse otpResponse;
      if (e.getMessage().contains("locked")) {
        otpResponse =
            OtpResponse.error(
                OtpErrorType.ACCOUNT_LOCKED, "Account security issue: " + e.getMessage());
      } else if (e.getMessage().contains("rate limit") || e.getMessage().contains("too many")) {
        otpResponse =
            OtpResponse.error(
                OtpErrorType.RATE_LIMIT_EXCEEDED, "Rate limit exceeded: " + e.getMessage());
      } else if (e.getMessage().contains("phone")) {
        otpResponse =
            OtpResponse.error(OtpErrorType.INVALID_PHONE_NUMBER, "Phone issue: " + e.getMessage());
      } else if (e.getMessage().contains("OTP") || e.getMessage().contains("WhatsApp")) {
        otpResponse =
            OtpResponse.error(
                OtpErrorType.OTP_SERVICE_ERROR, "OTP service error: " + e.getMessage());
      } else {
        otpResponse =
            OtpResponse.error(
                OtpErrorType.SYSTEM_ERROR, "System error during login: " + e.getMessage());
      }

      ApiResponse<OtpResponse> response =
          ApiResponse.error(
              "Login gagal: " + otpResponse.getErrorDetail().getMessage(),
              httpRequest.getRequestURI());
      response.setData(otpResponse);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

    } catch (Exception e) {
      log.error("Unexpected error during login: {}", e.getMessage(), e);

      OtpResponse otpResponse =
          OtpResponse.error(
              OtpErrorType.SYSTEM_ERROR, "Unexpected error during login: " + e.getMessage());

      ApiResponse<OtpResponse> response =
          ApiResponse.error(
              "Terjadi kesalahan sistem. Silakan coba lagi.", httpRequest.getRequestURI());
      response.setData(otpResponse);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refreshToken(
      @RequestBody TokenRefreshRequest request, HttpServletRequest httpRequest) {
    // Get client IP address
    String ipAddress = getClientIpAddress(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");
    request.setIpAddress(ipAddress);
    request.setUserAgent(userAgent);
    AuthResponse response = authService.refreshToken(request);

    return ResponseEntity.ok(response);
  }

  /** User logout POST /api/v1/auth/logout */
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<String>> logout(
      @RequestHeader("Authorization") String authHeader, HttpServletRequest httpRequest) {

    try {
      // Extract token from Authorization header
      String token = jwtUtil.extractTokenFromHeader(authHeader);

      authService.logout(token);

      ApiResponse<String> response =
          ApiResponse.success(null, "Logout berhasil", httpRequest.getRequestURI());

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Logout failed: {}", e.getMessage());

      ApiResponse<String> response = ApiResponse.error(e.getMessage(), httpRequest.getRequestURI());

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
  }

  /** Validate JWT token POST /api/v1/auth/validate */
  @PostMapping("/validate")
  public ResponseEntity<ApiResponse<Boolean>> validateToken(
      @RequestHeader("Authorization") String authHeader, HttpServletRequest httpRequest) {

    try {
      String token = jwtUtil.extractTokenFromHeader(authHeader);
      boolean isValid = authService.validateToken(token);

      ApiResponse<Boolean> response =
          ApiResponse.success(
              isValid, isValid ? "Token valid" : "Token tidak valid", httpRequest.getRequestURI());

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Token validation failed: {}", e.getMessage());

      ApiResponse<Boolean> response =
          ApiResponse.success(false, "Token tidak valid", httpRequest.getRequestURI());

      return ResponseEntity.ok(response);
    }
  }

  @GetMapping("/verify")
  public ResponseEntity<VerifyEmailResponse> verifyEmail(@RequestParam("token") String token) {
    try {
      VerifyEmailResponse res = authService.verifyEmail(token);
      return ResponseEntity.ok(res);
    } catch (IllegalArgumentException e) { // invalid
      return ResponseEntity.badRequest().body(new VerifyEmailResponse(false, e.getMessage(), null));
    } catch (IllegalStateException e) { // expired
      return ResponseEntity.status(410).body(new VerifyEmailResponse(false, e.getMessage(), null));
    }
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<ApiResponse<Boolean>> forgotPassword(
      @Valid @RequestBody() ForgotPasswordRequest request) {
    authService.forgotPassword(request);
    return ResponseEntity.ok(
        new ApiResponse<>(true, "Email lupa password berhasil terkirim", null));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request.getToken(), request.getNewPassword());
    return ResponseEntity.ok("Password has been reset successfully.");
  }

  @PostMapping("/forgot-password/phone/send-otp")
  public ResponseEntity<ApiResponse<OtpResponse>> sendOtpForgotPassword(
      @RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
    String phone = request.get("phone");
    if (phone == null || phone.trim().isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Nomor telepon wajib diisi", httpRequest.getRequestURI()));
    }
    if (!authService.isPhoneRegistered(phone)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Nomor telepon tidak terdaftar", httpRequest.getRequestURI()));
    }
    OtpResponse otp = otpService.sendOtp(phone, "reset_password");
    if (otp.isSuccess()) {
      return ResponseEntity.ok(
          ApiResponse.success(otp, "OTP terkirim", httpRequest.getRequestURI()));
    }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ApiResponse.error(
                otp.getErrorDetail() != null ? otp.getErrorDetail().getMessage() : otp.getMessage(),
                httpRequest.getRequestURI()));
  }

  @PostMapping("/forgot-password/phone/verify-otp")
  public ResponseEntity<ApiResponse<Map<String, Object>>> verifyOtpForgotPassword(
      @RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
    String phone = request.get("phone");
    String otp = request.get("otp");
    if (phone == null || otp == null || phone.trim().isEmpty() || otp.trim().isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Phone dan OTP wajib diisi", httpRequest.getRequestURI()));
    }
    if (!authService.isPhoneRegistered(phone)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Nomor telepon tidak terdaftar", httpRequest.getRequestURI()));
    }
    boolean ok = otpService.verifyOtp(phone, otp, "reset_password");
    if (!ok) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("OTP tidak valid atau kedaluwarsa", httpRequest.getRequestURI()));
    }
    String resetToken = java.util.UUID.randomUUID().toString().replace("-", "");
    String normalized = otpService.normalizePhoneNumber(phone);
    redisService.set("pwd_reset:" + resetToken, normalized, 600);
    Map<String, Object> data = new java.util.HashMap<>();
    data.put("resetToken", resetToken);
    data.put("expiresIn", 600);
    return ResponseEntity.ok(ApiResponse.success(data, "OTP valid", httpRequest.getRequestURI()));
  }

  @PostMapping("/forgot-password/phone/set-password")
  public ResponseEntity<ApiResponse<Boolean>> setNewPasswordPhone(
      @RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
    String token = request.get("resetToken");
    String newPassword = request.get("newPassword");
    if (token == null
        || newPassword == null
        || token.trim().isEmpty()
        || newPassword.trim().isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.error(
                  "resetToken dan newPassword wajib diisi", httpRequest.getRequestURI()));
    }
    String phone = redisService.get("pwd_reset:" + token, String.class);
    if (phone == null || phone.isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.error(
                  "Reset token tidak valid atau kedaluwarsa", httpRequest.getRequestURI()));
    }
    try {
      authService.setNewPasswordByPhone(phone, newPassword);
      redisService.delete("pwd_reset:" + token);
      return ResponseEntity.ok(
          ApiResponse.success(true, "Password berhasil direset", httpRequest.getRequestURI()));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(e.getMessage(), httpRequest.getRequestURI()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.error(
                  "Gagal mengubah password: " + e.getMessage(), httpRequest.getRequestURI()));
    }
  }

  /** Verify OTP code POST /api/v1/auth/verify-otp */
  @Operation(
      summary = "Verify OTP code",
      description =
          "Verify the OTP code sent to the phone number. Returns authentication tokens for login/registration purposes, or simple confirmation for other purposes.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description =
                "OTP verified successfully - returns tokens for login/registration or confirmation for other purposes",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad request - invalid or expired OTP",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "429",
            description = "Too many attempts - account temporarily locked",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class)))
      })
  @PostMapping("/verify-otp")
  public ResponseEntity<ApiResponse<Object>> verifyOtp(
      @Valid @RequestBody VerifyOtpRequest request, HttpServletRequest httpRequest) {

    log.info(
        "OTP verification for identifier: {} with purpose: {}",
        request.getIdentifier(),
        request.getPurpose());

    try {
      // Berdasarkan purpose, panggil method yang sesuai tanpa double validation
      String purpose = request.getPurpose() != null ? request.getPurpose().toLowerCase() : "login";

      switch (purpose) {
        case "login":
          // Get client IP address and user agent for login
          String ipAddress = getClientIpAddress(httpRequest);
          String userAgent = httpRequest.getHeader("User-Agent");

          AuthResponse authResponse =
              authService.verifyLoginOtp(
                  request.getIdentifier(),
                  request.getOtp(),
                  request.getPurpose(),
                  ipAddress,
                  userAgent);

          ApiResponse<Object> loginResponse =
              ApiResponse.success(authResponse, "Login berhasil", httpRequest.getRequestURI());

          return ResponseEntity.ok(loginResponse);

        case "registration":
          RegisterResponse registerResponse =
              authService.verifyRegistrationOtp(
                  request.getIdentifier(), request.getOtp(), request.getPurpose());

          ApiResponse<Object> regResponse =
              ApiResponse.success(
                  registerResponse,
                  "Registrasi berhasil diverifikasi",
                  httpRequest.getRequestURI());

          return ResponseEntity.ok(regResponse);

        default:
          // Untuk purpose lainnya (reset_password, transaction, dll), validate OTP saja
          // Note: For non-login/registration purposes, we still need phone number for OTP
          // validation
          // This would require additional logic to find user by identifier and get phone
          throw new IllegalArgumentException(
              "Purpose '"
                  + purpose
                  + "' tidak didukung dengan identifier. Gunakan phone number untuk purpose ini.");
      }

    } catch (IllegalArgumentException e) {
      log.error("Invalid OTP verification request: {}", e.getMessage());

      ApiResponse<Object> response = ApiResponse.error(e.getMessage(), httpRequest.getRequestURI());

      return ResponseEntity.badRequest().body(response);

    } catch (Exception e) {
      log.error("Error verifying OTP: {}", e.getMessage(), e);

      ApiResponse<Object> response =
          ApiResponse.error(
              "Gagal memverifikasi OTP. Silakan coba lagi.", httpRequest.getRequestURI());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @Operation(
      summary = "Debug OTP - Development Only",
      description =
          "Debug endpoint to diagnose OTP storage and validation issues. Only for development use.")
  @PostMapping("/debug-otp")
  public ResponseEntity<ApiResponse<Map<String, Object>>> debugOtp(
      @RequestBody Map<String, String> request, HttpServletRequest httpRequest) {

    try {
      String phone = request.get("phone");
      String otp = request.get("otp");
      String purpose = request.get("purpose");

      if (phone == null || purpose == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Phone and purpose are required", httpRequest.getRequestURI()));
      }

      Map<String, Object> debugInfo = new HashMap<>();

      // 1. Phone normalization
      String originalPhone = phone;
      String normalizedPhone = otpService.normalizePhoneNumber(phone);
      debugInfo.put("originalPhone", originalPhone);
      debugInfo.put("normalizedPhone", normalizedPhone);

      // 2. OTP identifier
      String identifier = normalizedPhone + ":" + purpose;
      debugInfo.put("otpIdentifier", identifier);
      debugInfo.put("redisKey", "otp:" + identifier);

      // 3. Check if OTP exists in Redis
      boolean otpExists = otpService.hasValidOtp(normalizedPhone, purpose);
      debugInfo.put("otpExists", otpExists);

      // 4. Get OTP TTL
      long ttl = otpService.getOtpTtl(normalizedPhone, purpose);
      debugInfo.put("otpTtlSeconds", ttl);

      // 5. Input OTP info
      if (otp != null) {
        debugInfo.put("inputOtp", otp);
        debugInfo.put("inputOtpLength", otp.length());
        debugInfo.put("inputOtpTrimmed", otp.trim());

        // 6. Try to validate OTP
        try {
          boolean isValid = otpService.verifyOtp(normalizedPhone, otp, purpose);
          debugInfo.put("otpValidationResult", isValid);
        } catch (Exception e) {
          debugInfo.put("otpValidationError", e.getMessage());
        }
      }

      // 7. Redis connection test
      try {
        boolean redisWorking = redisService.testConnection();
        debugInfo.put("redisConnectionTest", redisWorking ? "WORKING" : "FAILED");
      } catch (Exception e) {
        debugInfo.put("redisConnectionError", e.getMessage());
      }

      ApiResponse<Map<String, Object>> response =
          ApiResponse.success(
              debugInfo, httpRequest.getRequestURI(), "Debug information retrieved");

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error in debug OTP endpoint: {}", e.getMessage(), e);

      ApiResponse<Map<String, Object>> response =
          ApiResponse.error("Debug endpoint error: " + e.getMessage(), httpRequest.getRequestURI());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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

  /** Extract client IP address from request */
  private String getClientIpAddress(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }

    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }

    return request.getRemoteAddr();
  }

  /** Handle OtpResponse and return appropriate ResponseEntity */
  private ResponseEntity<ApiResponse<OtpResponse>> handleOtpResponse(
      OtpResponse otpResponse, String successMessage, String requestUri, HttpStatus successStatus) {

    if (otpResponse.isSuccess()) {
      ApiResponse<OtpResponse> response =
          ApiResponse.success(otpResponse, successMessage, requestUri);
      return ResponseEntity.status(successStatus).body(response);
    } else {
      ApiResponse<OtpResponse> response =
          ApiResponse.error(
              otpResponse.getMessage() != null ? otpResponse.getMessage() : "Operasi gagal",
              requestUri);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
  }
}
