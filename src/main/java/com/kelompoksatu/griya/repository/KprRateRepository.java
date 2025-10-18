package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.KprRate;
import com.kelompoksatu.griya.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for KPR Rate operations
 */
@Repository
public interface KprRateRepository extends JpaRepository<KprRate, Integer> {

    /**
     * Find active rates by property type and loan criteria
     */
    @Query("SELECT k FROM KprRate k WHERE k.isActive = true " +
           "AND (k.propertyType = :propertyType OR k.propertyType = 'ALL') " +
           "AND k.minLoanAmount <= :loanAmount AND k.maxLoanAmount >= :loanAmount " +
           "AND k.minTermYears <= :termYears AND k.maxTermYears >= :termYears " +
           "AND (k.effectiveDate <= :currentDate) " +
           "AND (k.expiryDate IS NULL OR k.expiryDate >= :currentDate) " +
           "ORDER BY k.effectiveRate ASC, k.effectiveDate ASC, k.isPromotional DESC")
    List<KprRate> findEligibleRates(@Param("propertyType") KprRate.PropertyTypeFilter propertyType,
                                   @Param("loanAmount") BigDecimal loanAmount,
                                   @Param("termYears") Integer termYears,
                                   @Param("currentDate") LocalDate currentDate);

    /**
     * Find best rate with customer segment filtering
     */
    @Query("SELECT k FROM KprRate k WHERE k.isActive = true " +
           "AND (k.propertyType = :propertyType OR k.propertyType = 'ALL') " +
           "AND (k.customerSegment = :customerSegment OR k.customerSegment = 'ALL') " +
           "AND k.minLoanAmount <= :loanAmount AND k.maxLoanAmount >= :loanAmount " +
           "AND k.minTermYears <= :termYears AND k.maxTermYears >= :termYears " +
           "AND k.minIncome <= :monthlyIncome " +
           "AND (k.effectiveDate <= :currentDate) " +
           "AND (k.expiryDate IS NULL OR k.expiryDate >= :currentDate) " +
           "ORDER BY k.effectiveRate ASC, k.effectiveDate ASC, k.isPromotional DESC")
    Optional<KprRate> findBestEligibleRate(@Param("propertyType") KprRate.PropertyTypeFilter propertyType,
                                          @Param("customerSegment") KprRate.CustomerSegment customerSegment,
                                          @Param("loanAmount") BigDecimal loanAmount,
                                          @Param("termYears") Integer termYears,
                                          @Param("monthlyIncome") BigDecimal monthlyIncome,
                                          @Param("currentDate") LocalDate currentDate);

    /**
     * Find active promotional rates
     */
    @Query("SELECT k FROM KprRate k WHERE k.isActive = true AND k.isPromotional = true " +
           "AND k.promoStartDate <= :currentDate AND k.promoEndDate >= :currentDate " +
           "ORDER BY k.effectiveRate ASC")
    List<KprRate> findActivePromotionalRates(@Param("currentDate") LocalDate currentDate);

    /**
     * Find rates by property type
     */
    List<KprRate> findByPropertyTypeAndIsActiveTrueOrderByEffectiveRateAsc(KprRate.PropertyTypeFilter propertyType);

    /**
     * Find rates by customer segment
     */
    List<KprRate> findByCustomerSegmentAndIsActiveTrueOrderByEffectiveRateAsc(KprRate.CustomerSegment customerSegment);
}