package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.KprApplication;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Comprehensive form request DTO for KPR application submission with multipart/form-data support
 * Handles nested data structures and file uploads for complete loan application
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KprApplicationFormRequest {

  // ========================================
  // MAIN DATA
  // ========================================

  @NotNull(message = "Property ID is required") @Positive(message = "Property ID must be positive") private Integer propertyId;

  @NotNull(message = "KPR Rate ID is required") @Positive(message = "KPR Rate ID must be positive") private Integer kprRateId;

  // ========================================
  // NESTED DATA STRUCTURES
  // ========================================

  @Valid
  @NotNull(message = "Simulation data is required") private SimulationData simulationData;

  @Valid
  @NotNull(message = "Personal data is required") private PersonalData personalData;

  @Valid
  @NotNull(message = "Employment data is required") private EmploymentData employmentData;

  // ========================================
  // APPLICATION PURPOSE
  // ========================================

  @NotNull(message = "Application purpose is required") private KprApplication.ApplicationPurpose purpose =
      KprApplication.ApplicationPurpose.PRIMARY_RESIDENCE;

  // ========================================
  // DOCUMENT UPLOADS
  // ========================================

  private MultipartFile ktpDocument;

  private MultipartFile npwpDocument;

  private MultipartFile salarySlipDocument;

  private MultipartFile otherDocument;

  // ========================================
  // VALIDATION METHODS
  // ========================================

  /** Validate that loan amount matches property value minus down payment */
  public boolean isLoanAmountValid() {
    if (simulationData == null
        || simulationData.getPropertyValue() == null
        || simulationData.getDownPayment() == null
        || simulationData.getLoanAmount() == null) {
      return false;
    }

    return simulationData
            .getPropertyValue()
            .subtract(simulationData.getDownPayment())
            .compareTo(simulationData.getLoanAmount())
        == 0;
  }

  /** Validate that down payment is at least minimum percentage of property value */
  public boolean isDownPaymentValid(double minDownPaymentPercent) {
    if (simulationData == null
        || simulationData.getPropertyValue() == null
        || simulationData.getDownPayment() == null) {
      return false;
    }

    var minDownPayment =
        simulationData
            .getPropertyValue()
            .multiply(java.math.BigDecimal.valueOf(minDownPaymentPercent / 100));

    return simulationData.getDownPayment().compareTo(minDownPayment) >= 0;
  }

  /** Check if required documents are provided */
  public boolean hasRequiredDocuments() {
    return (ktpDocument != null && !ktpDocument.isEmpty())
        || (salarySlipDocument != null && !salarySlipDocument.isEmpty())
        || (npwpDocument != null && !npwpDocument.isEmpty())
        || (otherDocument != null && !otherDocument.isEmpty());
  }

  /** Get all uploaded documents as array for processing */
  public MultipartFile[] getAllDocuments() {
    return new MultipartFile[] {ktpDocument, npwpDocument, salarySlipDocument, otherDocument};
  }

  public void validate() {}

  /** Get document names for logging and processing */
  public String[] getDocumentNames() {
    return new String[] {"KTP", "NPWP", "Salary Slip", "Other"};
  }
}
