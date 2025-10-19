package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.Developer;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for developer registration response Contains both user account information and developer
 * profile details
 */
@Schema(description = "Response object containing both user account and developer profile information after successful registration")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDeveloperResponse {

  // User Account Information
  @Schema(description = "User account ID", example = "123")
  private Integer userId;
  
  @Schema(description = "Username", example = "dev_company_001")
  private String username;
  
  @Schema(description = "Email address", example = "admin@devcompany.com")
  private String email;
  
  @Schema(description = "Phone number", example = "+6281234567890")
  private String phone;
  
  @Schema(description = "User role", example = "DEVELOPER")
  private String roleName;
  
  @Schema(description = "User account status", example = "ACTIVE")
  private String userStatus;

  // Developer Profile Information
  @Schema(description = "Developer profile ID", example = "45")
  private Integer developerId;
  
  @Schema(description = "Company name", example = "PT Developer Properti Indonesia")
  private String companyName;
  
  @Schema(description = "Company code", example = "DEV001")
  private String companyCode;
  
  @Schema(description = "Business license", example = "SIUP-123456789")
  private String businessLicense;
  
  @Schema(description = "Developer license", example = "IZIN-DEV-001")
  private String developerLicense;
  
  @Schema(description = "Contact person", example = "John Doe")
  private String contactPerson;
  
  @Schema(description = "Website URL", example = "https://www.devcompany.com")
  private String website;
  
  @Schema(description = "Company address", example = "Jl. Sudirman No. 123, Jakarta Selatan")
  private String address;
  
  @Schema(description = "City", example = "Jakarta")
  private String city;
  
  @Schema(description = "Province", example = "DKI Jakarta")
  private String province;
  
  @Schema(description = "Postal code", example = "12190")
  private String postalCode;
  
  @Schema(description = "Established year", example = "2010")
  private Integer establishedYear;
  
  @Schema(description = "Company description", example = "Leading property developer specializing in residential projects")
  private String description;
  
  @Schema(description = "Company specialization", example = "RESIDENTIAL")
  private Developer.Specialization specialization;
  
  @Schema(description = "Partnership status", example = "true")
  private Boolean isPartner;
  
  @Schema(description = "Partnership level", example = "GOLD")
  private Developer.PartnershipLevel partnershipLevel;
  
  @Schema(description = "Commission rate", example = "0.0300")
  private BigDecimal commissionRate;
  
  @Schema(description = "Developer status", example = "ACTIVE")
  private Developer.DeveloperStatus developerStatus;
  
  @Schema(description = "Verification timestamp", example = "2024-01-20T10:00:00")
  private LocalDateTime verifiedAt;
  
  @Schema(description = "Verified by user ID", example = "1")
  private Integer verifiedBy;
  
  @Schema(description = "Creation timestamp", example = "2024-01-20T10:00:00")
  private LocalDateTime createdAt;
  
  @Schema(description = "Last update timestamp", example = "2024-01-20T10:00:00")
  private LocalDateTime updatedAt;

  // Constructor from User and Developer entities
  public RegisterDeveloperResponse(UserResponse userResponse, DeveloperResponse developerResponse) {
    // User information
    this.userId = userResponse.getId();
    this.username = userResponse.getUsername();
    this.email = userResponse.getEmail();
    this.phone = userResponse.getPhone();
    this.roleName = userResponse.getRoleName();
    this.userStatus = userResponse.getStatus().toString();

    // Developer information
    this.developerId = developerResponse.getId();
    this.companyName = developerResponse.getCompanyName();
    this.companyCode = developerResponse.getCompanyCode();
    this.businessLicense = developerResponse.getBusinessLicense();
    this.developerLicense = developerResponse.getDeveloperLicense();
    this.contactPerson = developerResponse.getContactPerson();
    this.website = developerResponse.getWebsite();
    this.address = developerResponse.getAddress();
    this.city = developerResponse.getCity();
    this.province = developerResponse.getProvince();
    this.postalCode = developerResponse.getPostalCode();
    this.establishedYear = developerResponse.getEstablishedYear();
    this.description = developerResponse.getDescription();
    this.specialization = developerResponse.getSpecialization();
    this.isPartner = developerResponse.getIsPartner();
    this.partnershipLevel = developerResponse.getPartnershipLevel();
    this.commissionRate = developerResponse.getCommissionRate();
    this.developerStatus = developerResponse.getStatus();
    this.verifiedAt = developerResponse.getVerifiedAt();
    this.verifiedBy = developerResponse.getVerifiedBy();
    this.createdAt = developerResponse.getCreatedAt();
    this.updatedAt = developerResponse.getUpdatedAt();
  }
}
