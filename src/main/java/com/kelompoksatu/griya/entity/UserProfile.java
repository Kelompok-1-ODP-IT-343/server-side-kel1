package com.kelompoksatu.griya.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

/** Extended user profile with KYC (Know Your Customer) data */
@Entity
@Table(
    name = "user_profiles",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = "nik"),
      @UniqueConstraint(columnNames = "npwp")
    })
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "user_id", unique = true, nullable = false)
  private Integer userId;

  @Column(name = "full_name", length = 100, nullable = false)
  private String fullName;

  @Column(name = "nik", length = 16, unique = true, nullable = true)
  private String nik; // NIK (Nomor Induk Kependudukan)

  @Column(name = "npwp", length = 16, unique = true)
  private String npwp; // NPWP (Nomor Pokok Wajib Pajak)

  @Column(name = "birth_date", nullable = false)
  private LocalDate birthDate;

  @Column(name = "birth_place", length = 100, nullable = false)
  private String birthPlace;

  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  @Column(name = "gender", columnDefinition = "gender")
  private Gender gender;

  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  @Column(name = "marital_status", columnDefinition = "marital_status")
  private MaritalStatus maritalStatus;

  @Column(name = "address", columnDefinition = "TEXT")
  private String address;

  @Column(name = "city", length = 100)
  private String city;

  @Column(name = "province", length = 100)
  private String province;

  @Column(name = "postal_code", length = 10)
  private String postalCode;

  @Column(name = "occupation", length = 100)
  private String occupation;

  @Column(name = "company_name", length = 100)
  private String companyName;

  @Column(name = "monthly_income", precision = 15, scale = 2, nullable = false)
  private BigDecimal monthlyIncome;

  @Column(name = "work_experience")
  private Integer workExperience;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
