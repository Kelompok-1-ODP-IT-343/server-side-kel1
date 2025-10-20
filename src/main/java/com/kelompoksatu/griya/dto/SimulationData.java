package com.kelompoksatu.griya.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for loan simulation data in KPR application Contains property value, down payment, loan
 * amount, and loan term
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimulationData {

  @NotNull(message = "Property value is required") @DecimalMin(value = "1.00", message = "Property value must be greater than 0")
  @Digits(integer = 15, fraction = 2, message = "Property value format is invalid")
  private BigDecimal propertyValue;

  @NotNull(message = "Down payment is required") @DecimalMin(value = "1.00", message = "Down payment must be greater than 0")
  @Digits(integer = 15, fraction = 2, message = "Down payment format is invalid")
  private BigDecimal downPayment;

  @NotNull(message = "Loan amount is required") @DecimalMin(value = "1.00", message = "Loan amount must be greater than 0")
  @Digits(integer = 15, fraction = 2, message = "Loan amount format is invalid")
  private BigDecimal loanAmount;

  @NotNull(message = "Loan term is required") @Min(value = 1, message = "Loan term must be at least 1 year")
  @Max(value = 30, message = "Loan term cannot exceed 30 years")
  private Integer loanTermYears;
}
