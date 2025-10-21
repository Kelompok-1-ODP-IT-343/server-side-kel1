package com.kelompoksatu.griya.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Approval workflow tracking for KPR applications */
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

  @Enumerated(EnumType.STRING)
  @Column(name = "stage", nullable = false)
  private WorkflowStage stage;

  @Column(name = "assigned_to", nullable = false)
  private Integer assignedTo;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private WorkflowStatus status = WorkflowStatus.PENDING;

  @Enumerated(EnumType.STRING)
  @Column(name = "priority", nullable = false)
  private PriorityLevel priority = PriorityLevel.NORMAL;

  @Column(name = "due_date")
  private LocalDateTime dueDate;

  @Column(name = "started_at")
  private LocalDateTime startedAt;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @Column(name = "approval_notes", columnDefinition = "TEXT")
  private String approvalNotes;

  @Column(name = "rejection_reason", columnDefinition = "TEXT")
  private String rejectionReason;

  @Column(name = "escalated_to")
  private Integer escalatedTo;

  @Column(name = "escalated_at")
  private LocalDateTime escalatedAt;

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
  @JoinColumn(name = "assigned_to", insertable = false, updatable = false)
  private User assignedUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "escalated_to", insertable = false, updatable = false)
  private User escalatedUser;

  // Enums matching the SQL schema exactly
  public enum WorkflowStage {
    DOCUMENT_VERIFICATION,
    CREDIT_ANALYSIS,
    PROPERTY_APPRAISAL,
    FINAL_APPROVAL
  }

  public enum WorkflowStatus {
    PENDING,
    IN_PROGRESS,
    APPROVED,
    REJECTED,
    ESCALATED,
    SKIPPED
  }

  public enum PriorityLevel {
    LOW,
    NORMAL,
    HIGH,
    URGENT
  }

  // Utility methods for workflow management
  public void startWorkflow() {
    this.status = WorkflowStatus.IN_PROGRESS;
    this.startedAt = LocalDateTime.now();
  }

  public void approveWorkflow(String notes) {
    this.status = WorkflowStatus.APPROVED;
    this.completedAt = LocalDateTime.now();
    this.approvalNotes = notes;
  }

  public void rejectWorkflow(String reason) {
    this.status = WorkflowStatus.REJECTED;
    this.completedAt = LocalDateTime.now();
    this.rejectionReason = reason;
  }

  public void escalateWorkflow(Integer escalatedToUserId) {
    this.status = WorkflowStatus.ESCALATED;
    this.escalatedTo = escalatedToUserId;
    this.escalatedAt = LocalDateTime.now();
  }

  public void skipWorkflow() {
    this.status = WorkflowStatus.SKIPPED;
    this.completedAt = LocalDateTime.now();
  }

  public boolean isCompleted() {
    return status == WorkflowStatus.APPROVED
        || status == WorkflowStatus.REJECTED
        || status == WorkflowStatus.SKIPPED;
  }

  public boolean isPending() {
    return status == WorkflowStatus.PENDING;
  }

  public boolean isInProgress() {
    return status == WorkflowStatus.IN_PROGRESS;
  }
}
