package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.entity.ApprovalWorkflow;
import com.kelompoksatu.griya.entity.ApprovalWorkflow.PriorityLevel;
import com.kelompoksatu.griya.entity.ApprovalWorkflow.WorkflowStage;
import com.kelompoksatu.griya.entity.ApprovalWorkflow.WorkflowStatus;
import com.kelompoksatu.griya.repository.ApprovalWorkflowRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for ApprovalWorkflow business logic Handles CRUD operations and workflow management
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApprovalWorkflowService {

  private final ApprovalWorkflowRepository approvalWorkflowRepository;

  // ========================================
  // CRUD Operations
  // ========================================

  /** Create a new approval workflow */
  public ApprovalWorkflow createWorkflow(ApprovalWorkflow workflow) {
    log.info("Creating new approval workflow for application ID: {}", workflow.getApplicationId());

    // Set default values if not provided
    if (workflow.getStatus() == null) {
      workflow.setStatus(WorkflowStatus.PENDING);
    }
    if (workflow.getPriority() == null) {
      workflow.setPriority(PriorityLevel.NORMAL);
    }

    ApprovalWorkflow savedWorkflow = approvalWorkflowRepository.save(workflow);
    log.info("Created approval workflow with ID: {}", savedWorkflow.getId());

    return savedWorkflow;
  }

  /** Get workflow by ID */
  @Transactional(readOnly = true)
  public Optional<ApprovalWorkflow> getWorkflowById(Integer id) {
    log.debug("Fetching approval workflow with ID: {}", id);
    return approvalWorkflowRepository.findById(id);
  }

  /** Get all workflows with pagination */
  @Transactional(readOnly = true)
  public Page<ApprovalWorkflow> getAllWorkflows(Pageable pageable) {
    log.debug("Fetching all approval workflows with pagination");
    return approvalWorkflowRepository.findAll(pageable);
  }

  /** Update existing workflow */
  public ApprovalWorkflow updateWorkflow(Integer id, ApprovalWorkflow updatedWorkflow) {
    log.info("Updating approval workflow with ID: {}", id);

    return approvalWorkflowRepository
        .findById(id)
        .map(
            existingWorkflow -> {
              // Update fields
              if (updatedWorkflow.getStage() != null) {
                existingWorkflow.setStage(updatedWorkflow.getStage());
              }
              if (updatedWorkflow.getAssignedTo() != null) {
                existingWorkflow.setAssignedTo(updatedWorkflow.getAssignedTo());
              }
              if (updatedWorkflow.getStatus() != null) {
                existingWorkflow.setStatus(updatedWorkflow.getStatus());
              }
              if (updatedWorkflow.getPriority() != null) {
                existingWorkflow.setPriority(updatedWorkflow.getPriority());
              }
              if (updatedWorkflow.getDueDate() != null) {
                existingWorkflow.setDueDate(updatedWorkflow.getDueDate());
              }
              if (updatedWorkflow.getApprovalNotes() != null) {
                existingWorkflow.setApprovalNotes(updatedWorkflow.getApprovalNotes());
              }
              if (updatedWorkflow.getRejectionReason() != null) {
                existingWorkflow.setRejectionReason(updatedWorkflow.getRejectionReason());
              }
              if (updatedWorkflow.getEscalatedTo() != null) {
                existingWorkflow.setEscalatedTo(updatedWorkflow.getEscalatedTo());
              }

              ApprovalWorkflow saved = approvalWorkflowRepository.save(existingWorkflow);
              log.info("Updated approval workflow with ID: {}", saved.getId());
              return saved;
            })
        .orElseThrow(
            () -> {
              log.error("Approval workflow not found with ID: {}", id);
              return new RuntimeException("Approval workflow not found with ID: " + id);
            });
  }

  /** Delete workflow by ID */
  public void deleteWorkflow(Integer id) {
    log.info("Deleting approval workflow with ID: {}", id);

    if (!approvalWorkflowRepository.existsById(id)) {
      log.error("Approval workflow not found with ID: {}", id);
      throw new RuntimeException("Approval workflow not found with ID: " + id);
    }

    approvalWorkflowRepository.deleteById(id);
    log.info("Deleted approval workflow with ID: {}", id);
  }

  // ========================================
  // Business Logic Methods
  // ========================================

  /** Start a workflow (change status to IN_PROGRESS) */
  public ApprovalWorkflow startWorkflow(Integer workflowId) {
    log.info("Starting workflow with ID: {}", workflowId);

    return approvalWorkflowRepository
        .findById(workflowId)
        .map(
            workflow -> {
              workflow.startWorkflow();
              ApprovalWorkflow saved = approvalWorkflowRepository.save(workflow);
              log.info("Started workflow with ID: {}", workflowId);
              return saved;
            })
        .orElseThrow(
            () -> {
              log.error("Workflow not found with ID: {}", workflowId);
              return new RuntimeException("Workflow not found with ID: " + workflowId);
            });
  }

  /** Approve a workflow */
  public ApprovalWorkflow approveWorkflow(Integer workflowId, String approvalNotes) {
    log.info("Approving workflow with ID: {}", workflowId);

    return approvalWorkflowRepository
        .findById(workflowId)
        .map(
            workflow -> {
              workflow.approveWorkflow(approvalNotes);
              ApprovalWorkflow saved = approvalWorkflowRepository.save(workflow);
              log.info("Approved workflow with ID: {}", workflowId);
              return saved;
            })
        .orElseThrow(
            () -> {
              log.error("Workflow not found with ID: {}", workflowId);
              return new RuntimeException("Workflow not found with ID: " + workflowId);
            });
  }

  /** Reject a workflow */
  public ApprovalWorkflow rejectWorkflow(Integer workflowId, String rejectionReason) {
    log.info("Rejecting workflow with ID: {}", workflowId);

    return approvalWorkflowRepository
        .findById(workflowId)
        .map(
            workflow -> {
              workflow.rejectWorkflow(rejectionReason);
              ApprovalWorkflow saved = approvalWorkflowRepository.save(workflow);
              log.info("Rejected workflow with ID: {}", workflowId);
              return saved;
            })
        .orElseThrow(
            () -> {
              log.error("Workflow not found with ID: {}", workflowId);
              return new RuntimeException("Workflow not found with ID: " + workflowId);
            });
  }

  /** Escalate a workflow */
  public ApprovalWorkflow escalateWorkflow(Integer workflowId, Integer escalatedToUserId) {
    log.info("Escalating workflow with ID: {} to user: {}", workflowId, escalatedToUserId);

    return approvalWorkflowRepository
        .findById(workflowId)
        .map(
            workflow -> {
              workflow.escalateWorkflow(escalatedToUserId);
              ApprovalWorkflow saved = approvalWorkflowRepository.save(workflow);
              log.info("Escalated workflow with ID: {} to user: {}", workflowId, escalatedToUserId);
              return saved;
            })
        .orElseThrow(
            () -> {
              log.error("Workflow not found with ID: {}", workflowId);
              return new RuntimeException("Workflow not found with ID: " + workflowId);
            });
  }

  /** Skip a workflow */
  public ApprovalWorkflow skipWorkflow(Integer workflowId) {
    log.info("Skipping workflow with ID: {}", workflowId);

    return approvalWorkflowRepository
        .findById(workflowId)
        .map(
            workflow -> {
              workflow.skipWorkflow();
              ApprovalWorkflow saved = approvalWorkflowRepository.save(workflow);
              log.info("Skipped workflow with ID: {}", workflowId);
              return saved;
            })
        .orElseThrow(
            () -> {
              log.error("Workflow not found with ID: {}", workflowId);
              return new RuntimeException("Workflow not found with ID: " + workflowId);
            });
  }

  // ========================================
  // Query Methods
  // ========================================

  /** Get workflows by application ID */
  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> getWorkflowsByApplicationId(Integer applicationId) {
    log.debug("Fetching workflows for application ID: {}", applicationId);
    return approvalWorkflowRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId);
  }

  /** Get workflows assigned to a user */
  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> getWorkflowsByAssignedUser(Integer userId) {
    log.debug("Fetching workflows assigned to user ID: {}", userId);
    return approvalWorkflowRepository.findByAssignedTo(userId);
  }

  /** Get workflows assigned to a user with pagination */
  @Transactional(readOnly = true)
  public Page<ApprovalWorkflow> getWorkflowsByAssignedUser(Integer userId, Pageable pageable) {
    log.debug("Fetching workflows assigned to user ID: {} with pagination", userId);
    return approvalWorkflowRepository.findByAssignedTo(userId, pageable);
  }

  /** Get workflows by status */
  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> getWorkflowsByStatus(WorkflowStatus status) {
    log.debug("Fetching workflows with status: {}", status);
    return approvalWorkflowRepository.findByStatus(status);
  }

  /** Get workflows by status with pagination */
  @Transactional(readOnly = true)
  public Page<ApprovalWorkflow> getWorkflowsByStatus(WorkflowStatus status, Pageable pageable) {
    log.debug("Fetching workflows with status: {} with pagination", status);
    return approvalWorkflowRepository.findByStatus(status, pageable);
  }

  /** Get workflows by stage */
  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> getWorkflowsByStage(WorkflowStage stage) {
    log.debug("Fetching workflows with stage: {}", stage);
    return approvalWorkflowRepository.findByStage(stage);
  }

  /** Get workflows by priority */
  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> getWorkflowsByPriority(PriorityLevel priority) {
    log.debug("Fetching workflows with priority: {}", priority);
    return approvalWorkflowRepository.findByPriority(priority);
  }

  /** Get pending workflows for a user */
  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> getPendingWorkflowsByUser(Integer userId) {
    log.debug("Fetching pending workflows for user ID: {}", userId);
    return approvalWorkflowRepository.findPendingWorkflowsByUser(userId);
  }

  /** Get in-progress workflows for a user */
  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> getInProgressWorkflowsByUser(Integer userId) {
    log.debug("Fetching in-progress workflows for user ID: {}", userId);
    return approvalWorkflowRepository.findInProgressWorkflowsByUser(userId);
  }

  /** Get overdue workflows */
  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> getOverdueWorkflows() {
    log.debug("Fetching overdue workflows");
    return approvalWorkflowRepository.findOverdueWorkflows(LocalDateTime.now());
  }

  /** Get current active workflow for application */
  @Transactional(readOnly = true)
  public Optional<ApprovalWorkflow> getCurrentWorkflow(Integer applicationId) {
    log.debug("Fetching current workflow for application ID: {}", applicationId);
    return approvalWorkflowRepository.findCurrentWorkflow(applicationId);
  }

  /** Get next pending workflow for application */
  @Transactional(readOnly = true)
  public Optional<ApprovalWorkflow> getNextPendingWorkflow(Integer applicationId) {
    log.debug("Fetching next pending workflow for application ID: {}", applicationId);
    return approvalWorkflowRepository.findNextPendingWorkflow(applicationId);
  }

  /** Check if application has active workflows */
  @Transactional(readOnly = true)
  public boolean hasActiveWorkflows(Integer applicationId) {
    log.debug("Checking if application ID: {} has active workflows", applicationId);
    return approvalWorkflowRepository.hasActiveWorkflows(applicationId);
  }

  /** Get high priority active workflows */
  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> getHighPriorityActiveWorkflows() {
    log.debug("Fetching high priority active workflows");
    List<WorkflowStatus> activeStatuses =
        List.of(WorkflowStatus.PENDING, WorkflowStatus.IN_PROGRESS);
    return approvalWorkflowRepository.findHighPriorityActiveWorkflows(activeStatuses);
  }

  /** Get workflows that need escalation */
  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> getWorkflowsNeedingEscalation() {
    log.debug("Fetching workflows that need escalation");
    return approvalWorkflowRepository.findWorkflowsNeedingEscalation(LocalDateTime.now());
  }

  /** Get workflow statistics by user */
  @Transactional(readOnly = true)
  public List<Object[]> getWorkflowStatisticsByUser(Integer userId) {
    log.debug("Fetching workflow statistics for user ID: {}", userId);
    return approvalWorkflowRepository.findWorkflowStatisticsByUser(userId);
  }

  /** Get workflow statistics by stage */
  @Transactional(readOnly = true)
  public List<Object[]> getWorkflowStatisticsByStage() {
    log.debug("Fetching workflow statistics by stage");
    List<WorkflowStatus> activeStatuses =
        List.of(WorkflowStatus.PENDING, WorkflowStatus.IN_PROGRESS);
    return approvalWorkflowRepository.findWorkflowStatisticsByStage(activeStatuses);
  }

  /** Get completed workflows within date range */
  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> getCompletedWorkflowsBetween(
      LocalDateTime startDate, LocalDateTime endDate) {
    log.debug("Fetching completed workflows between {} and {}", startDate, endDate);
    return approvalWorkflowRepository.findCompletedWorkflowsBetween(startDate, endDate);
  }

  // ========================================
  // Count Methods
  // ========================================

  /** Count workflows by status */
  @Transactional(readOnly = true)
  public long countWorkflowsByStatus(WorkflowStatus status) {
    log.debug("Counting workflows with status: {}", status);
    return approvalWorkflowRepository.countByStatus(status);
  }

  /** Count workflows by assigned user */
  @Transactional(readOnly = true)
  public long countWorkflowsByAssignedUser(Integer userId) {
    log.debug("Counting workflows assigned to user ID: {}", userId);
    return approvalWorkflowRepository.countByAssignedTo(userId);
  }

  /** Count workflows by assigned user and status */
  @Transactional(readOnly = true)
  public long countWorkflowsByAssignedUserAndStatus(Integer userId, WorkflowStatus status) {
    log.debug("Counting workflows assigned to user ID: {} with status: {}", userId, status);
    return approvalWorkflowRepository.countByAssignedToAndStatus(userId, status);
  }

  // ========================================
  // Bulk Operations
  // ========================================

  /** Create multiple workflows for an application */
  public List<ApprovalWorkflow> createWorkflowsForApplication(
      Integer applicationId, List<ApprovalWorkflow> workflows) {
    log.info("Creating {} workflows for application ID: {}", workflows.size(), applicationId);

    workflows.forEach(
        workflow -> {
          workflow.setApplicationId(applicationId);
          if (workflow.getStatus() == null) {
            workflow.setStatus(WorkflowStatus.PENDING);
          }
          if (workflow.getPriority() == null) {
            workflow.setPriority(PriorityLevel.NORMAL);
          }
        });

    List<ApprovalWorkflow> savedWorkflows = approvalWorkflowRepository.saveAll(workflows);
    log.info("Created {} workflows for application ID: {}", savedWorkflows.size(), applicationId);

    return savedWorkflows;
  }

  /** Delete all workflows for an application */
  public void deleteWorkflowsByApplicationId(Integer applicationId) {
    log.info("Deleting all workflows for application ID: {}", applicationId);
    approvalWorkflowRepository.deleteByApplicationId(applicationId);
    log.info("Deleted all workflows for application ID: {}", applicationId);
  }
}
