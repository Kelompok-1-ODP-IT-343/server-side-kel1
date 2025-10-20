package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.Developer;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Data;

/** DTO for creating a new developer */
@Data
public class CreateDeveloperRequest {

  @NotBlank(message = "Company name is required")
  @Size(max = 255, message = "Company name must not exceed 255 characters")
  private String companyName;

  @NotBlank(message = "Company code is required")
  @Size(max = 20, message = "Company code must not exceed 20 characters")
  private String companyCode;

  @NotBlank(message = "Business license is required")
  @Size(max = 100, message = "Business license must not exceed 100 characters")
  private String businessLicense; // SIUP

  @NotBlank(message = "Developer license is required")
  @Size(max = 100, message = "Developer license must not exceed 100 characters")
  private String developerLicense; // Izin Developer

  // Contact Information
  @NotBlank(message = "Contact person is required")
  @Size(max = 100, message = "Contact person must not exceed 100 characters")
  private String contactPerson;

  @NotBlank(message = "Phone is required")
  @Size(max = 20, message = "Phone must not exceed 20 characters")
  private String phone;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  @Size(max = 100, message = "Email must not exceed 100 characters")
  private String email;

  @Size(max = 255, message = "Website must not exceed 255 characters")
  private String website;

  // Address
  @NotBlank(message = "Address is required")
  private String address;

  @NotBlank(message = "City is required")
  @Size(max = 100, message = "City must not exceed 100 characters")
  private String city;

  @NotBlank(message = "Province is required")
  @Size(max = 100, message = "Province must not exceed 100 characters")
  private String province;

  @NotBlank(message = "Postal code is required")
  @Size(max = 10, message = "Postal code must not exceed 10 characters")
  private String postalCode;

  // Business Details
  @Min(value = 1900, message = "Established year must be after 1900")
  @Max(value = 2100, message = "Established year must be before 2100")
  private Integer establishedYear;

  private String description;

  private Developer.Specialization specialization;

  // Partnership
  private Boolean isPartner = false;

  private Developer.PartnershipLevel partnershipLevel;

  @DecimalMin(value = "0.0000", message = "Commission rate must be non-negative")
  @DecimalMax(value = "1.0000", message = "Commission rate must not exceed 100%")
  private BigDecimal commissionRate = new BigDecimal("0.0250"); // 2.5% default

  private Integer userId;
}
