package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.ApprovalConfirmation;
import com.kelompoksatu.griya.entity.ApprovalWorkflow;
import com.kelompoksatu.griya.entity.KprApplication.ApplicationStatus;
import com.kelompoksatu.griya.entity.User;
import com.kelompoksatu.griya.repository.ApprovalWorkflowRepository;
import com.kelompoksatu.griya.repository.DeveloperRepository;
import com.kelompoksatu.griya.repository.KprApplicationRepository;
import com.kelompoksatu.griya.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final DeveloperRepository developerRepository;
  private final UserRepository userRepository;
  private final KprApplicationRepository kprApplicationRepository;

  public boolean approveOrRejectWorkflowDeveloper(ApprovalConfirmation request, Integer userID) {
    var user =
        userRepository
            .findById(userID)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userID));
    log.info(
        "Processing approval/rejection for user: {} with request: {}",
        user.getDeveloper().getCompanyName(),
        request);
    Boolean isApproved = request.getIsApproved();
    String reason = request.getReason();
    var now = LocalDateTime.now();
    if (isApproved) {
      log.info("Approving workflow for user ID: {}", userID);
      int updatedRows =
          approvalWorkflowRepository.approveByUserIDandApplicationID(
              userID, request.getApplicationId(), now, reason);

      // Update KPR application status to PROPERTY_APPRAISAL when developer approves
      if (updatedRows > 0) {
        approvalWorkflowRepository.updateStatusKPRApplication(
            request.getApplicationId(), ApplicationStatus.PROPERTY_APPRAISAL, now);
      }

      return updatedRows > 0;
    }
    log.info("Rejecting workflow for user ID: {}", userID);
    int updatedRows =
        approvalWorkflowRepository.rejectByUserIDandApplicationID(
            userID, request.getApplicationId(), now, reason);
    approvalWorkflowRepository.updateStatusKPRApplication(
        request.getApplicationId(), ApplicationStatus.REJECTED, now);
    return updatedRows > 0;
  }

  public boolean approveOrRejectWorkflowVerifikator(ApprovalConfirmation request, Integer userID) {
    User verifikator =
        userRepository
            .findById(userID)
            .orElseThrow(
                () -> new IllegalArgumentException("Verifikator not found with ID: " + userID));

    log.info(
        "Processing approval/rejection for verifikator: {} with request: {}",
        verifikator.getUsername(),
        request);

    Boolean isApproved = request.getIsApproved();
    String reason = request.getReason();
    var now = LocalDateTime.now();

    if (isApproved) {
      log.info("Approving workflow for user ID: {}", userID);

      // First, get current pending workflow to determine the stage
      var currentWorkflow =
          approvalWorkflowRepository
              .findByApplicationIdAndStatus(
                  request.getApplicationId(), ApprovalWorkflow.WorkflowStatus.PENDING)
              .stream()
              .filter(w -> w.getAssignedTo().equals(userID))
              .findFirst();

      if (currentWorkflow.isEmpty()) {
        log.warn(
            "No pending workflow found for user {} and application {}",
            userID,
            request.getApplicationId());
        return false;
      }

      var workflow = currentWorkflow.get();
      log.info("Processing approval for stage: {}", workflow.getStage());

      // Approve the workflow
      int updatedRows =
          approvalWorkflowRepository.approveByUserIDandApplicationID(
              userID, request.getApplicationId(), now, reason);

      if (updatedRows > 0) {
        // Determine next application status based on current stage
        ApplicationStatus nextStatus = determineNextApplicationStatus(workflow.getStage());

        if (nextStatus == ApplicationStatus.APPROVED) {
          kprApplicationRepository.updateKprApplicationStatus(
              request.getApplicationId(), ApplicationStatus.APPROVED);
        } else {
          approvalWorkflowRepository.updateStatusKPRApplication(
              request.getApplicationId(), nextStatus, now);
        }
      }

      return updatedRows > 0;
    }

    kprApplicationRepository.updateKprApplicationStatus(
        request.getApplicationId(), ApplicationStatus.REJECTED);

    log.info("Rejecting workflow for user ID: {}", userID);
    int updatedRows =
        approvalWorkflowRepository.rejectByUserIDandApplicationID(
            userID, request.getApplicationId(), now, reason);
    approvalWorkflowRepository.updateStatusKPRApplication(
        request.getApplicationId(), ApplicationStatus.REJECTED, now);
    return updatedRows > 0;
  }

  /** Determine next application status based on current workflow stage */
  private ApplicationStatus determineNextApplicationStatus(
      ApprovalWorkflow.WorkflowStage currentStage) {
    switch (currentStage) {
      case PROPERTY_APPRAISAL:
        return ApplicationStatus.CREDIT_ANALYSIS;
      case CREDIT_ANALYSIS:
        return ApplicationStatus.APPROVAL_PENDING;
      case FINAL_APPROVAL:
        return ApplicationStatus.APPROVED;
      default:
        return ApplicationStatus.APPROVAL_PENDING;
    }
  }
}
