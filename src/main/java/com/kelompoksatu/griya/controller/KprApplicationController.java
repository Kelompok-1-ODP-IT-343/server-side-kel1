package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.ApiResponse;
import com.kelompoksatu.griya.dto.KprApplicationRequest;
import com.kelompoksatu.griya.dto.KprApplicationResponse;
import com.kelompoksatu.griya.service.AuthService;
import com.kelompoksatu.griya.service.KprApplicationService;
import com.kelompoksatu.griya.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for KPR Application operations Handles home loan application submissions with JWT
 * authentication
 */
@RestController
@RequestMapping("/api/me")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "KPR Applications", description = "KPR (Home Loan) Application Management")
public class KprApplicationController {

  private final KprApplicationService kprApplicationService;
  private final AuthService authService;
  private final JwtUtil jwtUtil;

  /** Submit a new KPR application POST /api/me/kpr-applications */
  @PostMapping("/kpr-applications")
  @Operation(
      summary = "Submit KPR Application",
      description =
          "Submit a new KPR (home loan) application for an authenticated user. "
              + "The system validates the request, calculates loan details, selects appropriate interest rate, "
              + "and saves the application for approval process.",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Application submitted successfully",
            content = @Content(schema = @Schema(implementation = KprApplicationResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad Request - Validation failed",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Not Found - Property not found or not available",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "422",
            description = "Unprocessable Entity - No matching active KPR rate",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(schema = @Schema(implementation = ApiResponse.class)))
      })
  public ResponseEntity<ApiResponse<KprApplicationResponse>> submitKprApplication(
      @Parameter(description = "JWT Bearer token", required = true) @RequestHeader("Authorization")
          String authHeader,
      @Parameter(description = "KPR application request data", required = true) @Valid @RequestBody
          KprApplicationRequest request,
      HttpServletRequest httpRequest) {

    try {
      log.info("Received KPR application request for property ID: {}", request.getPropertyId());

      // 1. Extract and validate JWT token
      String token = extractTokenFromHeader(authHeader);

      if (!authService.validateToken(token)) {
        log.warn("Invalid or expired JWT token provided");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(
                ApiResponse.error(
                    "Token tidak valid atau telah kedaluwarsa", httpRequest.getRequestURI()));
      }

      // 2. Extract user ID from JWT token
      Integer userId = jwtUtil.extractUserId(token);
      if (userId == null) {
        log.warn("User ID not found in JWT token");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(
                ApiResponse.error(
                    "User ID tidak ditemukan dalam token", httpRequest.getRequestURI()));
      }

      log.info("Processing KPR application for user ID: {}", userId);

      // 3. Submit KPR application
      KprApplicationResponse response = kprApplicationService.submitApplication(userId, request);

      log.info(
          "KPR application submitted successfully with ID: {} for user: {}",
          response.getApplicationId(),
          userId);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              ApiResponse.success(
                  response, "Aplikasi KPR berhasil disubmit", httpRequest.getRequestURI()));

    } catch (IllegalArgumentException e) {
      log.warn("Validation error in KPR application: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error(e.getMessage(), httpRequest.getRequestURI()));

    } catch (IllegalStateException e) {
      log.warn("Business rule violation in KPR application: {}", e.getMessage());

      // Determine appropriate HTTP status based on error message
      HttpStatus status = determineErrorStatus(e.getMessage());

      return ResponseEntity.status(status)
          .body(ApiResponse.error(e.getMessage(), httpRequest.getRequestURI()));

    } catch (RuntimeException e) {
      log.error("Authentication error in KPR application: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.error(e.getMessage(), httpRequest.getRequestURI()));

    } catch (Exception e) {
      log.error("Unexpected error during KPR application submission", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              ApiResponse.error("Terjadi kesalahan internal server", httpRequest.getRequestURI()));
    }
  }

  /** Extract JWT token from Authorization header */
  private String extractTokenFromHeader(String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new RuntimeException("Authorization header tidak valid");
    }
    return authHeader.substring(7);
  }

  /** Determine appropriate HTTP status based on error message */
  private HttpStatus determineErrorStatus(String errorMessage) {
    String lowerMessage = errorMessage.toLowerCase();

    if (lowerMessage.contains("not found") || lowerMessage.contains("tidak ditemukan")) {
      return HttpStatus.NOT_FOUND;
    } else if (lowerMessage.contains("not available") || lowerMessage.contains("tidak tersedia")) {
      return HttpStatus.NOT_FOUND;
    } else if (lowerMessage.contains("no eligible") || lowerMessage.contains("tidak ada rate")) {
      return HttpStatus.UNPROCESSABLE_ENTITY;
    } else if (lowerMessage.contains("suspended") || lowerMessage.contains("not eligible")) {
      return HttpStatus.FORBIDDEN;
    } else if (lowerMessage.contains("pending application")
        || lowerMessage.contains("sudah ada aplikasi")) {
      return HttpStatus.CONFLICT;
    }

    return HttpStatus.BAD_REQUEST;
  }
}
