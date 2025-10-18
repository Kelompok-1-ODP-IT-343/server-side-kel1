package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.KprApplication;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for KPR application submission
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KprApplicationRequest {

    @NotNull(message = "Property ID is required")
    @Positive(message = "Property ID must be positive")
    private Integer propertyId;

    @NotNull(message = "Down payment is required")
    @DecimalMin(value = "0.01", message = "Down payment must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Down payment format is invalid")
    private BigDecimal downPayment;

    @NotNull(message = "Loan term is required")
    @Min(value = 1, message = "Loan term must be at least 1 year")
    @Max(value = 30, message = "Loan term cannot exceed 30 years")
    private Integer loanTermYears;

    @NotNull(message = "Purpose is required")
    private KprApplication.ApplicationPurpose purpose;
}
