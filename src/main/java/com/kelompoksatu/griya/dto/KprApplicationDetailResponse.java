package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.ApplicationDocument;
import com.kelompoksatu.griya.entity.ApprovalWorkflow;
import com.kelompoksatu.griya.entity.KprApplication;
import com.kelompoksatu.griya.entity.Property;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Comprehensive Response DTO for KPR Application Detail Contains all application information and
 * associated entities
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KprApplicationDetailResponse {

  // Basic Application Info
  private Integer applicationId;
  private String applicationNumber;
  private Integer userId;
  private Integer propertyId;
  private Integer kprRateId;

  // Property Information
  private Property.PropertyType propertyType;
  private BigDecimal propertyValue;
  private String propertyAddress;
  private Property.CertificateType propertyCertificateType;
  private String developerName;

  // Loan Details
  private BigDecimal loanAmount;
  private Integer loanTermYears;
  private BigDecimal interestRate;
  private BigDecimal monthlyInstallment;
  private BigDecimal downPayment;
  private BigDecimal ltvRatio;

  // Application Status
  private KprApplication.ApplicationPurpose purpose;
  private KprApplication.ApplicationStatus status;

  // Timestamps
  private LocalDateTime submittedAt;
  private LocalDateTime approvedAt;
  private LocalDateTime rejectedAt;
  private String rejectionReason;
  private String notes;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // User Information
  private UserInfo userInfo;

  // Property Details
  private PropertyInfo propertyInfo;

  // Developer Information
  private DeveloperInfo developerInfo;

  // KPR Rate Information
  private KprRateInfo kprRateInfo;

  // Associated Documents
  private List<DocumentInfo> documents;

  // Approval Workflows
  private List<ApprovalWorkflowInfo> approvalWorkflows;

  /** User information DTO */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserInfo {
    private Integer userId;
    private String username;
    private String email;
    private String phone;
    private String roleName;
    private String fullName;
    private String nik;
    private String npwp;
    private String birthDate;
    private String birthPlace;
    private String gender;
    private String maritalStatus;
    private String address;
    private String city;
    private String province;
    private String postalCode;
    private String occupation;
    private String companyName;
    private String companyAddress;
    private BigDecimal monthlyIncome;
  }

  /** Property information DTO */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PropertyInfo {
    private Integer propertyId;
    private String propertyCode;
    private String title;
    private String description;
    private String address;
    private String city;
    private String province;
    private String postalCode;
    private String district;
    private String village;
    private BigDecimal landArea;
    private BigDecimal buildingArea;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer floors;
    private Integer garage;
    private Integer yearBuilt;
    private BigDecimal price;
    private BigDecimal pricePerSqm;
    private Property.CertificateType certificateType;
    private String certificateNumber;
    private BigDecimal pbbValue;
    private Property.PropertyStatus status;
    private Boolean isKprEligible;
    private BigDecimal minDownPaymentPercent;
    private Integer maxLoanTermYears;
  }

  /** Developer information DTO */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DeveloperInfo {
    private Integer developerId;
    private String companyName;
    private String companyCode;
    private String businessLicense;
    private String developerLicense;
    private String contactPerson;
    private String phone;
    private String email;
    private String website;
    private String address;
    private String city;
    private String province;
    private String postalCode;
    private Integer establishedYear;
    private String description;
    private String specialization;
    private Boolean isPartner;
    private String partnershipLevel;
    private BigDecimal commissionRate;
    private String status;
    private LocalDateTime verifiedAt;
    private Integer verifiedBy;
  }

  /** KPR Rate information DTO */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class KprRateInfo {
    private Integer kprRateId;
    private String rateName;
    private String rateType;
    private String propertyType;
    private String customerSegment;
    private BigDecimal baseRate;
    private BigDecimal margin;
    private BigDecimal effectiveRate;
    private BigDecimal minLoanAmount;
    private BigDecimal maxLoanAmount;
    private Integer minTermYears;
    private Integer maxTermYears;
    private BigDecimal maxLtvRatio;
    private BigDecimal minIncome;
    private Integer maxAge;
    private BigDecimal minDownPaymentPercent;
    private BigDecimal adminFee;
    private BigDecimal adminFeePercent;
    private BigDecimal appraisalFee;
    private BigDecimal insuranceRate;
    private BigDecimal notaryFeePercent;
    private Boolean isPromotional;
    private String promoDescription;
    private String promoStartDate;
    private String promoEndDate;
  }

  /** Document information DTO */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DocumentInfo {
    private Integer documentId;
    private ApplicationDocument.DocumentType documentType;
    private String documentName;
    private String originalFilename;
    private String filePath;
    private Integer fileSize;
    private String mimeType;
    private Boolean isVerified;
    private Integer verifiedBy;
    private String verifiedByName;
    private LocalDateTime verifiedAt;
    private String verificationNotes;
    private LocalDateTime uploadedAt;
  }

  /** Approval Workflow information DTO */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ApprovalWorkflowInfo {
    private Integer workflowId;
    private Integer applicationId;
    private ApprovalWorkflow.WorkflowStage stage;
    private Integer assignedTo;
    private String assignedToName;
    private String assignedToEmail;
    private String assignedToRole;
    private ApprovalWorkflow.WorkflowStatus status;
    private ApprovalWorkflow.PriorityLevel priority;
    private LocalDateTime dueDate;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String approvalNotes;
    private String rejectionReason;
    private Integer escalatedTo;
    private String escalatedToName;
    private LocalDateTime escalatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
  }
}
