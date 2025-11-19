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
  private String district;
  private String subDistrict;
  private String city;
  private String province;
  private String postalCode;
  private String occupation;
  private String companyName;
  private BigDecimal monthlyIncome;
  private Integer workExperience;

  // Developer indicator
  private boolean developer;

  private String bankAccountNumber;

  /**
   * Convenience constructor used by JPQL projections in UserRepository. This matches the SELECT new
   * com.kelompoksatu.griya.dto.UserResponse(...) argument list and intentionally omits district and
   * subDistrict (left as null).
   */
  public UserResponse(
      Integer id,
      String username,
      String email,
      String phone,
      Integer roleId,
      String roleName,
      UserStatus status,
      boolean emailVerified,
      boolean phoneVerified,
      LocalDateTime lastLoginAt,
      LocalDateTime createdAt,
      LocalDateTime updatedAt,
      String fullName,
      String nik,
      String npwp,
      LocalDate birthDate,
      String birthPlace,
      Gender gender,
      MaritalStatus maritalStatus,
      String address,
      String city,
      String province,
      String postalCode,
      String occupation,
      String companyName,
      java.math.BigDecimal monthlyIncome,
      Integer workExperience,
      boolean developer) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.phone = phone;
    this.roleId = roleId;
    this.roleName = roleName;
    this.status = status;
    this.emailVerified = emailVerified;
    this.phoneVerified = phoneVerified;
    this.lastLoginAt = lastLoginAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.fullName = fullName;
    this.nik = nik;
    this.npwp = npwp;
    this.birthDate = birthDate;
    this.birthPlace = birthPlace;
    this.gender = gender;
    this.maritalStatus = maritalStatus;
    this.address = address;
    // district and subDistrict are intentionally not provided by this projection
    this.district = null;
    this.subDistrict = null;
    this.city = city;
    this.province = province;
    this.postalCode = postalCode;
    this.occupation = occupation;
    this.companyName = companyName;
    this.monthlyIncome = monthlyIncome;
    this.workExperience = workExperience;
    this.developer = developer;
  }
}
