package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.*;
import com.kelompoksatu.griya.service.KprApplicationService;
import com.kelompoksatu.griya.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for KPR (Kredit Pemilikan Rumah) Application Management Handles loan application
 * submissions with comprehensive validation and security
 *
 * <p>Compliance: - OJK Regulations (POJK No. 13/POJK.02/2018) - Indonesian Personal Data Protection
 * Law (UU No. 27/2022) - ISO 27001 Security Standards
 */
@RestController
@RequestMapping("/api/v1/kpr-applications")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "KPR Applications", description = "KPR (Home Loan) application management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class KprApplicationController {

  private static final Logger logger = LoggerFactory.getLogger(KprApplicationController.class);
  private final KprApplicationService kprApplicationService;
  private final JwtUtil jwtUtil;

  /**
   * Submit a new KPR application with form-data including file uploads
   *
   * @param propertyId Property ID
   * @param kprRateId KPR Rate ID
   * @param propertyValue Property value for simulation
   * @param downPayment Down payment amount
   * @param loanAmount Loan amount requested
   * @param loanTermYears Loan term in years
   * @param fullName Full name of applicant
   * @param nik NIK (Nomor Induk Kependudukan)
   * @param npwp NPWP number
   * @param birthDate Birth date (YYYY-MM-DD)
   * @param birthPlace Birth place
   * @param gender Gender (male/female)
   * @param maritalStatus Marital status
   * @param address Address
   * @param city City
   * @param province Province
   * @param postalCode Postal code
   * @param occupation Occupation
   * @param monthlyIncome Monthly income
   * @param companyName Company name
   * @param companyAddress Company address
   * @param companyCity Company city
   * @param companyProvince Company province
   * @param companyPostalCode Company postal code
   * @param ktpDocument KTP document file
   * @param npwpDocument NPWP document file
   * @param salarySlipDocument Salary slip document file
   * @param otherDocument Other supporting document (optional)
   * @param authHeader JWT token in Authorization header
   * @return KprApplicationResponse with application details
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Submit KPR Application w`ith Documents",
      description =
          "Submit a new KPR (Home Loan) application with property details, personal data, "
              + "employment information, and required documents. Supports file uploads for KTP, NPWP, "
              + "salary slip, and other supporting documents.")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "KPR application submitted successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = KprApplicationResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request data, validation failed, or file upload error",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Property not found or user not found",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Conflict - User already has pending application for this property",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "413",
            description = "File too large - exceeds maximum allowed size",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "415",
            description = "Unsupported file type",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json"))
      })
  public ResponseEntity<?> submitKprApplication(
      // Main Data
      @RequestParam("propertyId") Integer propertyId,
      @RequestParam("kprRateId") Integer kprRateId,

      // Simulation Data
      @RequestParam("simulationData.propertyValue") BigDecimal propertyValue,
      @RequestParam("simulationData.downPayment") BigDecimal downPayment,
      @RequestParam("simulationData.loanAmount") BigDecimal loanAmount,
      @RequestParam("simulationData.loanTermYears") Integer loanTermYears,

      // Personal Data
      @RequestParam("personalData.fullName") String fullName,
      @RequestParam("personalData.nik") String nik,
      @RequestParam(value = "personalData.npwp", required = false) String npwp,
      @RequestParam("personalData.birthDate") String birthDate,
      @RequestParam("personalData.birthPlace") String birthPlace,
      @RequestParam("personalData.gender") String gender,
      @RequestParam("personalData.maritalStatus") String maritalStatus,
      @RequestParam("personalData.address") String address,
      @RequestParam(value = "personalData.district", required = false) String district,
      @RequestParam(value = "personalData.subDistrict", required = false) String subDistrict,
      @RequestParam("personalData.city") String city,
      @RequestParam("personalData.province") String province,
      @RequestParam("personalData.postalCode") String postalCode,

      // Employment Data
      @RequestParam("employmentData.occupation") String occupation,
      @RequestParam("employmentData.monthlyIncome") BigDecimal monthlyIncome,
      @RequestParam("employmentData.companyName") String companyName,
      @RequestParam("employmentData.companyAddress") String companyAddress,
      @RequestParam("employmentData.companyCity") String companyCity,
      @RequestParam("employmentData.companyProvince") String companyProvince,
      @RequestParam("employmentData.companyPostalCode") String companyPostalCode,
      @RequestParam(value = "employmentData.companyDistrict", required = false)
          String companyDistrict,
      @RequestParam(value = "employmentData.companySubdistrict", required = false)
          String companySubdistrict,
      // Document Files
      @RequestParam(value = "ktpDocument", required = false) MultipartFile ktpDocument,
      @RequestParam(value = "ktp", required = false) MultipartFile ktpAlt,
      @RequestParam(value = "npwpDocument", required = false) MultipartFile npwpDocument,
      @RequestParam(value = "npwp", required = false) MultipartFile npwpAlt,
      @RequestParam(value = "salarySlipDocument", required = false)
          MultipartFile salarySlipDocument,
      @RequestParam(value = "salarySlip", required = false) MultipartFile salarySlipAlt,
      @RequestParam(value = "otherDocument", required = false) MultipartFile otherDocument,
      @RequestParam(value = "other", required = false) MultipartFile otherAlt,
      @RequestParam("bankAccountNumber") String bankAccountNumber,
      @RequestParam(value = "notes", required = false) String notes,
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {

    try {
      logger.info("Received KPR application form-data request for property ID: {}", propertyId);

      // Extract and validate JWT token
      var token = jwtUtil.extractTokenFromHeader(authHeader);

      // Extract user ID from token
      Integer userId = jwtUtil.extractUserId(token);
      if (userId == null) {
        logger.warn("Invalid token - user ID not found");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("Token tidak valid"));
      }

      logger.info("Processing KPR application for user ID: {}", userId);

      // Build form request object
      // Resolve possible alternative file field names
      MultipartFile ktpResolved = ktpDocument != null ? ktpDocument : ktpAlt;
      MultipartFile npwpResolved = npwpDocument != null ? npwpDocument : npwpAlt;
      MultipartFile salarySlipResolved =
          salarySlipDocument != null ? salarySlipDocument : salarySlipAlt;
      MultipartFile otherResolved = otherDocument != null ? otherDocument : otherAlt;

      var formRequest =
          KprApplicationFormRequest.builder()
              .propertyId(propertyId)
              .kprRateId(kprRateId)
              .bankAccountNumber(bankAccountNumber)
              .simulationData(
                  SimulationData.builder()
                      .propertyValue(propertyValue)
                      .downPayment(downPayment)
                      .loanAmount(loanAmount)
                      .loanTermYears(loanTermYears)
                      .build())
              .personalData(
                  PersonalData.builder()
                      .fullName(fullName)
                      .nik(nik)
                      .npwp(npwp)
                      .birthDate(LocalDate.parse(birthDate))
                      .birthPlace(birthPlace)
                      .gender(gender)
                      .maritalStatus(maritalStatus)
                      .address(address)
                      .district(district)
                      .subDistrict(subDistrict)
                      .city(city)
                      .province(province)
                      .postalCode(postalCode)
                      .build())
              .employmentData(
                  EmploymentData.builder()
                      .occupation(occupation)
                      .monthlyIncome(monthlyIncome)
                      .companyName(companyName)
                      .companyAddress(companyAddress)
                      .companyCity(companyCity)
                      .companyProvince(companyProvince)
                      .companyPostalCode(companyPostalCode)
                      .companyDistrict(companyDistrict)
                      .companySubdistrict(companySubdistrict)
                      .build())
              .ktpDocument(ktpResolved)
              .npwpDocument(npwpResolved)
              .salarySlipDocument(salarySlipResolved)
              .otherDocument(otherResolved)
              .notes(notes)
              .build();

      try {
        formRequest.validate();
      } catch (IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse<>(false, ex.getMessage(), null));
      }

      // Submit application through service
      KprApplicationResponse response =
          kprApplicationService.submitApplicationWithDocuments(userId, formRequest);

      logger.info(
          "KPR application submitted successfully with ID: {}", response.getApplicationId());

      return ResponseEntity.status(HttpStatus.CREATED).body(response);

    } catch (Exception e) {
      logger.error("Error processing KPR application: {}", e.getMessage(), e);
      ApiResponse<KprApplicationResponse> response =
          new ApiResponse<>(false, "Failed to submit KPR application: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Create standardized error response */
  /**
   * Get KPR application detail with documents
   *
   * @param applicationId Application ID
   * @param authHeader JWT token in Authorization header
   * @return KprApplicationDetailResponse with application and document details
   */
  @GetMapping("/{applicationId}")
  @Operation(
      summary = "Get KPR Application Detail",
      description =
          "Retrieve detailed information about a KPR application including all associated documents. "
              + "Users can only access their own applications.")
  @io.swagger.v3.oas.annotations.responses.ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "KPR application detail retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = KprApplicationDetailResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have access to this application",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "KPR application not found",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json"))
      })
  public ResponseEntity<ApiResponse<?>> getKprApplicationDetail(
      @PathVariable("applicationId") Integer applicationId,
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    try {
      logger.info("Received request for KPR application detail ID: {}", applicationId);

      // Extract and validate JWT token
      var token = jwtUtil.extractTokenFromHeader(authHeader);

      // Extract user ID from token
      Integer userId = jwtUtil.extractUserId(token);
      if (userId == null) {
        logger.warn("Invalid token - user ID not found");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse<>(false, "Token tidak valid", null));
      }

      logger.info("Processing KPR application detail request for user ID: {}", userId);

      // Get application detail through service
      KprApplicationDetailResponse response =
          kprApplicationService.getApplicationDetail(applicationId, userId);

      logger.info("KPR application detail retrieved successfully for ID: {}", applicationId);
      return ResponseEntity.ok(
          new ApiResponse<>(true, "KPR application detail retrieved successfully", response));

    } catch (Exception e) {
      logger.error("Error retrieving KPR application detail: {}", e.getMessage(), e);

      // Determine appropriate HTTP status based on error type
      ApiResponse<KprApplicationDetailResponse> response =
          new ApiResponse<>(false, "Failed to get KPR application detail: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * Get KPR application history for a user
   *
   * @param authHeader JWT token in Authorization header
   * @return List of KprHistoryListResponse with application history
   */
  @GetMapping("/user/history")
  @Operation(
      summary = "Get KPR Application History",
      description =
          "Retrieve the history of KPR applications for the authenticated user. "
              + "Users can only access their own history.")
  @io.swagger.v3.oas.annotations.responses.ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "KPR application history retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = KprHistoryListResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have access to this history",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json"))
      })
  public ResponseEntity<ApiResponse<List<KprHistoryListResponse>>> getHistoryUser(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    try {
      logger.info("Received request for KPR application history");

      // Extract and validate JWT token
      var token = jwtUtil.extractTokenFromHeader(authHeader);

      // Extract user ID from token
      Integer userId = jwtUtil.extractUserId(token);
      if (userId == null) {
        logger.warn("Invalid token - user ID not found");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse<>(false, "Token tidak valid", null));
      }

      logger.info("Processing KPR application history request for user ID: {}", userId);

      // Get application history through service
      List<KprHistoryListResponse> response = kprApplicationService.getApplicationHistory(userId);

      logger.info("KPR application history retrieved successfully for user ID: {}", userId);
      return ResponseEntity.ok(
          new ApiResponse<>(true, "KPR application history retrieved successfully", response));

    } catch (Exception e) {
      logger.error("Error retrieving KPR application history: {}", e.getMessage(), e);

      // Determine appropriate HTTP status based on error type
      ApiResponse<List<KprHistoryListResponse>> response =
          new ApiResponse<>(
              false, "Failed to get KPR application history: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @GetMapping("/verifikator/history")
  @Operation(
      summary = "Get KPR Applications Assigned to Verifikator",
      description =
          "Retrieve the history of KPR applications assigned to the authenticated verifikator. "
              + "Verifikators can only access applications assigned to them.")
  @io.swagger.v3.oas.annotations.responses.ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "KPR application history retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = KprHistoryListResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - User does not have verifikator access",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json"))
      })
  public ResponseEntity<ApiResponse<List<KprHistoryListResponse>>> getHistoryAssignedVerifikator(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    try {
      logger.info("Received request for KPR application history assigned to verifikator");

      // Extract and validate JWT token
      var token = jwtUtil.extractTokenFromHeader(authHeader);

      // Extract user ID from token
      Integer userId = jwtUtil.extractUserId(token);
      if (userId == null) {
        logger.warn("Invalid token - user ID not found");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse<>(false, "Token tidak valid", null));
      }

      logger.info("Processing KPR application history request for user ID: {}", userId);

      // Get application history through service
      List<KprHistoryListResponse> response =
          kprApplicationService.getAssignedVerifikatorHistory(userId);

      logger.info("KPR application history retrieved successfully for user ID: {}", userId);
      return ResponseEntity.ok(
          new ApiResponse<>(true, "KPR application history retrieved successfully", response));

    } catch (Exception e) {
      logger.error("Error retrieving KPR application history: {}", e.getMessage(), e);

      // Determine appropriate HTTP status based on error type
      ApiResponse<List<KprHistoryListResponse>> response =
          new ApiResponse<>(
              false, "Failed to get KPR application history: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @GetMapping("/verifikator/progress")
  public ResponseEntity<ApiResponse<List<KprInProgress>>> getAssignedKprApplicationsOnProgress(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    try {
      logger.info("Received request for KPR application history assigned to verifikator");

      // Extract and validate JWT token
      var token = jwtUtil.extractTokenFromHeader(authHeader);

      // Extract user ID from token
      Integer userId = jwtUtil.extractUserId(token);
      if (userId == null) {
        logger.warn("Invalid token - user ID not found");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse<>(false, "Token tidak valid", null));
      }

      logger.info("Processing KPR application history request for user ID: {}", userId);

      // Get application history through service
      List<KprInProgress> response =
          kprApplicationService.getAssignedKprApplicationsOnProgress(userId);

      logger.info("KPR application history retrieved successfully for user ID: {}", userId);
      return ResponseEntity.ok(
          new ApiResponse<>(true, "KPR application history retrieved successfully", response));

    } catch (Exception e) {
      logger.error("Error retrieving KPR application history: {}", e.getMessage(), e);

      // Determine appropriate HTTP status based on error type
      ApiResponse<List<KprInProgress>> response =
          new ApiResponse<>(
              false, "Failed to get KPR application history: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * Get KPR application approval history for a developer
   *
   * @param authHeader JWT token in Authorization header
   * @return List of KprHistoryListResponse with approval history
   */
  @GetMapping("/developer/history")
  @Operation(
      summary = "Get KPR Application Approval History",
      description =
          "Retrieve the approval history of KPR applications for the authenticated developer. "
              + "Developers can only access their own approval history.")
  @io.swagger.v3.oas.annotations.responses.ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "KPR application approval history retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = KprHistoryListResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Developer does not have access to this history",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json"))
      })
  public ResponseEntity<ApiResponse<List<KprHistoryListResponse>>> getApprovalDeveloper(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    try {
      logger.info("Received request for KPR application approval history");

      // Extract and validate JWT token
      var token = jwtUtil.extractTokenFromHeader(authHeader);

      // Extract developer ID from token
      Integer developerId = jwtUtil.extractUserId(token);
      if (developerId == null) {
        logger.warn("Invalid token - developer ID not found");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse<>(false, "Token tidak valid", null));
      }

      logger.info(
          "Processing KPR application approval history request for developer ID: {}", developerId);

      // Get approval history through service
      List<KprHistoryListResponse> response =
          kprApplicationService.getApprovalDeveloper(developerId);

      logger.info(
          "KPR application approval history retrieved successfully for developer ID: {}",
          developerId);
      return ResponseEntity.ok(
          new ApiResponse<List<KprHistoryListResponse>>(
              true, "KPR application approval history retrieved successfully", response));
    } catch (Exception e) {
      logger.error("Error retrieving KPR application approval history: {}", e.getMessage(), e);

      // Determine appropriate HTTP status based on error type
      ApiResponse<List<KprHistoryListResponse>> response =
          new ApiResponse<>(
              false, "Failed to get KPR application approval history: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * Get KPR applications on progress for a developer
   *
   * @param authHeader JWT token in Authorization header
   * @return List of KprInProgress with applications on progress
   */
  @GetMapping("/developer/progress")
  @Operation(
      summary = "Get KPR Applications On Progress",
      description =
          "Retrieve the KPR applications that are currently in progress for the authenticated developer. "
              + "Developers can only access their own applications on progress.")
  @io.swagger.v3.oas.annotations.responses.ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "KPR applications on progress retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = KprInProgress.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden - Developer does not have access to this progress",
            content = @Content(mediaType = "application/json")),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json"))
      })
  public ResponseEntity<ApiResponse<List<KprInProgress>>> getKprApplicationsOnProgressByDeveloper(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    try {
      logger.info("Received request for KPR applications on progress");

      // Extract and validate JWT token
      var token = jwtUtil.extractTokenFromHeader(authHeader);

      // Extract developer ID from token
      Integer developerId = jwtUtil.extractUserId(token);
      if (developerId == null) {
        logger.warn("Invalid token - developer ID not found");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse<>(false, "Token tidak valid", null));
      }

      logger.info(
          "Processing KPR applications on progress request for developer ID: {}", developerId);

      // Get KPR applications on progress through service
      List<KprInProgress> response =
          kprApplicationService.getKprApplicationsOnProgressByDeveloper(developerId);

      logger.info(
          "KPR applications on progress retrieved successfully for developer ID: {}", developerId);
      return ResponseEntity.ok(
          new ApiResponse<List<KprInProgress>>(
              true, "KPR applications on progress retrieved successfully", response));
    } catch (Exception e) {
      logger.error("Error retrieving KPR applications on progress: {}", e.getMessage(), e);

      // Determine appropriate HTTP status based on error type
      ApiResponse<List<KprInProgress>> response =
          new ApiResponse<>(
              false, "Failed to get KPR applications on progress: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @GetMapping("/applicant")
  public ResponseEntity<ApiResponse<List<KPRApplicant>>> getAllKprApplications(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    try {
      logger.info("Received request for all KPR applications");
      // Extract and validate JWT token
      var token = jwtUtil.extractTokenFromHeader(authHeader);

      // Extract user ID from token
      Integer userID = jwtUtil.extractUserId(token);
      if (userID == null) {
        logger.warn("Invalid token - user ID not found");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse<>(false, "Token tidak valid", null));
      }

      // Get all KPR applications through service
      List<KPRApplicant> response = kprApplicationService.getAllKprApplications(userID);

      logger.info("All KPR applications retrieved successfully");
      return ResponseEntity.ok(
          new ApiResponse<List<KPRApplicant>>(
              true, "All KPR applications retrieved successfully", response));
    } catch (Exception e) {
      logger.error("Error retrieving all KPR applications: {}", e.getMessage(), e);

      // Determine appropriate HTTP status based on error type
      ApiResponse<List<KPRApplicant>> response =
          new ApiResponse<>(false, "Failed to get all KPR applications: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @PostMapping("/admin/assign")
  public ResponseEntity<ApiResponse<String>> assignApprovalWorkflow(
      @RequestBody AssignWorkflowRequest request,
      @RequestHeader("Authorization") String authHeader) {
    try {
      logger.info("Received request for assigning KPR application");
      var token = jwtUtil.extractTokenFromHeader(authHeader);
      Integer adminId = jwtUtil.extractUserId(token);
      if (adminId == null) {
        logger.warn("Invalid token - admin ID not found");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse<>(false, "Token tidak valid", null));
      }
      kprApplicationService.assignApprovalWorkflow(request, adminId);
      logger.info("KPR application assigned successfully");
      return ResponseEntity.ok(
          new ApiResponse<String>(true, "KPR application assigned successfully", "ASSIGNED"));
    } catch (Exception e) {
      logger.error("Error assigning KPR application: {}", e.getMessage(), e);
      ApiResponse<String> response =
          new ApiResponse<>(false, "Failed to assign KPR application: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @GetMapping("/admin/in-progress")
  public ResponseEntity<ApiResponse<List<KprInProgress>>> getAllKprApplicationsAll(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    try {
      logger.info("Received request for all KPR applications (admin)");

      // Extract and validate JWT token
      var token = jwtUtil.extractTokenFromHeader(authHeader);

      // Extract user ID from token
      Integer userID = jwtUtil.extractUserId(token);
      if (userID == null) {
        logger.warn("Invalid token - user ID not found");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse<>(false, "Token tidak valid", null));
      }

      // Get all KPR applications for admin
      List<KprInProgress> response = kprApplicationService.getAdminInProgressNotAssigned(userID);

      logger.info("All KPR applications retrieved successfully (admin)");
      return ResponseEntity.ok(
          new ApiResponse<List<KprInProgress>>(
              true, "All KPR applications retrieved successfully", response));
    } catch (Exception e) {
      logger.error("Error retrieving all KPR applications: {}", e.getMessage(), e);

      ApiResponse<List<KprInProgress>> response =
          new ApiResponse<>(false, "Failed to get all KPR applications: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @GetMapping("/admin/history")
  public ResponseEntity<ApiResponse<List<KprInProgress>>> getAssignedKprApplicationsHistory(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
    try {
      var token = jwtUtil.extractTokenFromHeader(authHeader);
      Integer userID = jwtUtil.extractUserId(token);
      if (userID == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse<>(false, "Token tidak valid", null));
      }

      List<KprInProgress> response = kprApplicationService.getAdminAssignedHistory(userID);
      return ResponseEntity.ok(
          new ApiResponse<List<KprInProgress>>(true, "All assigned KPR applications", response));
    } catch (Exception e) {
      ApiResponse<List<KprInProgress>> response =
          new ApiResponse<>(
              false, "Failed to get assigned KPR applications: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }
}
