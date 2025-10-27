package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.ApprovalWorkflow;
import com.kelompoksatu.griya.entity.ApprovalWorkflow.PriorityLevel;
import com.kelompoksatu.griya.entity.ApprovalWorkflow.WorkflowStage;
import com.kelompoksatu.griya.entity.ApprovalWorkflow.WorkflowStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for ApprovalWorkflow entity Provides CRUD operations and custom queries for
 * workflow management
 */
@Repository
public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, Integer> {

  // Basic CRUD operations are inherited from JpaRepository

  // Find by application ID
  List<ApprovalWorkflow> findByApplicationId(Integer applicationId);

  List<ApprovalWorkflow> findByApplicationIdOrderByCreatedAtAsc(Integer applicationId);

  Optional<ApprovalWorkflow> findByApplicationIdAndStage(
      Integer applicationId, WorkflowStage stage);

  // Find by assigned user
  List<ApprovalWorkflow> findByAssignedTo(Integer assignedTo);

  Page<ApprovalWorkflow> findByAssignedTo(Integer assignedTo, Pageable pageable);

  // Find by status
  List<ApprovalWorkflow> findByStatus(WorkflowStatus status);

  Page<ApprovalWorkflow> findByStatus(WorkflowStatus status, Pageable pageable);

  // Find by stage
  List<ApprovalWorkflow> findByStage(WorkflowStage stage);

  Page<ApprovalWorkflow> findByStage(WorkflowStage stage, Pageable pageable);

  // Find by priority
  List<ApprovalWorkflow> findByPriority(PriorityLevel priority);

  Page<ApprovalWorkflow> findByPriority(PriorityLevel priority, Pageable pageable);

  // Find by assigned user and status
  List<ApprovalWorkflow> findByAssignedToAndStatus(Integer assignedTo, WorkflowStatus status);

  List<ApprovalWorkflow> findByAssignedToAndStatusInOrderByDueDateAsc(
      Integer assignedTo, List<WorkflowStatus> statuses);

  Page<ApprovalWorkflow> findByAssignedToAndStatus(
      Integer assignedTo, WorkflowStatus status, Pageable pageable);

  // Find by assigned user and stage
  List<ApprovalWorkflow> findByAssignedToAndStage(Integer assignedTo, WorkflowStage stage);

  // Find current active workflow for application
  @Query(
      "SELECT w FROM ApprovalWorkflow w WHERE w.applicationId = :applicationId "
          + "AND w.status IN ('PENDING', 'IN_PROGRESS') ORDER BY w.createdAt DESC")
  Optional<ApprovalWorkflow> findCurrentWorkflow(@Param("applicationId") Integer applicationId);

  // Find overdue workflows
  @Query(
      "SELECT w FROM ApprovalWorkflow w WHERE w.status IN ('PENDING', 'IN_PROGRESS') "
          + "AND w.dueDate < :currentTime")
  List<ApprovalWorkflow> findOverdueWorkflows(@Param("currentTime") LocalDateTime currentTime);

  // Find workflows by stage and status
  List<ApprovalWorkflow> findByStageAndStatusOrderByCreatedAtAsc(
      WorkflowStage stage, WorkflowStatus status);

  // Find workflows by priority and status
  List<ApprovalWorkflow> findByPriorityAndStatusInOrderByDueDateAsc(
      PriorityLevel priority, List<WorkflowStatus> statuses);

  // Find workflows by date range
  @Query("SELECT aw FROM ApprovalWorkflow aw WHERE aw.createdAt BETWEEN :startDate AND :endDate")
  List<ApprovalWorkflow> findByCreatedAtBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  // Find workflows by completion date range
  @Query("SELECT aw FROM ApprovalWorkflow aw WHERE aw.completedAt BETWEEN :startDate AND :endDate")
  List<ApprovalWorkflow> findByCompletedAtBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  // Find pending workflows for a specific user
  @Query(
      "SELECT aw FROM ApprovalWorkflow aw WHERE aw.assignedTo = :userId AND aw.status = 'PENDING' ORDER BY aw.priority DESC, aw.createdAt ASC")
  List<ApprovalWorkflow> findPendingWorkflowsByUser(@Param("userId") Integer userId);

  // Find in-progress workflows for a specific user
  @Query(
      "SELECT aw FROM ApprovalWorkflow aw WHERE aw.assignedTo = :userId AND aw.status = 'IN_PROGRESS' ORDER BY aw.priority DESC, aw.startedAt ASC")
  List<ApprovalWorkflow> findInProgressWorkflowsByUser(@Param("userId") Integer userId);

  // Find escalated workflows
  List<ApprovalWorkflow> findByEscalatedTo(Integer escalatedTo);

  // Find workflows by multiple statuses
  List<ApprovalWorkflow> findByStatusIn(List<WorkflowStatus> statuses);

  Page<ApprovalWorkflow> findByStatusIn(List<WorkflowStatus> statuses, Pageable pageable);

  // Find workflows by multiple stages
  List<ApprovalWorkflow> findByStageIn(List<WorkflowStage> stages);

  // Count workflows by status
  long countByStatus(WorkflowStatus status);

  // Count workflows by assigned user
  long countByAssignedTo(Integer assignedTo);

  // Count workflows by assigned user and status
  long countByAssignedToAndStatus(Integer assignedTo, WorkflowStatus status);

  // Find workflows with high priority
  @Query(
      "SELECT aw FROM ApprovalWorkflow aw WHERE aw.priority IN ('HIGH', 'URGENT') AND aw.status IN :activeStatuses ORDER BY aw.priority DESC, aw.createdAt ASC")
  List<ApprovalWorkflow> findHighPriorityActiveWorkflows(
      @Param("activeStatuses") List<WorkflowStatus> activeStatuses);

  // Find workflow statistics by user
  @Query(
      "SELECT aw.status, COUNT(aw) FROM ApprovalWorkflow aw WHERE aw.assignedTo = :userId GROUP BY aw.status")
  List<Object[]> findWorkflowStatisticsByUser(@Param("userId") Integer userId);

  // Find workflow statistics by stage
  @Query(
      "SELECT aw.stage, COUNT(aw) FROM ApprovalWorkflow aw WHERE aw.status IN :statuses GROUP BY aw.stage")
  List<Object[]> findWorkflowStatisticsByStage(@Param("statuses") List<WorkflowStatus> statuses);

  // Find workflows that need escalation (overdue and still active)
  @Query(
      "SELECT aw FROM ApprovalWorkflow aw WHERE aw.dueDate < :currentTime AND aw.status IN ('PENDING', 'IN_PROGRESS') AND aw.escalatedTo IS NULL")
  List<ApprovalWorkflow> findWorkflowsNeedingEscalation(
      @Param("currentTime") LocalDateTime currentTime);

  // Find completed workflows within date range
  @Query(
      "SELECT aw FROM ApprovalWorkflow aw WHERE aw.status IN ('APPROVED', 'REJECTED', 'SKIPPED') AND aw.completedAt BETWEEN :startDate AND :endDate")
  List<ApprovalWorkflow> findCompletedWorkflowsBetween(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  // Find workflows by application and status
  List<ApprovalWorkflow> findByApplicationIdAndStatus(Integer applicationId, WorkflowStatus status);

  // Find workflows by application and multiple statuses
  List<ApprovalWorkflow> findByApplicationIdAndStatusIn(
      Integer applicationId, List<WorkflowStatus> statuses);

  // Check if application has any pending workflows
  @Query(
      "SELECT COUNT(aw) > 0 FROM ApprovalWorkflow aw WHERE aw.applicationId = :applicationId AND aw.status IN ('PENDING', 'IN_PROGRESS')")
  boolean hasActiveWorkflows(@Param("applicationId") Integer applicationId);

  // Find next workflow stage for application
  @Query(
      "SELECT aw FROM ApprovalWorkflow aw WHERE aw.applicationId = :applicationId AND aw.status = 'PENDING' ORDER BY aw.stage ASC")
  Optional<ApprovalWorkflow> findNextPendingWorkflow(@Param("applicationId") Integer applicationId);

  // Find current active workflow for application
  @Query(
      "SELECT aw FROM ApprovalWorkflow aw WHERE aw.applicationId = :applicationId AND aw.status = 'IN_PROGRESS'")
  Optional<ApprovalWorkflow> findCurrentActiveWorkflow(
      @Param("applicationId") Integer applicationId);

  /** Approve workflow by user ID and application ID */
  @Modifying
  @Query(
      "UPDATE ApprovalWorkflow aw SET aw.status = 'CREDIT_ANALYSIS', aw.completedAt = :completedAt, aw.approvalNotes = :approvalNotes WHERE aw.assignedTo = :userId AND aw.applicationId = :applicationId AND aw.status = 'PENDING'")
  int approveByUserIDandApplicationID(
      @Param("userId") Integer userId,
      @Param("applicationId") Integer applicationId,
      @Param("completedAt") LocalDateTime completedAt,
      @Param("approvalNotes") String approvalNotes);

  // Reject workflow by user ID and application ID
  @Modifying
  @Query(
      "UPDATE ApprovalWorkflow aw SET aw.status = 'REJECTED', aw.completedAt = :completedAt, aw.rejectionReason = :rejectionReason WHERE aw.assignedTo = :userId AND aw.applicationId = :applicationId AND aw.status = 'PENDING'")
  int rejectByUserIDandApplicationID(
      @Param("userId") Integer userId,
      @Param("applicationId") Integer applicationId,
      @Param("completedAt") LocalDateTime completedAt,
      @Param("rejectionReason") String rejectionReason);

  // Delete workflows by application ID (for cleanup)
  void deleteByApplicationId(Integer applicationId);
}
