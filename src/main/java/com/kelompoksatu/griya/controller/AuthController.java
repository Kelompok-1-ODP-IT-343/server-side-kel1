package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.*;
import com.kelompoksatu.griya.service.AuthService;
import com.kelompoksatu.griya.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for authentication operations API Version: v1 */
@Tag(name = "Authentication", description = "Authentication and user management operations")
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  private final AuthService authService;
  private final JwtUtil jwtUtil;

  /** Register a new user POST /api/v1/auth/register */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<RegisterResponse>> register(
      @Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {

    logger.info("Registration attempt for username: {}", request.getUsername());

    RegisterResponse authResponse = authService.register(request);

    ApiResponse<RegisterResponse> response =
        ApiResponse.success(authResponse, "Registrasi berhasil", httpRequest.getRequestURI());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
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

    logger.info("Developer registration attempt for username: {}", request.getUsername());

    try {
      RegisterDeveloperResponse developerResponse = authService.registerDeveloper(request);

      ApiResponse<RegisterDeveloperResponse> response =
          ApiResponse.success(
              developerResponse, "Developer registered successfully", httpRequest.getRequestURI());

      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (IllegalArgumentException e) {
      logger.error("Validation error during developer registration: {}", e.getMessage());
      ApiResponse<RegisterDeveloperResponse> response =
          ApiResponse.error(e.getMessage(), httpRequest.getRequestURI());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (Exception e) {
      logger.error("Failed to register developer: {}", e.getMessage());
      ApiResponse<RegisterDeveloperResponse> response =
          ApiResponse.error(
              "Failed to register developer: " + e.getMessage(), httpRequest.getRequestURI());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** User login POST /api/v1/auth/login */
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(
      @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {

    try {
      logger.info("Login attempt for identifier: {}", request.getIdentifier());

      // Get client IP address
      String ipAddress = getClientIpAddress(httpRequest);
      String userAgent = httpRequest.getHeader("User-Agent");

      AuthResponse authResponse = authService.login(request, ipAddress, userAgent);

      ApiResponse<AuthResponse> response =
          ApiResponse.success(authResponse, "Login berhasil", httpRequest.getRequestURI());

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      logger.error("Login failed for identifier {}: {}", request.getIdentifier(), e.getMessage());

      ApiResponse<AuthResponse> response =
          ApiResponse.error(e.getMessage(), httpRequest.getRequestURI());

      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(
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
      logger.error("Logout failed: {}", e.getMessage());

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
      logger.error("Token validation failed: {}", e.getMessage());

      ApiResponse<Boolean> response =
          ApiResponse.success(false, "Token tidak valid", httpRequest.getRequestURI());

      return ResponseEntity.ok(response);
    }
  }

  @GetMapping("/verify")
  public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
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
  public ResponseEntity<ApiResponse<?>> forgotPassword(
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

  /** Extract JWT token from Authorization header */
  // remove duplicate helper and delegate to JwtUtil
  // (deleted) private String extractTokenFromHeader(String authHeader) {
  // (deleted)   if (authHeader == null || !authHeader.startsWith("Bearer ")) {
  // (deleted)     throw new RuntimeException("Header Authorization tidak valid");
  // (deleted)   }
  // (deleted)   return authHeader.substring(7);
  // (deleted) }
}
