package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.Gender;
import com.kelompoksatu.griya.entity.MaritalStatus;
import com.kelompoksatu.griya.entity.UserStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

  private Integer id;
  private String username;
  private String email;
  private String phone;
  private Integer roleId;
  private String roleName;
  private UserStatus status;
  private boolean emailVerified;
  private boolean phoneVerified;
  private LocalDateTime lastLoginAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // User Profile fields (only for regular users, not developers)
  private String fullName;
  private String nik;
  private String npwp;
  private LocalDate birthDate;
  private String birthPlace;
  private Gender gender;
  private MaritalStatus maritalStatus;
  private String address;
  private String city;
  private String province;
  private String postalCode;
  private String occupation;
  private String companyName;
  private BigDecimal monthlyIncome;
  private Integer workExperience;

  // Developer indicator
  private boolean developer;
}
