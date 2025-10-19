package com.kelompoksatu.griya.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** KPR Application entity representing home loan applications */
@Entity
@Table(name = "kpr_applications")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KprApplication {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "application_number", length = 20, unique = true, nullable = false)
  private String applicationNumber; // Format: KPR-YYYY-XXXXXX

  @Column(name = "user_id", nullable = false)
  private Integer userId;

  @Column(name = "property_id", nullable = false)
  private Integer propertyId;

  @Column(name = "kpr_rate_id", nullable = false)
  private Integer kprRateId;

  @Enumerated(EnumType.STRING)
  @Column(name = "property_type", nullable = false)
  private Property.PropertyType propertyType;

  @Column(name = "property_value", precision = 15, scale = 2, nullable = false)
  private BigDecimal propertyValue;

  @Column(name = "loan_amount", precision = 15, scale = 2, nullable = false)
  private BigDecimal loanAmount;

  @Column(name = "loan_term_years", nullable = false)
  private Integer loanTermYears;

  @Column(name = "interest_rate", precision = 5, scale = 4, nullable = false)
  private BigDecimal interestRate;

  @Column(name = "monthly_installment", precision = 15, scale = 2, nullable = false)
  private BigDecimal monthlyInstallment;

  @Column(name = "down_payment", precision = 15, scale = 2, nullable = false)
  private BigDecimal downPayment;

  @Column(name = "property_address", columnDefinition = "TEXT", nullable = false)
  private String propertyAddress;

  @Enumerated(EnumType.STRING)
  @Column(name = "property_certificate_type", nullable = false)
  private Property.CertificateType propertyCertificateType;

  @Column(name = "developer_name", length = 255)
  private String developerName;

  @Enumerated(EnumType.STRING)
  @Column(name = "purpose", nullable = false)
  private ApplicationPurpose purpose;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ApplicationStatus status = ApplicationStatus.SUBMITTED;

  @Column(name = "current_approval_level")
  private Integer currentApprovalLevel;

  @Column(name = "submitted_at", nullable = false)
  private LocalDateTime submittedAt;

  @Column(name = "approved_at")
  private LocalDateTime approvedAt;

  @Column(name = "rejected_at")
  private LocalDateTime rejectedAt;

  @Column(name = "rejection_reason", columnDefinition = "TEXT")
  private String rejectionReason;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // Relationships
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "property_id", insertable = false, updatable = false)
  private Property property;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "kpr_rate_id", insertable = false, updatable = false)
  private KprRate kprRate;

  // Enums
  public enum ApplicationPurpose {
    PRIMARY_RESIDENCE,
    INVESTMENT,
    BUSINESS
  }

  public enum ApplicationStatus {
    SUBMITTED,
    DOCUMENT_VERIFICATION,
    PROPERTY_APPRAISAL,
    CREDIT_ANALYSIS,
    APPROVAL_PENDING,
    APPROVED,
    REJECTED,
    CANCELLED,
    DISBURSED
  }
}
