package com.kelompoksatu.griya.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Configurable approval hierarchy levels
 */
@Entity
@Table(name = "approval_levels")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "level_name", length = 100, nullable = false)
    private String levelName;

    @Column(name = "level_order", nullable = false)
    private Integer levelOrder; // 1, 2, 3, etc.

    @Column(name = "role_required", length = 100, nullable = false)
    private String roleRequired; // 'branch_manager', 'property_auditor', 'rm', etc.

    @Column(name = "min_loan_amount", precision = 15, scale = 2)
    private BigDecimal minLoanAmount = BigDecimal.ZERO;

    @Column(name = "max_loan_amount", precision = 15, scale = 2)
    private BigDecimal maxLoanAmount;

    @Column(name = "is_required")
    private Boolean isRequired = true;

    @Column(name = "can_skip")
    private Boolean canSkip = false;

    @Column(name = "timeout_hours")
    private Integer timeoutHours = 72; // Auto-escalation timeout

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}