package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.Gender;
import com.kelompoksatu.griya.entity.MaritalStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for comprehensive user registration request
 * Includes both user account and profile information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    // Basic account information
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
             message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+62|62|0)[0-9]{9,13}$", message = "Please provide a valid Indonesian phone number")
    private String phone;

    // User profile information
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotBlank(message = "NIK is required")
    @Pattern(regexp = "^[0-9]{16}$", message = "NIK must be exactly 16 digits")
    private String nik;

    @Pattern(regexp = "^[0-9]{15}$", message = "NPWP must be exactly 15 digits")
    private String npwp;

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @NotBlank(message = "Birth place is required")
    @Size(max = 100, message = "Birth place must not exceed 100 characters")
    private String birthPlace;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Marital status is required")
    private MaritalStatus maritalStatus;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "Province is required")
    @Size(max = 100, message = "Province must not exceed 100 characters")
    private String province;

    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "^[0-9]{5}$", message = "Postal code must be exactly 5 digits")
    private String postalCode;

    @NotBlank(message = "Occupation is required")
    @Size(max = 100, message = "Occupation must not exceed 100 characters")
    private String occupation;

    @Size(max = 100, message = "Company name must not exceed 100 characters")
    private String companyName;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Monthly income must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Monthly income format is invalid")
    private BigDecimal monthlyIncome;

    @Min(value = 0, message = "Work experience cannot be negative")
    @Max(value = 50, message = "Work experience cannot exceed 50 years")
    private Integer workExperience;

    private String role;

    /**
     * Utility method to check if passwords match
     */
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}
