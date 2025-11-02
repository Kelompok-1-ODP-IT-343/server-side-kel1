package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.*;
import com.kelompoksatu.griya.entity.ImageAdmin;
import com.kelompoksatu.griya.entity.ImageCategory;
import com.kelompoksatu.griya.entity.ImageType;
import com.kelompoksatu.griya.repository.ImageAdminRepository;
import com.kelompoksatu.griya.repository.PropertyFavoriteRepository;
import com.kelompoksatu.griya.service.AdminService;
import com.kelompoksatu.griya.service.DeveloperService;
import com.kelompoksatu.griya.service.PropertyService;
import com.kelompoksatu.griya.service.UserService;
import com.kelompoksatu.griya.util.IDCloudHostS3Util;
import com.kelompoksatu.griya.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/** REST Controller for Admin operations */
@Tag(name = "Admin Management", description = "Administrative operations for system management")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@Validated
@RequiredArgsConstructor
public class AdminController {

  private final DeveloperService developerService;
  private final ImageAdminRepository imageAdminRepository;
  private final AdminService adminService;
  private final PropertyFavoriteRepository propertyFavoriteRepository;
  private final PropertyService propertyService;
  private final UserService userService;
  private final JwtUtil jwtUtil;
  private final IDCloudHostS3Util idCloudHostS3Util;

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
  @GetMapping("/properties")
  public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAdminProperties(
      @RequestParam(required = false) String city,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(required = false) String propertyType) {

    try {
      List<Map<String, Object>> properties =
          propertyService.getPropertiesSimpleByFilters(city, minPrice, maxPrice, propertyType);

      return ResponseEntity.ok(
          ApiResponse.success("Properties retrieved successfully", properties));

    } catch (Exception e) {
      log.error("❌ Gagal mengambil properties: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Gagal mengambil properties: " + e.getMessage()));
    }
  }

  /** Update property information (admin only) */
  @Operation(
      summary = "Update property information",
      description =
          "Update property data including details, images, features, and locations. This endpoint is restricted to admin users only.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Property updated successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpdatePropertyResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Property not found",
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
  @PutMapping("/properties/{id}")
  public ResponseEntity<ApiResponse<UpdatePropertyResponse>> updateProperty(
      @PathVariable Integer id, @Valid @RequestBody UpdatePropertyRequest request) {

    try {
      // 🔹 panggil service untuk update semua relasi
      UpdatePropertyResponse updatedProperty = propertyService.updateProperty(id, request);

      return ResponseEntity.ok(
          ApiResponse.success("Property updated successfully", updatedProperty));

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error("Property not found: " + e.getMessage()));

    } catch (Exception e) {
      log.error("❌ Failed to update property: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Failed to update property: " + e.getMessage()));
    }
  }

  @GetMapping("/users/{userId}/favorites")
  public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUserFavorites(
      @PathVariable Integer userId) {

    try {
      List<Map<String, Object>> favorites =
          propertyFavoriteRepository.findFavoritesByUserId(userId);

      if (favorites.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("No favorites found for user ID " + userId));
      }

      return ResponseEntity.ok(ApiResponse.success("Favorites retrieved successfully", favorites));

    } catch (Exception e) {
      log.error("❌ Gagal mengambil favorites user: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Gagal mengambil favorites: " + e.getMessage()));
    }
  }

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
          UpdateDeveloperRequest request,
      HttpServletRequest httpRequest) {
    DeveloperResponse developer = developerService.updateDeveloper(id, request);
    ApiResponse<DeveloperResponse> response =
        ApiResponse.success(
            developer, "Developer updated successfully", httpRequest.getRequestURI());
    return ResponseEntity.ok(response);
  }

  @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<ImageAdminResponse>> uploadAdminImage(
      @RequestPart("image") MultipartFile image, @RequestPart("data") ImageAdminRequest request) {
    try {
      log.info("Mulai upload image: {}", image.getOriginalFilename());

      // Validasi input
      if (image == null || image.isEmpty()) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Image file tidak boleh kosong"));
      }
      if (request == null || request.getImageType() == null || request.getImageCategory() == null) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Field imageType dan imageCategory wajib diisi"));
      }

      // Convert ENUM (handle invalid enum)
      ImageType imageType;
      ImageCategory imageCategory;
      try {
        imageType = ImageType.valueOf(request.getImageType().name());
        imageCategory = ImageCategory.valueOf(request.getImageCategory().name());
      } catch (IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Nilai imageType atau imageCategory tidak valid"));
      }

      // Upload ke IDCloudHost
      String folder =
          String.valueOf(request.getPropertyId() == null ? "misc" : request.getPropertyId());
      String imageUrl = idCloudHostS3Util.uploadPropertyImage(image, folder);
      log.info("Upload ke IDCloudHost sukses: {}", imageUrl);

      // Simpan metadata ke database
      ImageAdmin imageAdmin =
          ImageAdmin.builder()
              .propertyId(request.getPropertyId())
              .imageType(imageType)
              .imageCategory(imageCategory)
              .fileName(UUID.randomUUID().toString())
              .filePath(imageUrl)
              .fileSize((int) image.getSize())
              .mimeType(image.getContentType())
              .caption(request.getCaption())
              .build();

      imageAdminRepository.save(imageAdmin);
      log.info("Image berhasil disimpan di DB: {}", imageAdmin.getFileName());

      // Build response data
      ImageAdminResponse responseData =
          ImageAdminResponse.builder()
              .id(imageAdmin.getId())
              .propertyId(imageAdmin.getPropertyId())
              .imageUrl(imageUrl)
              .fileName(imageAdmin.getFileName())
              .imageType(imageType.name())
              .imageCategory(imageCategory.name())
              .caption(imageAdmin.getCaption())
              .fileSize(imageAdmin.getFileSize())
              .mimeType(imageAdmin.getMimeType())
              .build();

      ApiResponse<ImageAdminResponse> response =
          ApiResponse.success("Image uploaded successfully", responseData);
      return ResponseEntity.ok(response);

    } catch (IOException e) {
      log.error("❌ Gagal menyimpan file: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Gagal menyimpan file: " + e.getMessage()));
    } catch (Exception e) {
      log.error("❌ Gagal upload image: ", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("Gagal upload image: " + e.getMessage()));
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
  public ResponseEntity<ApiResponse<DeveloperResponse>> getDeveloperById(
      @PathVariable Integer id, HttpServletRequest httpRequest) {
    Optional<DeveloperResponse> developer = developerService.getDeveloperById(id);
    if (developer.isPresent()) {
      ApiResponse<DeveloperResponse> response =
          ApiResponse.success(
              developer.get(), "Developer retrieved successfully", httpRequest.getRequestURI());
      return ResponseEntity.ok(response);
    } else {
      ApiResponse<DeveloperResponse> response =
          new ApiResponse<>(false, "Developer not found with id: " + id, null);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
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
          String sortDirection,
      HttpServletRequest httpRequest) {
    PaginationRequest paginationRequest = new PaginationRequest(page, size, sortBy, sortDirection);
    PaginatedResponse<DeveloperResponse> developers =
        developerService.getAllDevelopers(paginationRequest);

    ApiResponse<PaginatedResponse<DeveloperResponse>> response =
        ApiResponse.success(
            developers, "All developers retrieved successfully", httpRequest.getRequestURI());

    return ResponseEntity.ok(response);
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
          String sortDirection,
      HttpServletRequest httpRequest) {
    PaginationRequest paginationRequest = new PaginationRequest(page, size, sortBy, sortDirection);
    PaginatedResponse<DeveloperResponse> developers =
        developerService.getActiveDevelopers(paginationRequest);

    ApiResponse<PaginatedResponse<DeveloperResponse>> response =
        ApiResponse.success(
            developers, "Active developers retrieved successfully", httpRequest.getRequestURI());

    return ResponseEntity.ok(response);
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
          String sortDirection,
      HttpServletRequest httpRequest) {
    PaginationRequest paginationRequest = new PaginationRequest(page, size, sortBy, sortDirection);
    PaginatedResponse<DeveloperResponse> developers =
        developerService.getPartnerDevelopers(paginationRequest);

    ApiResponse<PaginatedResponse<DeveloperResponse>> response =
        ApiResponse.success(
            developers, "Partner developers retrieved successfully", httpRequest.getRequestURI());

    return ResponseEntity.ok(response);
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
          String sortDirection,
      HttpServletRequest httpRequest) {
    if (companyName == null || companyName.trim().isEmpty()) {
      ApiResponse<PaginatedResponse<DeveloperResponse>> response =
          ApiResponse.error(
              "Company name parameter is required", "/api/v1/admin/developers/search");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    PaginationRequest paginationRequest = new PaginationRequest(page, size, sortBy, sortDirection);
    PaginatedResponse<DeveloperResponse> developers =
        developerService.searchDevelopersByCompanyName(companyName, paginationRequest);

    ApiResponse<PaginatedResponse<DeveloperResponse>> response =
        ApiResponse.success(
            developers, "Search results retrieved successfully", httpRequest.getRequestURI());

    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get All Admin",
      description = "Get All Admin. This endpoint is restricted to admin users only.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Get All successfully",
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
  @GetMapping("/simple")
  public ResponseEntity<ApiResponse<List<AdminSimpleResponse>>> getAllSimple() {

    List<AdminSimpleResponse> admin = adminService.getAllAdminSimple();
    var apiResponse = new ApiResponse<>(true, "", admin);

    return ResponseEntity.ok(apiResponse);
  }

  @GetMapping("/approver")
  public ResponseEntity<ApiResponse<List<UserResponse>>> getAllApprovers(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    var token = jwtUtil.extractTokenFromHeader(authHeader);

    // Extract user ID from token
    Integer userId = jwtUtil.extractUserId(token);
    if (userId == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.error("Token tidak valid"));
    }
    List<UserResponse> approver = adminService.getAllApprovalPovAdmin(userId);
    return ResponseEntity.ok(ApiResponse.success("Approver retrieved successfully", approver));
  }

  @GetMapping("/users")
  public ResponseEntity<ApiResponse<PaginatedResponse<UserResponse>>> getAllUsers(
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Number of items per page", example = "10")
          @RequestParam(defaultValue = "10")
          int size,
      @Parameter(description = "Sort field name", example = "fullName")
          @RequestParam(defaultValue = "createdAt")
          String sortBy,
      @Parameter(description = "Sort direction", example = "desc")
          @RequestParam(defaultValue = "desc")
          String sortDirection,
      HttpServletRequest httpRequest) {
    PaginationRequest paginationRequest = new PaginationRequest(page, size, sortBy, sortDirection);
    PaginatedResponse<UserResponse> users = userService.getAllUsers(paginationRequest);

    ApiResponse<PaginatedResponse<UserResponse>> response =
        ApiResponse.success(users, "All users retrieved successfully", httpRequest.getRequestURI());

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/users/{id}")
  public ResponseEntity<ApiResponse<Void>> hardDeleteUser(
      @PathVariable Integer id,
      @RequestParam(required = false) String reason,
      @RequestHeader("Authorization") String authHeader,
      HttpServletRequest httpRequest) {

    Integer adminId = jwtUtil.extractUserIdFromHeader(authHeader);
    adminService.hardDeleteUser(id, adminId, reason);

    return ResponseEntity.ok(
        ApiResponse.success(null, "User berhasil dihapus permanen", httpRequest.getRequestURI()));
  }

  @GetMapping("/users/{id}")
  public ResponseEntity<ApiResponse<UserResponse>> getUserById(
      @PathVariable Integer id, HttpServletRequest httpRequest) {
    UserResponse userProfile = userService.getUserProfile(id);

    return ResponseEntity.ok(
        ApiResponse.success(userProfile, "User berhasil diambil", httpRequest.getRequestURI()));
  }

  @PutMapping("/users/{id}")
  public ResponseEntity<ApiResponse<UserResponse>> updateUser(
      @Parameter(description = "User ID to update", example = "1") @PathVariable Integer id,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description =
                  "User update request from Admin with optional user account and profile information",
              content =
                  @Content(
                      mediaType = "application/json",
                      schema = @Schema(implementation = UpdateUserRequest.class)))
          @Valid
          @RequestBody
          UpdateUserRequest request,
      HttpServletRequest httpRequest) {

    log.info("User update attempt for user ID: {}", id);

    // Update user and profile information
    UserResponse updatedUser = userService.updateUserAndProfile(id, request);

    ApiResponse<UserResponse> response =
        ApiResponse.success(
            updatedUser, "User updated successfully by admin", httpRequest.getRequestURI());

    log.info("User updated successfully for user ID: {}", id);
    return ResponseEntity.ok(response);
  }
}
