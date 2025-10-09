package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.ApiResponse;
import com.kelompoksatu.griya.dto.UserResponse;
import com.kelompoksatu.griya.service.AuthService;
import com.kelompoksatu.griya.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user operations
 * API Version: v1
 */
@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Get user profile
     * GET /api/v1/user/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest) {

        try {
            // Extract token from Authorization header
            String token = extractTokenFromHeader(authHeader);

            // Validate token
            if (!authService.validateToken(token)) {
                ApiResponse<UserResponse> response = ApiResponse.error(
                        "Token tidak valid atau telah kedaluwarsa",
                        httpRequest.getRequestURI()
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Get user profile
            UserResponse userResponse = authService.getProfile(token);

            ApiResponse<UserResponse> response = ApiResponse.success(
                    userResponse,
                    "Profil user berhasil diambil",
                    httpRequest.getRequestURI()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get user profile: {}", e.getMessage());

            ApiResponse<UserResponse> response = ApiResponse.error(
                    e.getMessage(),
                    httpRequest.getRequestURI()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get current user info (minimal endpoint for quick user verification)
     * GET /api/v1/user/me
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest) {

        try {
            String token = extractTokenFromHeader(authHeader);

            if (!authService.validateToken(token)) {
                ApiResponse<UserResponse> response = ApiResponse.error(
                        "Token tidak valid atau telah kedaluwarsa",
                        httpRequest.getRequestURI()
                );
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            UserResponse userResponse = authService.getProfile(token);

            ApiResponse<UserResponse> response = ApiResponse.success(
                    userResponse,
                    "Data user berhasil diambil",
                    httpRequest.getRequestURI()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to get current user: {}", e.getMessage());

            ApiResponse<UserResponse> response = ApiResponse.error(
                    "Gagal mengambil data user",
                    httpRequest.getRequestURI()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
