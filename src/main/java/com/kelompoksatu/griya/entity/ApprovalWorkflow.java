package com.kelompoksatu.griya.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Approval workflow tracking for KPR applications
 */
@Entity
@Table(name = "approval_workflow")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApprovalWorkflow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "application_id", nullable = false)
    private Integer applicationId;

    @Column(name = "approval_level_id", nullable = false)
    private Integer approvalLevelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "stage", nullable = false)
    private WorkflowStage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkflowStatus status = WorkflowStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private WorkflowPriority priority = WorkflowPriority.NORMAL;

    @Column(name = "assigned_to")
    private Integer assignedTo; // User ID of assigned approver

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @Column(name = "decision_reason", columnDefinition = "TEXT")
    private String decisionReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", insertable = false, updatable = false)
    private KprApplication kprApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_level_id", insertable = false, updatable = false)
    private ApprovalLevel approvalLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", insertable = false, updatable = false)
    private User assignedUser;

    // Enums
    public enum WorkflowStage {
        DOCUMENT_VERIFICATION, PROPERTY_APPRAISAL, CREDIT_ANALYSIS, 
        MANAGER_APPROVAL, FINAL_APPROVAL
    }

    public enum WorkflowStatus {
        PENDING, IN_PROGRESS, APPROVED, REJECTED, ESCALATED, CANCELLED
    }

    public enum WorkflowPriority {
        LOW, NORMAL, HIGH, URGENT
    }
}