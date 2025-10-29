package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.Gender;
import com.kelompoksatu.griya.entity.MaritalStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for personal data in KPR application Contains personal information required for loan
 * application
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalData {

  @NotBlank(message = "Full name is required")
  @Size(max = 100, message = "Full name cannot exceed 100 characters")
  private String fullName;

  @NotBlank(message = "NIK is required")
  @Pattern(regexp = "^[0-9]{16}$", message = "NIK must be exactly 16 digits")
  private String nik;

  @Pattern(regexp = "^[0-9]{15}$", message = "NPWP must be exactly 15 digits")
  private String npwp;

  @NotBlank(message = "Birth date is required")
  @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Birth date must be in YYYY-MM-DD format")
  private LocalDate birthDate;

  @NotBlank(message = "Birth place is required")
  @Size(max = 100, message = "Birth place cannot exceed 100 characters")
  private String birthPlace;

  @NotBlank(message = "Gender is required")
  @Pattern(regexp = "^(male|female)$", message = "Gender must be 'male' or 'female'")
  private String gender;

  @NotBlank(message = "Marital status is required")
  @Pattern(
      regexp = "^(single|married|divorced|widowed)$",
      message = "Marital status must be one of: single, married, divorced, widowed")
  private String maritalStatus;

  @NotBlank(message = "Address is required")
  @Size(max = 500, message = "Address cannot exceed 500 characters")
  private String address;

  @NotBlank(message = "City is required")
  @Size(max = 100, message = "City cannot exceed 100 characters")
  private String city;

  @NotBlank(message = "Province is required")
  @Size(max = 100, message = "Province cannot exceed 100 characters")
  private String province;

  @NotBlank(message = "Postal code is required")
  @Pattern(regexp = "^[0-9]{5}$", message = "Postal code must be exactly 5 digits")
  private String postalCode;

  /** Convert string gender to Gender enum */
  public Gender getGenderEnum() {
    return "male".equalsIgnoreCase(gender) ? Gender.MALE : Gender.FEMALE;
  }

  /** Convert string marital status to MaritalStatus enum */
  public MaritalStatus getMaritalStatusEnum() {
    return switch (maritalStatus.toLowerCase()) {
      case "single" -> MaritalStatus.SINGLE;
      case "married" -> MaritalStatus.MARRIED;
      case "divorced" -> MaritalStatus.DIVORCED;
      case "widowed" -> MaritalStatus.WIDOWED;
      default -> throw new IllegalArgumentException("Invalid marital status: " + maritalStatus);
    };
  }
}
