package com.kelompoksatu.griya.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Developer entity representing property developers and real estate companies */
@Entity
@Table(name = "developers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Developer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "company_name", length = 255, nullable = false)
  private String companyName;

  @Column(name = "company_code", length = 20, unique = true, nullable = false)
  private String companyCode;

  @Column(name = "business_license", length = 100, nullable = false)
  private String businessLicense; // SIUP

  @Column(name = "developer_license", length = 100, nullable = false)
  private String developerLicense; // Izin Developer

  // Contact Information
  @Column(name = "contact_person", length = 100, nullable = false)
  private String contactPerson;

  @Column(name = "phone", length = 20, nullable = false)
  private String phone;

  @Column(name = "email", length = 100, nullable = false)
  private String email;

  @Column(name = "website", length = 255)
  private String website;

  // Address
  @Column(name = "address", columnDefinition = "TEXT", nullable = false)
  private String address;

  @Column(name = "city", length = 100, nullable = false)
  private String city;

  @Column(name = "province", length = 100, nullable = false)
  private String province;

  @Column(name = "postal_code", length = 10, nullable = false)
  private String postalCode;

  // Business Details
  @Column(name = "established_year")
  private Integer establishedYear;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "specialization")
  private Specialization specialization;

  // Partnership
  @Column(name = "is_partner", nullable = false)
  private Boolean isPartner = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "partnership_level")
  private PartnershipLevel partnershipLevel;

  @Column(name = "commission_rate", precision = 5, scale = 4)
  private BigDecimal commissionRate = new BigDecimal("0.0250"); // 2.5% default

  // Status
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private DeveloperStatus status = DeveloperStatus.ACTIVE;

  @Column(name = "verified_at")
  private LocalDateTime verifiedAt;

  @Column(name = "verified_by")
  private Integer verifiedBy;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // Enums
  public enum Specialization {
    RESIDENTIAL,
    COMMERCIAL,
    MIXED,
    INDUSTRIAL
  }

  public enum PartnershipLevel {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM
  }

  public enum DeveloperStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
  }
}
