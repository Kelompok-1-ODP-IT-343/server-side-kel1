package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.ApplicationDocument;
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
 * Response DTO for KPR Application Detail Contains comprehensive application information and
 * associated documents
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
  private Integer currentApprovalLevel;

  // Timestamps
  private LocalDateTime submittedAt;
  private LocalDateTime approvedAt;
  private LocalDateTime rejectedAt;
  private String rejectionReason;
  private String notes;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // Associated Documents
  private List<DocumentInfo> documents;

  /** Document information DTO */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DocumentInfo {
    private Integer documentId;
    private ApplicationDocument.DocumentType documentType;
    private String documentName;
    private String filePath;
    private Integer fileSize;
    private String mimeType;
    private Boolean isVerified;
    private Integer verifiedBy;
    private LocalDateTime verifiedAt;
    private String verificationNotes;
    private LocalDateTime uploadedAt;
  }
}
