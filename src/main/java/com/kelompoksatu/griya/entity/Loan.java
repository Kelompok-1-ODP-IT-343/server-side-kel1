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

/**
 * Loan entity representing active loans after KPR application approval Tracks loan disbursement,
 * payments, and outstanding balance
 */
@Entity
@Table(name = "loans")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Loan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "loan_number", length = 20, unique = true, nullable = false)
  private String loanNumber;

  @Column(name = "application_id", unique = true, nullable = false)
  private Integer applicationId;

  @Column(name = "user_id", nullable = false)
  private Integer userId;

  @Column(name = "principal_amount", precision = 15, scale = 2, nullable = false)
  private BigDecimal principalAmount;

  @Column(name = "interest_rate", precision = 5, scale = 4, nullable = false)
  private BigDecimal interestRate;

  @Column(name = "term_months", nullable = false)
  private Integer termMonths;

  @Column(name = "monthly_installment", precision = 15, scale = 2, nullable = false)
  private BigDecimal monthlyInstallment;

  @Column(name = "outstanding_balance", precision = 15, scale = 2, nullable = false)
  private BigDecimal outstandingBalance;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(name = "end_date", nullable = false)
  private LocalDate endDate;

  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  @Column(name = "status")
  private LoanStatus status = LoanStatus.ACTIVE;

  @Column(name = "disbursement_date")
  private LocalDate disbursementDate;

  @Column(name = "disbursement_amount", precision = 15, scale = 2)
  private BigDecimal disbursementAmount;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // Relationships
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "application_id", insertable = false, updatable = false)
  private KprApplication kprApplication;

  // Enums
  public enum LoanStatus {
    ACTIVE,
    PAID_OFF,
    DEFAULTED,
    RESTRUCTURED,
    CLOSED
  }
}
