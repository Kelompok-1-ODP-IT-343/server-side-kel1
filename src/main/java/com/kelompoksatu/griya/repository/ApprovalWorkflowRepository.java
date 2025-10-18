package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.ApprovalWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Approval Workflow operations
 */
@Repository
public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, Integer> {

    /**
     * Find workflow by application ID
     */
    List<ApprovalWorkflow> findByApplicationIdOrderByCreatedAtAsc(Integer applicationId);

    /**
     * Find current active workflow for application
     */
    @Query("SELECT w FROM ApprovalWorkflow w WHERE w.applicationId = :applicationId " +
           "AND w.status IN ('PENDING', 'IN_PROGRESS') ORDER BY w.createdAt DESC")
    Optional<ApprovalWorkflow> findCurrentWorkflow(@Param("applicationId") Integer applicationId);

    /**
     * Find workflows assigned to user
     */
    List<ApprovalWorkflow> findByAssignedToAndStatusInOrderByDueDateAsc(
            Integer assignedTo, List<ApprovalWorkflow.WorkflowStatus> statuses);

    /**
     * Find overdue workflows
     */
    @Query("SELECT w FROM ApprovalWorkflow w WHERE w.status IN ('PENDING', 'IN_PROGRESS') " +
           "AND w.dueDate < :currentTime")
    List<ApprovalWorkflow> findOverdueWorkflows(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Find workflows by stage and status
     */
    List<ApprovalWorkflow> findByStageAndStatusOrderByCreatedAtAsc(
            ApprovalWorkflow.WorkflowStage stage, ApprovalWorkflow.WorkflowStatus status);

    /**
     * Find workflows by priority
     */
    List<ApprovalWorkflow> findByPriorityAndStatusInOrderByDueDateAsc(
            ApprovalWorkflow.WorkflowPriority priority, List<ApprovalWorkflow.WorkflowStatus> statuses);
}
