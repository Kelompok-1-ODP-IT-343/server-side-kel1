package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.Loan;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for Loan operations Handles loan management, payment tracking, and balance
 * calculations
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Integer> {

  /** Find loan by application ID */
  Optional<Loan> findByApplicationId(Integer applicationId);

  /** Find loan by loan number */
  Optional<Loan> findByLoanNumber(String loanNumber);

  /** Find all loans for a specific user */
  List<Loan> findByUserIdOrderByCreatedAtDesc(Integer userId);

  /** Find active loans for a user */
  List<Loan> findByUserIdAndStatusOrderByCreatedAtDesc(Integer userId, Loan.LoanStatus status);

  /** Find loans by status */
  List<Loan> findByStatusOrderByCreatedAtDesc(Loan.LoanStatus status);

  /** Find loans with outstanding balance greater than zero */
  @Query("SELECT l FROM Loan l WHERE l.outstandingBalance > 0 ORDER BY l.createdAt DESC")
  List<Loan> findLoansWithOutstandingBalance();

  /** Find loans due for payment (end date approaching) */
  @Query(
      "SELECT l FROM Loan l WHERE l.endDate <= :dueDate AND l.status = 'ACTIVE' ORDER BY l.endDate ASC")
  List<Loan> findLoansDueByDate(@Param("dueDate") LocalDate dueDate);

  /** Calculate total outstanding balance for a user */
  @Query(
      "SELECT COALESCE(SUM(l.outstandingBalance), 0) FROM Loan l WHERE l.userId = :userId AND l.status = 'ACTIVE'")
  BigDecimal calculateTotalOutstandingBalanceByUser(@Param("userId") Integer userId);

  /** Find loans by disbursement date range */
  @Query(
      "SELECT l FROM Loan l WHERE l.disbursementDate BETWEEN :startDate AND :endDate ORDER BY l.disbursementDate DESC")
  List<Loan> findByDisbursementDateBetween(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
