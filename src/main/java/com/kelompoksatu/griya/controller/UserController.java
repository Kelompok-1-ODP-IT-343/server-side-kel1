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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for user operations API Version: v1 */
@Tag(name = "User Management", description = "User profile and account management operations")
@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class UserController {

  private final AuthService authService;
  private final UserService userService;
  private final JwtUtil jwtUtil;

  /** Get user profile GET /api/v1/user/profile */
  @GetMapping("/profile")
  public ResponseEntity<ApiResponse<UserResponse>> getProfile(
      @RequestHeader("Authorization") String authHeader, HttpServletRequest httpRequest) {

    // Extract token from Authorization header
    String token = jwtUtil.extractTokenFromHeader(authHeader);

    // Get user profile
    UserResponse userResponse = authService.getProfile(token);

    ApiResponse<UserResponse> response =
        ApiResponse.success(
            userResponse, "Profil user berhasil diambil", httpRequest.getRequestURI());

    return ResponseEntity.ok(response);
  }

  /** Get current user info (minimal endpoint for quick user verification) GET /api/v1/user/me */
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
      @RequestHeader("Authorization") String authHeader, HttpServletRequest httpRequest) {

    String token = jwtUtil.extractTokenFromHeader(authHeader);

    UserResponse userResponse = authService.getProfile(token);

    ApiResponse<UserResponse> response =
        ApiResponse.success(
            userResponse, "Data user berhasil diambil", httpRequest.getRequestURI());

    return ResponseEntity.ok(response);
  }

  /** Update user information PUT /api/v1/user/{id} */
  @Operation(
      summary = "Update user information",
      description =
          "Update user account and profile information including username, email, phone, status, and profile details. Users can only update their own profile. Password updates are not allowed (use reset-password endpoint).")
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
            responseCode = "403",
            description = "Forbidden - user can only update their own profile",
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
              description =
                  "User update request with optional user account and profile information",
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = UpdateUserRequest.class)))
          @Valid
          @RequestBody
          UpdateUserRequest request,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest httpRequest) {

    log.info("User update attempt for user ID: {}", id);

    try {
      // Extract and validate token
      String token = jwtUtil.extractTokenFromHeader(authHeader);

      if (!authService.validateToken(token)) {
        ApiResponse<UserResponse> response =
            ApiResponse.error(
                "Token tidak valid atau telah kedaluwarsa", httpRequest.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
      }

      // Extract user ID from JWT token
      Integer tokenUserId = jwtUtil.extractUserId(token);

      // Ensure user can only update their own profile
      if (!tokenUserId.equals(id)) {
        log.warn("User {} attempted to update profile of user {}", tokenUserId, id);
        ApiResponse<UserResponse> response =
            ApiResponse.error("You can only update your own profile", httpRequest.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
      }

      // Update user and profile information
      UserResponse updatedUser = userService.updateUserComplete(id, request);

      ApiResponse<UserResponse> response =
          ApiResponse.success(
              updatedUser, "User updated successfully", httpRequest.getRequestURI());

      log.info("User updated successfully for user ID: {}", id);
      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      log.error("Validation error during user update: {}", e.getMessage());
      ApiResponse<UserResponse> response =
          ApiResponse.error(e.getMessage(), httpRequest.getRequestURI());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (RuntimeException e) {
      if (e.getMessage().contains("not found")) {
        log.error("User not found: {}", e.getMessage());
        ApiResponse<UserResponse> response =
            ApiResponse.error("User not found", httpRequest.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }
      log.error("Failed to update user: {}", e.getMessage());
      ApiResponse<UserResponse> response =
          ApiResponse.error(
              "Failed to update user: " + e.getMessage(), httpRequest.getRequestURI());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    } catch (Exception e) {
      log.error("Unexpected error during user update: {}", e.getMessage());
      ApiResponse<UserResponse> response =
          ApiResponse.error("An unexpected error occurred", httpRequest.getRequestURI());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }
}
