package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.entity.ApprovalWorkflow;
import com.kelompoksatu.griya.entity.ApprovalWorkflow.PriorityLevel;
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
 * following clean architecture principles
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ApprovalWorkflowService {

  private final ApprovalWorkflowRepository approvalWorkflowRepository;

  // ========================================
  // VALIDATION METHODS
  // ========================================

  /** Validate workflow exists and return it */
  private ApprovalWorkflow validateWorkflowExists(Integer workflowId) {
    return approvalWorkflowRepository
        .findById(workflowId)
        .orElseThrow(
            () ->
                new IllegalArgumentException("Approval workflow not found with ID: " + workflowId));
  }

  /** Validate workflow can be modified */
  private void validateWorkflowCanBeModified(ApprovalWorkflow workflow) {
    if (workflow.isCompleted()) {
      throw new IllegalStateException("Cannot modify completed workflow");
    }
  }

  /** Set default values for new workflow */
  private void setWorkflowDefaults(ApprovalWorkflow workflow) {
    if (workflow.getStatus() == null) {
      workflow.setStatus(WorkflowStatus.PENDING);
    }
    if (workflow.getPriority() == null) {
      workflow.setPriority(PriorityLevel.NORMAL);
    }
  }

  // ========================================
  // CRUD OPERATIONS
  // ========================================

  /** Create a new approval workflow */
  public ApprovalWorkflow createWorkflow(ApprovalWorkflow workflow) {
    log.info("Creating new approval workflow for application ID: {}", workflow.getApplicationId());

    setWorkflowDefaults(workflow);
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
              updateWorkflowFields(existingWorkflow, updatedWorkflow);
              ApprovalWorkflow saved = approvalWorkflowRepository.save(existingWorkflow);
              log.info("Updated approval workflow with ID: {}", saved.getId());
              return saved;
            })
        .orElseThrow(
            () -> new IllegalArgumentException("Approval workflow not found with ID: " + id));
  }

  /** Update workflow fields helper method */
  private void updateWorkflowFields(ApprovalWorkflow existing, ApprovalWorkflow updated) {
    if (updated.getStage() != null) {
      existing.setStage(updated.getStage());
    }
    if (updated.getAssignedTo() != null) {
      existing.setAssignedTo(updated.getAssignedTo());
    }
    if (updated.getStatus() != null) {
      existing.setStatus(updated.getStatus());
    }
    if (updated.getPriority() != null) {
      existing.setPriority(updated.getPriority());
    }
    if (updated.getDueDate() != null) {
      existing.setDueDate(updated.getDueDate());
    }
    if (updated.getApprovalNotes() != null) {
      existing.setApprovalNotes(updated.getApprovalNotes());
    }
    if (updated.getRejectionReason() != null) {
      existing.setRejectionReason(updated.getRejectionReason());
    }
    if (updated.getEscalatedTo() != null) {
      existing.setEscalatedTo(updated.getEscalatedTo());
    }
  }

  /** Delete workflow by ID */
  public void deleteWorkflow(Integer id) {
    log.info("Deleting approval workflow with ID: {}", id);

    if (!approvalWorkflowRepository.existsById(id)) {
      throw new IllegalArgumentException("Approval workflow not found with ID: " + id);
    }

    approvalWorkflowRepository.deleteById(id);
    log.info("Deleted approval workflow with ID: {}", id);
  }

  // ========================================
  // WORKFLOW STATE MANAGEMENT
  // ========================================

  /** Start workflow processing */
  public ApprovalWorkflow startWorkflow(Integer workflowId) {
    log.info("Starting approval workflow with ID: {}", workflowId);

    ApprovalWorkflow workflow = validateWorkflowExists(workflowId);
    validateWorkflowCanBeModified(workflow);

    workflow.startWorkflow();
    ApprovalWorkflow saved = approvalWorkflowRepository.save(workflow);

    log.info("Started approval workflow with ID: {}", saved.getId());
    return saved;
  }

  /** Approve workflow */
  public ApprovalWorkflow approveWorkflow(Integer workflowId, String approvalNotes) {
    log.info("Approving workflow with ID: {}", workflowId);

    ApprovalWorkflow workflow = validateWorkflowExists(workflowId);
    validateWorkflowCanBeModified(workflow);

    workflow.approveWorkflow(approvalNotes);
    ApprovalWorkflow saved = approvalWorkflowRepository.save(workflow);

    log.info("Approved workflow with ID: {}", saved.getId());
    return saved;
  }

  /** Reject workflow */
  public ApprovalWorkflow rejectWorkflow(Integer workflowId, String rejectionReason) {
    log.info("Rejecting workflow with ID: {}", workflowId);

    ApprovalWorkflow workflow = validateWorkflowExists(workflowId);
    validateWorkflowCanBeModified(workflow);

    workflow.rejectWorkflow(rejectionReason);
    ApprovalWorkflow saved = approvalWorkflowRepository.save(workflow);

    log.info("Rejected workflow with ID: {}", saved.getId());
    return saved;
  }

  /** Escalate workflow */
  public ApprovalWorkflow escalateWorkflow(Integer workflowId, Integer escalatedToUserId) {
    log.info("Escalating workflow with ID: {} to user: {}", workflowId, escalatedToUserId);

    ApprovalWorkflow workflow = validateWorkflowExists(workflowId);
    validateWorkflowCanBeModified(workflow);

    workflow.escalateWorkflow(escalatedToUserId);
    ApprovalWorkflow saved = approvalWorkflowRepository.save(workflow);

    log.info("Escalated workflow with ID: {} to user: {}", saved.getId(), escalatedToUserId);
    return saved;
  }

  /** Skip workflow */
  public ApprovalWorkflow skipWorkflow(Integer workflowId) {
    log.info("Skipping workflow with ID: {}", workflowId);

    ApprovalWorkflow workflow = validateWorkflowExists(workflowId);
    validateWorkflowCanBeModified(workflow);

    workflow.skipWorkflow();
    ApprovalWorkflow saved = approvalWorkflowRepository.save(workflow);

    log.info("Skipped workflow with ID: {}", saved.getId());
    return saved;
  }

  // ========================================
  // QUERY METHODS - BY APPLICATION
  // ========================================

  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> getWorkflowsByApplicationId(Integer applicationId) {
    log.debug("Fetching workflows for application ID: {}", applicationId);
    return approvalWorkflowRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId);
  }

  @Transactional(readOnly = true)
  public Page<ApprovalWorkflow> getWorkflowsByAssignedUser(Integer userId, Pageable pageable) {
    log.debug("Fetching workflows assigned to user ID: {} with pagination", userId);
    return approvalWorkflowRepository.findByAssignedTo(userId, pageable);
  }

  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> getPendingWorkflowsByUser(Integer userId) {
    log.debug("Fetching pending workflows for user ID: {}", userId);
    return approvalWorkflowRepository.findByAssignedToAndStatus(userId, WorkflowStatus.PENDING);
  }

  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> getInProgressWorkflowsByUser(Integer userId) {
    log.debug("Fetching in-progress workflows for user ID: {}", userId);
    return approvalWorkflowRepository.findByAssignedToAndStatus(userId, WorkflowStatus.IN_PROGRESS);
  }

  // ========================================
  // QUERY METHODS - BY STATUS AND STAGE
  // ========================================
  @Transactional(readOnly = true)
  public Page<ApprovalWorkflow> getWorkflowsByStatus(WorkflowStatus status, Pageable pageable) {
    log.debug("Fetching workflows with status: {} with pagination", status);
    return approvalWorkflowRepository.findByStatus(status, pageable);
  }

  // ========================================
  // SPECIALIZED QUERY METHODS
  // ========================================

  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> getOverdueWorkflows() {
    log.debug("Fetching overdue workflows");
    return approvalWorkflowRepository.findOverdueWorkflows(LocalDateTime.now());
  }

  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> getWorkflowsNeedingEscalation() {
    log.debug("Fetching workflows needing escalation");
    return approvalWorkflowRepository.findWorkflowsNeedingEscalation(LocalDateTime.now());
  }

  // ========================================
  // STATISTICS AND REPORTING
  // ========================================

  @Transactional(readOnly = true)
  public List<ApprovalWorkflow> getCompletedWorkflowsBetween(
      LocalDateTime startDate, LocalDateTime endDate) {
    log.debug("Fetching completed workflows between {} and {}", startDate, endDate);
    return approvalWorkflowRepository.findCompletedWorkflowsBetween(startDate, endDate);
  }

  @Transactional(readOnly = true)
  public long countWorkflowsByStatus(WorkflowStatus status) {
    log.debug("Counting workflows with status: {}", status);
    return approvalWorkflowRepository.countByStatus(status);
  }

  @Transactional(readOnly = true)
  public long countWorkflowsByAssignedUser(Integer userId) {
    log.debug("Counting workflows assigned to user ID: {}", userId);
    return approvalWorkflowRepository.countByAssignedTo(userId);
  }

  @Transactional(readOnly = true)
  public long countWorkflowsByAssignedUserAndStatus(Integer userId, WorkflowStatus status) {
    log.debug("Counting workflows assigned to user ID: {} with status: {}", userId, status);
    return approvalWorkflowRepository.countByAssignedToAndStatus(userId, status);
  }

  // ========================================
  // BULK OPERATIONS
  // ========================================

  /** Create multiple workflows for an application */
  public List<ApprovalWorkflow> createWorkflowsForApplication(
      Integer applicationId, List<ApprovalWorkflow> workflows) {
    log.info("Creating {} workflows for application ID: {}", workflows.size(), applicationId);

    workflows.forEach(
        workflow -> {
          workflow.setApplicationId(applicationId);
          setWorkflowDefaults(workflow);
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
