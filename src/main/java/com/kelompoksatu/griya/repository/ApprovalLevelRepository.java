package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.ApprovalLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Approval Level operations
 */
@Repository
public interface ApprovalLevelRepository extends JpaRepository<ApprovalLevel, Integer> {

    /**
     * Find active approval levels ordered by level order
     */
    List<ApprovalLevel> findByIsActiveTrueOrderByLevelOrderAsc();

    /**
     * Find first approval level (level_order = 1)
     */
    Optional<ApprovalLevel> findByLevelOrderAndIsActiveTrue(Integer levelOrder);

    /**
     * Find approval levels applicable for loan amount
     */
    @Query("SELECT a FROM ApprovalLevel a WHERE a.isActive = true " +
           "AND (a.minLoanAmount IS NULL OR a.minLoanAmount <= :loanAmount) " +
           "AND (a.maxLoanAmount IS NULL OR a.maxLoanAmount >= :loanAmount) " +
           "ORDER BY a.levelOrder ASC")
    List<ApprovalLevel> findApplicableLevels(@Param("loanAmount") BigDecimal loanAmount);

    /**
     * Find next approval level after current level
     */
    @Query("SELECT a FROM ApprovalLevel a WHERE a.isActive = true " +
           "AND a.levelOrder > :currentLevelOrder " +
           "ORDER BY a.levelOrder ASC")
    Optional<ApprovalLevel> findNextLevel(@Param("currentLevelOrder") Integer currentLevelOrder);

    /**
     * Find approval level by role
     */
    List<ApprovalLevel> findByRoleRequiredAndIsActiveTrueOrderByLevelOrderAsc(String roleRequired);
}