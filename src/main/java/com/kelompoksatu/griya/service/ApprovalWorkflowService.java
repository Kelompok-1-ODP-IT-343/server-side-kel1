package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.ApprovalConfirmation;
import com.kelompoksatu.griya.entity.KprApplication.ApplicationStatus;
import com.kelompoksatu.griya.entity.User;
import com.kelompoksatu.griya.repository.ApprovalWorkflowRepository;
import com.kelompoksatu.griya.repository.DeveloperRepository;
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
      int updatedRows =
          approvalWorkflowRepository.approveByUserIDandApplicationID(userID, userID, now, reason);

      approvalWorkflowRepository.updateStatusKPRApplication(
          request.getApplicationId(), ApplicationStatus.APPROVAL_PENDING, now);

      return updatedRows > 0;
    }

    log.info("Rejecting workflow for user ID: {}", userID);
    int updatedRows =
        approvalWorkflowRepository.rejectByUserIDandApplicationID(userID, userID, now, reason);
    approvalWorkflowRepository.updateStatusKPRApplication(
        request.getApplicationId(), ApplicationStatus.REJECTED, now);
    return updatedRows > 0;
  }
}
