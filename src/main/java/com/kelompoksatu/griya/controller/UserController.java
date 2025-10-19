package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.ApiResponse;
import com.kelompoksatu.griya.dto.UpdateUserRequest;
import com.kelompoksatu.griya.dto.UserResponse;
import com.kelompoksatu.griya.service.AuthService;
import com.kelompoksatu.griya.service.UserService;
import com.kelompoksatu.griya.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

/** REST Controller for user operations API Version: v1 */
@Tag(name = "User Management", description = "User profile and account management operations")
@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class UserController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  private final AuthService authService;
  private final UserService userService;
  private final JwtUtil jwtUtil;

  /** Get user profile GET /api/v1/user/profile */
  @GetMapping("/profile")
  public ResponseEntity<ApiResponse<UserResponse>> getProfile(
      @RequestHeader("Authorization") String authHeader, HttpServletRequest httpRequest) {

    try {
      // Extract token from Authorization header
      String token = extractTokenFromHeader(authHeader);

      // Validate token
      if (!authService.validateToken(token)) {
        ApiResponse<UserResponse> response =
            ApiResponse.error(
                "Token tidak valid atau telah kedaluwarsa", httpRequest.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }

      // Get user profile
      UserResponse userResponse = authService.getProfile(token);

      ApiResponse<UserResponse> response =
          ApiResponse.success(
              userResponse, "Profil user berhasil diambil", httpRequest.getRequestURI());

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      logger.error("Failed to get user profile: {}", e.getMessage());

      ApiResponse<UserResponse> response =
          ApiResponse.error(e.getMessage(), httpRequest.getRequestURI());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get current user info (minimal endpoint for quick user verification) GET /api/v1/user/me */
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
      @RequestHeader("Authorization") String authHeader, HttpServletRequest httpRequest) {

    try {
      String token = extractTokenFromHeader(authHeader);

      if (!authService.validateToken(token)) {
        ApiResponse<UserResponse> response =
            ApiResponse.error(
                "Token tidak valid atau telah kedaluwarsa", httpRequest.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }

      UserResponse userResponse = authService.getProfile(token);

      ApiResponse<UserResponse> response =
          ApiResponse.success(
              userResponse, "Data user berhasil diambil", httpRequest.getRequestURI());

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      logger.error("Failed to get current user: {}", e.getMessage());

      ApiResponse<UserResponse> response =
          ApiResponse.error("Gagal mengambil data user", httpRequest.getRequestURI());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Update user information PUT /api/v1/user/{id} */
  @Operation(
      summary = "Update user information",
      description = "Update user profile information including username, email, phone, password, and status. All fields are optional for partial updates.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User updated successfully",
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
            responseCode = "401",
            description = "Unauthorized - invalid or expired token",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found",
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
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResponse>> updateUser(
      @Parameter(description = "User ID to update", example = "1") @PathVariable Integer id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "User update request with optional fields",
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = UpdateUserRequest.class)))
          @Valid
          @RequestBody
          UpdateUserRequest request,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest httpRequest) {

    logger.info("User update attempt for user ID: {}", id);

    try {
      // Extract and validate token
      String token = extractTokenFromHeader(authHeader);

      if (!authService.validateToken(token)) {
        ApiResponse<UserResponse> response =
            ApiResponse.error(
                "Token tidak valid atau telah kedaluwarsa", httpRequest.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }

      // Update user
      UserResponse updatedUser = userService.updateUser(id, request);

      ApiResponse<UserResponse> response =
          ApiResponse.success(
              updatedUser, "User updated successfully", httpRequest.getRequestURI());

      logger.info("User updated successfully for user ID: {}", id);
      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      logger.error("Validation error during user update: {}", e.getMessage());
      ApiResponse<UserResponse> response =
          ApiResponse.error(e.getMessage(), httpRequest.getRequestURI());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (RuntimeException e) {
      if (e.getMessage().contains("not found")) {
        logger.error("User not found: {}", e.getMessage());
        ApiResponse<UserResponse> response =
            ApiResponse.error("User not found", httpRequest.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }
      logger.error("Failed to update user: {}", e.getMessage());
      ApiResponse<UserResponse> response =
          ApiResponse.error("Failed to update user: " + e.getMessage(), httpRequest.getRequestURI());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    } catch (Exception e) {
      logger.error("Unexpected error during user update: {}", e.getMessage());
      ApiResponse<UserResponse> response =
          ApiResponse.error("An unexpected error occurred", httpRequest.getRequestURI());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Extract JWT token from Authorization header */
  private String extractTokenFromHeader(String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new RuntimeException("Authorization header tidak valid");
    }
    return authHeader.substring(7);
  }
}
