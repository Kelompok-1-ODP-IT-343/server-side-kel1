package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.ApiResponse;
import com.kelompoksatu.griya.dto.DeveloperResponse;
import com.kelompoksatu.griya.dto.PaginatedResponse;
import com.kelompoksatu.griya.dto.PaginationRequest;
import com.kelompoksatu.griya.dto.UpdateDeveloperRequest;
import com.kelompoksatu.griya.service.DeveloperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** REST Controller for Admin operations */
@Tag(name = "Admin Management", description = "Administrative operations for system management")
@RestController
@RequestMapping("/api/v1/admin")
@Validated
public class AdminController {

  private final DeveloperService developerService;

  @Autowired
  public AdminController(DeveloperService developerService) {
    this.developerService = developerService;
  }

  // ==================== DEVELOPER MANAGEMENT ====================

  /** Update developer information (admin only) */
  @Operation(
      summary = "Update developer information",
      description =
          "Update developer profile information. This endpoint is restricted to admin users only. All fields are optional for partial updates.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Developer updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Developer not found",
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
  @PutMapping("/developers/{id}")
  public ResponseEntity<ApiResponse<DeveloperResponse>> updateDeveloper(
      @PathVariable Integer id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Developer update request with optional fields",
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = UpdateDeveloperRequest.class)))
          @Valid
          @RequestBody
          UpdateDeveloperRequest request) {
    try {
      DeveloperResponse developer = developerService.updateDeveloper(id, request);
      ApiResponse<DeveloperResponse> response =
          ApiResponse.success("Developer updated successfully", developer);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      ApiResponse<DeveloperResponse> response =
          ApiResponse.error("Developer not found: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    } catch (Exception e) {
      ApiResponse<DeveloperResponse> response =
          ApiResponse.error("Failed to update developer: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get developer by ID (admin only) */
  @Operation(
      summary = "Get developer by ID",
      description =
          "Retrieve developer information by ID. This endpoint is restricted to admin users only.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Developer retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Developer not found",
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
  @GetMapping("/developers/{id}")
  public ResponseEntity<ApiResponse<DeveloperResponse>> getDeveloperById(@PathVariable Integer id) {
    try {
      Optional<DeveloperResponse> developer = developerService.getDeveloperById(id);
      if (developer.isPresent()) {
        ApiResponse<DeveloperResponse> response =
            new ApiResponse<>(true, "Developer retrieved successfully", developer.get());
        return ResponseEntity.ok(response);
      } else {
        ApiResponse<DeveloperResponse> response =
            new ApiResponse<>(false, "Developer not found with id: " + id, null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }
    } catch (Exception e) {
      ApiResponse<DeveloperResponse> response =
          ApiResponse.error("Failed to retrieve developer: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get all developers (admin only) */
  @Operation(
      summary = "Get all developers",
      description =
          "Retrieve all developers in the system with pagination support. This endpoint is restricted to admin users only.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "All developers retrieved successfully",
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
  @GetMapping("/developers")
  public ResponseEntity<ApiResponse<PaginatedResponse<DeveloperResponse>>> getAllDevelopers(
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Number of items per page", example = "10")
          @RequestParam(defaultValue = "10")
          int size,
      @Parameter(description = "Sort field name", example = "companyName")
          @RequestParam(defaultValue = "createdAt")
          String sortBy,
      @Parameter(description = "Sort direction", example = "desc")
          @RequestParam(defaultValue = "desc")
          String sortDirection) {
    try {
      PaginationRequest paginationRequest =
          new PaginationRequest(page, size, sortBy, sortDirection);
      PaginatedResponse<DeveloperResponse> developers =
          developerService.getAllDevelopers(paginationRequest);

      ApiResponse<PaginatedResponse<DeveloperResponse>> response =
          ApiResponse.success(
              developers, "All developers retrieved successfully", "/api/v1/admin/developers");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<PaginatedResponse<DeveloperResponse>> response =
          ApiResponse.error(
              "Failed to retrieve developers: " + e.getMessage(), "/api/v1/admin/developers");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get active developers with pagination (admin only) */
  @Operation(
      summary = "Get active developers",
      description =
          "Retrieve all active developers with pagination support. This endpoint is restricted to admin users only.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Active developers retrieved successfully",
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
  @GetMapping("/developers/active")
  public ResponseEntity<ApiResponse<PaginatedResponse<DeveloperResponse>>> getActiveDevelopers(
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Number of items per page", example = "10")
          @RequestParam(defaultValue = "10")
          int size,
      @Parameter(description = "Sort field name", example = "companyName")
          @RequestParam(defaultValue = "createdAt")
          String sortBy,
      @Parameter(description = "Sort direction", example = "desc")
          @RequestParam(defaultValue = "desc")
          String sortDirection) {
    try {
      PaginationRequest paginationRequest =
          new PaginationRequest(page, size, sortBy, sortDirection);
      PaginatedResponse<DeveloperResponse> developers =
          developerService.getActiveDevelopers(paginationRequest);

      ApiResponse<PaginatedResponse<DeveloperResponse>> response =
          ApiResponse.success(
              developers,
              "Active developers retrieved successfully",
              "/api/v1/admin/developers/active");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<PaginatedResponse<DeveloperResponse>> response =
          ApiResponse.error(
              "Failed to retrieve active developers: " + e.getMessage(),
              "/api/v1/admin/developers/active");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get partner developers with pagination (admin only) */
  @Operation(
      summary = "Get partner developers",
      description =
          "Retrieve all partner developers with pagination support. This endpoint is restricted to admin users only.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Partner developers retrieved successfully",
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
  @GetMapping("/developers/partners")
  public ResponseEntity<ApiResponse<PaginatedResponse<DeveloperResponse>>> getPartnerDevelopers(
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Number of items per page", example = "10")
          @RequestParam(defaultValue = "10")
          int size,
      @Parameter(description = "Sort field name", example = "companyName")
          @RequestParam(defaultValue = "createdAt")
          String sortBy,
      @Parameter(description = "Sort direction", example = "desc")
          @RequestParam(defaultValue = "desc")
          String sortDirection) {
    try {
      PaginationRequest paginationRequest =
          new PaginationRequest(page, size, sortBy, sortDirection);
      PaginatedResponse<DeveloperResponse> developers =
          developerService.getPartnerDevelopers(paginationRequest);

      ApiResponse<PaginatedResponse<DeveloperResponse>> response =
          ApiResponse.success(
              developers,
              "Partner developers retrieved successfully",
              "/api/v1/admin/developers/partners");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<PaginatedResponse<DeveloperResponse>> response =
          ApiResponse.error(
              "Failed to retrieve partner developers: " + e.getMessage(),
              "/api/v1/admin/developers/partners");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Search developers by company name with pagination (admin only) */
  @Operation(
      summary = "Search developers by company name",
      description =
          "Search developers by company name with pagination support. This endpoint is restricted to admin users only.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Search results retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Bad request - invalid search parameters",
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
  @GetMapping("/developers/search")
  public ResponseEntity<ApiResponse<PaginatedResponse<DeveloperResponse>>> searchDevelopers(
      @Parameter(description = "Company name to search for", example = "ABC") @RequestParam
          String companyName,
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Number of items per page", example = "10")
          @RequestParam(defaultValue = "10")
          int size,
      @Parameter(description = "Sort field name", example = "companyName")
          @RequestParam(defaultValue = "createdAt")
          String sortBy,
      @Parameter(description = "Sort direction", example = "desc")
          @RequestParam(defaultValue = "desc")
          String sortDirection) {
    try {
      if (companyName == null || companyName.trim().isEmpty()) {
        ApiResponse<PaginatedResponse<DeveloperResponse>> response =
            ApiResponse.error(
                "Company name parameter is required", "/api/v1/admin/developers/search");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
      }

      PaginationRequest paginationRequest =
          new PaginationRequest(page, size, sortBy, sortDirection);
      PaginatedResponse<DeveloperResponse> developers =
          developerService.searchDevelopersByCompanyName(companyName, paginationRequest);

      ApiResponse<PaginatedResponse<DeveloperResponse>> response =
          ApiResponse.success(
              developers,
              "Search results retrieved successfully",
              "/api/v1/admin/developers/search");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<PaginatedResponse<DeveloperResponse>> response =
          ApiResponse.error(
              "Failed to search developers: " + e.getMessage(), "/api/v1/admin/developers/search");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  // ==================== FUTURE ADMIN ENDPOINTS ====================
  // TODO: Add user management endpoints
  // TODO: Add property management endpoints
  // TODO: Add KPR application management endpoints
  // TODO: Add system configuration endpoints
}
