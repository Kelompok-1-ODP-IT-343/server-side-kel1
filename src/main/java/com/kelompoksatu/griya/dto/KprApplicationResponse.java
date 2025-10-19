package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.KprApplication;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for successful KPR application submission */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KprApplicationResponse {

  private Integer applicationId;
  private String applicationNumber;
  private KprApplication.ApplicationStatus status;
  private BigDecimal monthlyInstallment;
  private BigDecimal interestRate;
  private String message;

  public KprApplicationResponse(
      Integer applicationId,
      String applicationNumber,
      KprApplication.ApplicationStatus status,
      BigDecimal monthlyInstallment,
      BigDecimal interestRate) {
    this.applicationId = applicationId;
    this.applicationNumber = applicationNumber;
    this.status = status;
    this.monthlyInstallment = monthlyInstallment;
    this.interestRate = interestRate;
    this.message = "Application submitted successfully. Please upload the required documents.";
  }
}
