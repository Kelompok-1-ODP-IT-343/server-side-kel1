package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.Developer;
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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDeveloperResponse {

  // User Account Information
  private Integer userId;
  private String username;
  private String email;
  private String phone;
  private String roleName;
  private String userStatus;

  // Developer Profile Information
  private Integer developerId;
  private String companyName;
  private String companyCode;
  private String businessLicense;
  private String developerLicense;
  private String contactPerson;
  private String website;
  private String address;
  private String city;
  private String province;
  private String postalCode;
  private Integer establishedYear;
  private String description;
  private Developer.Specialization specialization;
  private Boolean isPartner;
  private Developer.PartnershipLevel partnershipLevel;
  private BigDecimal commissionRate;
  private Developer.DeveloperStatus developerStatus;
  private LocalDateTime verifiedAt;
  private Integer verifiedBy;
  private LocalDateTime createdAt;
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
