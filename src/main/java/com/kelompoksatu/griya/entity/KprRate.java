package com.kelompoksatu.griya.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** KPR interest rates and loan terms configuration */
@Entity
@Table(name = "kpr_rates")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KprRate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "rate_name", length = 100, nullable = false)
  private String rateName;

  @Enumerated(EnumType.STRING)
  @Column(name = "rate_type", nullable = false)
  private RateType rateType;

  @Enumerated(EnumType.STRING)
  @Column(name = "property_type", nullable = false)
  private PropertyTypeFilter propertyType;

  @Enumerated(EnumType.STRING)
  @Column(name = "customer_segment", nullable = false)
  private CustomerSegment customerSegment;

  // Rate Details
  @Column(name = "base_rate", precision = 5, scale = 4, nullable = false)
  private BigDecimal baseRate; // Base interest rate

  @Column(name = "margin", precision = 5, scale = 4, nullable = false)
  private BigDecimal margin; // Bank margin

  @Column(name = "effective_rate", precision = 5, scale = 4, nullable = false)
  private BigDecimal effectiveRate; // Final rate = base + margin

  // Loan Terms
  @Column(name = "min_loan_amount", precision = 15, scale = 2, nullable = false)
  private BigDecimal minLoanAmount;

  @Column(name = "max_loan_amount", precision = 15, scale = 2, nullable = false)
  private BigDecimal maxLoanAmount;

  @Column(name = "min_term_years", nullable = false)
  private Integer minTermYears;

  @Column(name = "max_term_years", nullable = false)
  private Integer maxTermYears;

  @Column(name = "max_ltv_ratio", precision = 5, scale = 4, nullable = false)
  private BigDecimal maxLtvRatio; // Loan to Value ratio

  // Eligibility
  @Column(name = "min_income", precision = 15, scale = 2, nullable = false)
  private BigDecimal minIncome;

  @Column(name = "max_age", nullable = false)
  private Integer maxAge;

  @Column(name = "min_down_payment_percent", precision = 5, scale = 2, nullable = false)
  private BigDecimal minDownPaymentPercent;

  // Fees
  @Column(name = "admin_fee", precision = 15, scale = 2)
  private BigDecimal adminFee = BigDecimal.ZERO;

  @Column(name = "admin_fee_percent", precision = 5, scale = 4)
  private BigDecimal adminFeePercent = BigDecimal.ZERO;

  @Column(name = "appraisal_fee", precision = 15, scale = 2)
  private BigDecimal appraisalFee = BigDecimal.ZERO;

  @Column(name = "insurance_rate", precision = 5, scale = 4)
  private BigDecimal insuranceRate = BigDecimal.ZERO;

  @Column(name = "notary_fee_percent", precision = 5, scale = 4)
  private BigDecimal notaryFeePercent = BigDecimal.ZERO;

  // Promotional
  @Column(name = "is_promotional")
  private Boolean isPromotional = false;

  @Column(name = "promo_description", columnDefinition = "TEXT")
  private String promoDescription;

  @Column(name = "promo_start_date")
  private LocalDate promoStartDate;

  @Column(name = "promo_end_date")
  private LocalDate promoEndDate;

  // Status
  @Column(name = "is_active")
  private Boolean isActive = true;

  @Column(name = "effective_date", nullable = false)
  private LocalDate effectiveDate;

  @Column(name = "expiry_date")
  private LocalDate expiryDate;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // Yearly rate breakdown per tenor/year
  @OneToMany(mappedBy = "kprRate", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("tenor ASC, year ASC")
  private List<KprRateYearly> yearlyRates;

  // Enums
  public enum RateType {
    FIXED,
    FLOATING,
    MIXED
  }

  public enum PropertyTypeFilter {
    RUMAH,
    APARTEMEN,
    RUKO,
    ALL
  }

  public enum CustomerSegment {
    EMPLOYEE,
    PROFESSIONAL,
    ENTREPRENEUR,
    PENSIONER,
    ALL
  }
}
