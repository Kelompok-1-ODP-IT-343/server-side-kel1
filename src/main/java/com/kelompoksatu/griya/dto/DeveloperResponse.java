package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.Developer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/** DTO for developer response */
@Setter
@Getter
public class DeveloperResponse {

  // Getters and Setters
  private Integer id;
  private String companyName;
  private String companyCode;
  private String businessLicense;
  private String developerLicense;

  // Contact Information
  private String contactPerson;
  private String phone;
  private String email;
  private String website;

  // Address
  private String address;
  private String city;
  private String province;
  private String postalCode;

  // Business Details
  private Integer establishedYear;
  private String description;
  private Developer.Specialization specialization;

  // Partnership
  private Boolean isPartner;
  private Developer.PartnershipLevel partnershipLevel;
  private BigDecimal commissionRate;

  // Status
  private Developer.DeveloperStatus status;
  private LocalDateTime verifiedAt;
  private Integer verifiedBy;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // Default constructor
  public DeveloperResponse() {}

  // Constructor from Developer entity
  public DeveloperResponse(Developer developer) {
    this.id = developer.getId();
    this.companyName = developer.getCompanyName();
    this.companyCode = developer.getCompanyCode();
    this.businessLicense = developer.getBusinessLicense();
    this.developerLicense = developer.getDeveloperLicense();
    this.contactPerson = developer.getContactPerson();
    this.phone = developer.getPhone();
    this.email = developer.getEmail();
    this.website = developer.getWebsite();
    this.address = developer.getAddress();
    this.city = developer.getCity();
    this.province = developer.getProvince();
    this.postalCode = developer.getPostalCode();
    this.establishedYear = developer.getEstablishedYear();
    this.description = developer.getDescription();
    this.specialization = developer.getSpecialization();
    this.isPartner = developer.getIsPartner();
    this.partnershipLevel = developer.getPartnershipLevel();
    this.commissionRate = developer.getCommissionRate();
    this.status = developer.getStatus();
    this.verifiedAt = developer.getVerifiedAt();
    this.verifiedBy = developer.getVerifiedBy();
    this.createdAt = developer.getCreatedAt();
    this.updatedAt = developer.getUpdatedAt();
  }
}
