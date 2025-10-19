package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.ApiResponse;
import com.kelompoksatu.griya.dto.CreateDeveloperRequest;
import com.kelompoksatu.griya.dto.DeveloperResponse;
import com.kelompoksatu.griya.entity.Developer;
import com.kelompoksatu.griya.service.DeveloperService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** REST Controller for Developer operations */
@RestController
@RequestMapping("/api/developers")
@Validated
public class DeveloperController {

  private final DeveloperService developerService;

  @Autowired
  public DeveloperController(DeveloperService developerService) {
    this.developerService = developerService;
  }

  /** Create a new developer */
  @PostMapping
  public ResponseEntity<ApiResponse<DeveloperResponse>> createDeveloper(
      @Valid @RequestBody CreateDeveloperRequest request) {
    try {
      DeveloperResponse developer = developerService.createDeveloper(request);
      ApiResponse<DeveloperResponse> response =
          new ApiResponse<>(true, "Developer created successfully", developer);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (IllegalArgumentException e) {
      ApiResponse<DeveloperResponse> response = new ApiResponse<>(false, e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (Exception e) {
      ApiResponse<DeveloperResponse> response =
          new ApiResponse<>(false, "Failed to create developer: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get all developers */
  @GetMapping
  public ResponseEntity<ApiResponse<List<DeveloperResponse>>> getAllDevelopers() {
    try {
      List<DeveloperResponse> developers = developerService.getAllDevelopers();
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(true, "Developers retrieved successfully", developers);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(false, "Failed to retrieve developers: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get developer by ID */
  @GetMapping("/{id}")
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
          new ApiResponse<>(false, "Failed to retrieve developer: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get developer by company code */
  @GetMapping("/company-code/{companyCode}")
  public ResponseEntity<ApiResponse<DeveloperResponse>> getDeveloperByCompanyCode(
      @PathVariable String companyCode) {
    try {
      Optional<DeveloperResponse> developer =
          developerService.getDeveloperByCompanyCode(companyCode);
      if (developer.isPresent()) {
        ApiResponse<DeveloperResponse> response =
            new ApiResponse<>(true, "Developer retrieved successfully", developer.get());
        return ResponseEntity.ok(response);
      } else {
        ApiResponse<DeveloperResponse> response =
            new ApiResponse<>(false, "Developer not found with company code: " + companyCode, null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }
    } catch (Exception e) {
      ApiResponse<DeveloperResponse> response =
          new ApiResponse<>(false, "Failed to retrieve developer: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get active developers */
  @GetMapping("/active")
  public ResponseEntity<ApiResponse<List<DeveloperResponse>>> getActiveDevelopers() {
    try {
      List<DeveloperResponse> developers = developerService.getActiveDevelopers();
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(true, "Active developers retrieved successfully", developers);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(false, "Failed to retrieve active developers: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get developers by status */
  @GetMapping("/status/{status}")
  public ResponseEntity<ApiResponse<List<DeveloperResponse>>> getDevelopersByStatus(
      @PathVariable String status) {
    try {
      Developer.DeveloperStatus developerStatus =
          Developer.DeveloperStatus.valueOf(status.toUpperCase());
      List<DeveloperResponse> developers = developerService.getDevelopersByStatus(developerStatus);
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(
              true, "Developers with status " + status + " retrieved successfully", developers);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(false, "Invalid status: " + status, null);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (Exception e) {
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(false, "Failed to retrieve developers: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get partner developers */
  @GetMapping("/partners")
  public ResponseEntity<ApiResponse<List<DeveloperResponse>>> getPartnerDevelopers() {
    try {
      List<DeveloperResponse> developers = developerService.getPartnerDevelopers();
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(true, "Partner developers retrieved successfully", developers);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(
              false, "Failed to retrieve partner developers: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get developers by specialization */
  @GetMapping("/specialization/{specialization}")
  public ResponseEntity<ApiResponse<List<DeveloperResponse>>> getDevelopersBySpecialization(
      @PathVariable String specialization) {
    try {
      Developer.Specialization spec =
          Developer.Specialization.valueOf(specialization.toUpperCase());
      List<DeveloperResponse> developers = developerService.getDevelopersBySpecialization(spec);
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(
              true,
              "Developers with specialization " + specialization + " retrieved successfully",
              developers);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(false, "Invalid specialization: " + specialization, null);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (Exception e) {
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(false, "Failed to retrieve developers: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get developers by city */
  @GetMapping("/city/{city}")
  public ResponseEntity<ApiResponse<List<DeveloperResponse>>> getDevelopersByCity(
      @PathVariable String city) {
    try {
      List<DeveloperResponse> developers = developerService.getDevelopersByCity(city);
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(true, "Developers in " + city + " retrieved successfully", developers);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(false, "Failed to retrieve developers: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get developers by province */
  @GetMapping("/province/{province}")
  public ResponseEntity<ApiResponse<List<DeveloperResponse>>> getDevelopersByProvince(
      @PathVariable String province) {
    try {
      List<DeveloperResponse> developers = developerService.getDevelopersByProvince(province);
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(
              true, "Developers in " + province + " retrieved successfully", developers);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(false, "Failed to retrieve developers: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Search developers by company name */
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<DeveloperResponse>>> searchDevelopers(
      @RequestParam String companyName) {
    try {
      List<DeveloperResponse> developers =
          developerService.searchDevelopersByCompanyName(companyName);
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(
              true, "Search results for '" + companyName + "' retrieved successfully", developers);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<DeveloperResponse>> response =
          new ApiResponse<>(false, "Failed to search developers: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Update developer status */
  @PutMapping("/{id}/status")
  public ResponseEntity<ApiResponse<DeveloperResponse>> updateDeveloperStatus(
      @PathVariable Integer id, @RequestParam String status) {
    try {
      Developer.DeveloperStatus developerStatus =
          Developer.DeveloperStatus.valueOf(status.toUpperCase());
      DeveloperResponse developer = developerService.updateDeveloperStatus(id, developerStatus);
      ApiResponse<DeveloperResponse> response =
          new ApiResponse<>(true, "Developer status updated successfully", developer);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      ApiResponse<DeveloperResponse> response = new ApiResponse<>(false, e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (Exception e) {
      ApiResponse<DeveloperResponse> response =
          new ApiResponse<>(false, "Failed to update developer status: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Verify developer */
  @PutMapping("/{id}/verify")
  public ResponseEntity<ApiResponse<DeveloperResponse>> verifyDeveloper(
      @PathVariable Integer id, @RequestParam Integer verifiedBy) {
    try {
      DeveloperResponse developer = developerService.verifyDeveloper(id, verifiedBy);
      ApiResponse<DeveloperResponse> response =
          new ApiResponse<>(true, "Developer verified successfully", developer);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      ApiResponse<DeveloperResponse> response = new ApiResponse<>(false, e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (Exception e) {
      ApiResponse<DeveloperResponse> response =
          new ApiResponse<>(false, "Failed to verify developer: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Update partnership status */
  @PutMapping("/{id}/partnership")
  public ResponseEntity<ApiResponse<DeveloperResponse>> updatePartnershipStatus(
      @PathVariable Integer id,
      @RequestParam Boolean isPartner,
      @RequestParam(required = false) String partnershipLevel) {
    try {
      Developer.PartnershipLevel level = null;
      if (partnershipLevel != null) {
        level = Developer.PartnershipLevel.valueOf(partnershipLevel.toUpperCase());
      }
      DeveloperResponse developer = developerService.updatePartnershipStatus(id, isPartner, level);
      ApiResponse<DeveloperResponse> response =
          new ApiResponse<>(true, "Developer partnership status updated successfully", developer);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      ApiResponse<DeveloperResponse> response = new ApiResponse<>(false, e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (Exception e) {
      ApiResponse<DeveloperResponse> response =
          new ApiResponse<>(false, "Failed to update partnership status: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Delete developer */
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteDeveloper(@PathVariable Integer id) {
    try {
      developerService.deleteDeveloper(id);
      ApiResponse<Void> response = new ApiResponse<>(true, "Developer deleted successfully", null);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      ApiResponse<Void> response = new ApiResponse<>(false, e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    } catch (Exception e) {
      ApiResponse<Void> response =
          new ApiResponse<>(false, "Failed to delete developer: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Check if company code exists */
  @GetMapping("/exists/company-code/{companyCode}")
  public ResponseEntity<ApiResponse<Boolean>> checkCompanyCodeExists(
      @PathVariable String companyCode) {
    try {
      boolean exists = developerService.existsByCompanyCode(companyCode);
      ApiResponse<Boolean> response =
          new ApiResponse<>(true, "Company code existence check completed", exists);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<Boolean> response =
          new ApiResponse<>(
              false, "Failed to check company code existence: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Check if email exists */
  @GetMapping("/exists/email/{email}")
  public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@PathVariable String email) {
    try {
      boolean exists = developerService.existsByEmail(email);
      ApiResponse<Boolean> response =
          new ApiResponse<>(true, "Email existence check completed", exists);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<Boolean> response =
          new ApiResponse<>(false, "Failed to check email existence: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }
}
