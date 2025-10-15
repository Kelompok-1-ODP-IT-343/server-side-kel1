package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.*;
import com.kelompoksatu.griya.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication operations
 * API Version: v1
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    /**
     * Register a new user
     * POST /api/v1/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        logger.info("Registration attempt for username: {}", request.getUsername());

        RegisterResponse authResponse = authService.register(request);

        ApiResponse<RegisterResponse> response = ApiResponse.success(
                authResponse,
                "Registrasi berhasil",
                httpRequest.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * User login
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        try {
            logger.info("Login attempt for identifier: {}", request.getIdentifier());

            // Get client IP address
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            AuthResponse authResponse = authService.login(request, ipAddress, userAgent);

            ApiResponse<AuthResponse> response = ApiResponse.success(
                    authResponse,
                    "Login berhasil",
                    httpRequest.getRequestURI()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Login failed for identifier {}: {}", request.getIdentifier(), e.getMessage());

            ApiResponse<AuthResponse> response = ApiResponse.error(
                    e.getMessage(),
                    httpRequest.getRequestURI()
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * User logout
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest) {

        try {
            // Extract token from Authorization header
            String token = extractTokenFromHeader(authHeader);

            authService.logout(token);

            ApiResponse<String> response = ApiResponse.success(
                    null,
                    "Logout berhasil",
                    httpRequest.getRequestURI()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Logout failed: {}", e.getMessage());

            ApiResponse<String> response = ApiResponse.error(
                    e.getMessage(),
                    httpRequest.getRequestURI()
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Validate JWT token
     * POST /api/v1/auth/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest) {

        try {
            String token = extractTokenFromHeader(authHeader);
            boolean isValid = authService.validateToken(token);

            ApiResponse<Boolean> response = ApiResponse.success(
                    isValid,
                    isValid ? "Token valid" : "Token tidak valid",
                    httpRequest.getRequestURI()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Token validation failed: {}", e.getMessage());

            ApiResponse<Boolean> response = ApiResponse.success(
                    false,
                    "Token tidak valid",
                    httpRequest.getRequestURI()
            );

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

    /**
     * Extract client IP address from request
     */
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

    /**
     * Extract JWT token from Authorization header
     */
    private String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization header tidak valid");
        }
        return authHeader.substring(7);
    }
}
