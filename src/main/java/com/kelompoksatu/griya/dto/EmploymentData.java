package com.kelompoksatu.griya.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for employment data in KPR application Contains employment and company information required
 * for loan application
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmploymentData {

  @NotBlank(message = "Occupation is required")
  @Size(max = 100, message = "Occupation cannot exceed 100 characters")
  private String occupation;

  @NotNull(message = "Monthly income is required") @DecimalMin(value = "1.00", message = "Monthly income must be greater than 0")
  @Digits(integer = 15, fraction = 2, message = "Monthly income format is invalid")
  private BigDecimal monthlyIncome;

  @NotBlank(message = "Company name is required")
  @Size(max = 100, message = "Company name cannot exceed 100 characters")
  private String companyName;

  @NotBlank(message = "Company address is required")
  @Size(max = 255, message = "Company address cannot exceed 255 characters")
  private String companyAddress;

  @NotBlank(message = "Company city is required")
  @Size(max = 100, message = "Company city cannot exceed 100 characters")
  private String companyCity;

  @NotBlank(message = "Company province is required")
  @Size(max = 100, message = "Company province cannot exceed 100 characters")
  private String companyProvince;

  @NotBlank(message = "Company postal code is required")
  @Pattern(regexp = "^[0-9]{5}$", message = "Company postal code must be exactly 5 digits")
  private String companyPostalCode;
}
