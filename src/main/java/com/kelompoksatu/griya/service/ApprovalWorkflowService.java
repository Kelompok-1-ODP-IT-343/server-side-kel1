package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.ApprovalConfirmation;
import com.kelompoksatu.griya.entity.ApprovalWorkflow;
import com.kelompoksatu.griya.entity.KprApplication.ApplicationStatus;
import com.kelompoksatu.griya.entity.NotificationChannel;
import com.kelompoksatu.griya.entity.NotificationType;
import com.kelompoksatu.griya.entity.SystemNotification;
import com.kelompoksatu.griya.entity.User;
import com.kelompoksatu.griya.repository.ApprovalWorkflowRepository;
import com.kelompoksatu.griya.repository.DeveloperRepository;
import com.kelompoksatu.griya.repository.KprApplicationRepository;
import com.kelompoksatu.griya.repository.PropertyRepository;
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
  private final PropertyRepository propertyRepository;
  private final SystemNotificationService systemNotificationService;

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

        // Notifications: actor and applicant
        try {
          var optApp = kprApplicationRepository.findById(request.getApplicationId());
          Integer applicantId = optApp.map(a -> a.getUser().getId()).orElse(null);
          String appNumber = optApp.map(a -> a.getApplicationNumber()).orElse("N/A");

          // Actor notification
          systemNotificationService.saveNotification(
              SystemNotification.builder()
                  .userId(userID)
                  .notificationType(NotificationType.APPLICATION_UPDATE)
                  .title("Anda menyetujui verifikasi awal")
                  .message("Aplikasi: " + appNumber + ", status ke PROPERTY_APPRAISAL")
                  .channel(NotificationChannel.IN_APP)
                  .build());

          // Applicant notification
          if (applicantId != null) {
            systemNotificationService.saveNotification(
                SystemNotification.builder()
                    .userId(applicantId)
                    .notificationType(NotificationType.APPLICATION_UPDATE)
                    .title("Aplikasi Anda lanjut ke tahap appraisal")
                    .message("Nomor aplikasi: " + appNumber)
                    .channel(NotificationChannel.IN_APP)
                    .build());
          }
        } catch (Exception notifEx) {
          log.warn("Failed to save developer approval notifications: {}", notifEx.getMessage());
        }
      }

      return updatedRows > 0;
    }
    log.info("Rejecting workflow for user ID: {}", userID);
    int updatedRows =
        approvalWorkflowRepository.rejectByUserIDandApplicationID(
            userID, request.getApplicationId(), now, reason);
    approvalWorkflowRepository.updateStatusKPRApplication(
        request.getApplicationId(), ApplicationStatus.REJECTED, now);

    // If the application was tied to a reserved property, release it back to AVAILABLE
    try {
      var optApp = kprApplicationRepository.findById(request.getApplicationId());
      if (optApp.isPresent()) {
        var app = optApp.get();
        Integer propertyId = app.getPropertyId();
        if (propertyId != null) {
          propertyRepository
              .findById(propertyId)
              .ifPresent(
                  p -> {
                    try {
                      if (p.getStatus() != null && p.getStatus().name().equals("RESERVED")) {
                        p.setStatus(
                            com.kelompoksatu.griya.entity.Property.PropertyStatus.AVAILABLE);
                        propertyRepository.save(p);
                        log.info(
                            "Property {} released from RESERVED to AVAILABLE due to rejection of application {}",
                            propertyId,
                            request.getApplicationId());
                      }
                    } catch (Exception e) {
                      log.warn(
                          "Failed to update property status for property {}: {}",
                          propertyId,
                          e.getMessage());
                    }
                  });
        }
      }
    } catch (Exception e) {
      log.warn(
          "Error while attempting to release reserved property for application {}: {}",
          request.getApplicationId(),
          e.getMessage());
    }

    // Notifications: actor and applicant for rejection
    try {
      var optApp = kprApplicationRepository.findById(request.getApplicationId());
      Integer applicantId = optApp.map(a -> a.getUser().getId()).orElse(null);
      String appNumber = optApp.map(a -> a.getApplicationNumber()).orElse("N/A");

      systemNotificationService.saveNotification(
          SystemNotification.builder()
              .userId(userID)
              .notificationType(NotificationType.APPLICATION_UPDATE)
              .title("Anda menolak aplikasi pada verifikasi awal")
              .message("Aplikasi: " + appNumber + ", status REJECTED")
              .channel(NotificationChannel.IN_APP)
              .build());

      if (applicantId != null) {
        systemNotificationService.saveNotification(
            SystemNotification.builder()
                .userId(applicantId)
                .notificationType(NotificationType.APPLICATION_UPDATE)
                .title("Aplikasi KPR Anda ditolak")
                .message("Nomor aplikasi: " + appNumber)
                .channel(NotificationChannel.IN_APP)
                .build());
      }
    } catch (Exception notifEx) {
      log.warn("Failed to save developer rejection notifications: {}", notifEx.getMessage());
    }
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

        // Notifications: actor and applicant for approval & transition
        try {
          var optApp = kprApplicationRepository.findById(request.getApplicationId());
          Integer applicantId = optApp.map(a -> a.getUser().getId()).orElse(null);
          String appNumber = optApp.map(a -> a.getApplicationNumber()).orElse("N/A");

          systemNotificationService.saveNotification(
              SystemNotification.builder()
                  .userId(userID)
                  .notificationType(NotificationType.APPLICATION_UPDATE)
                  .title("Anda menyetujui tahap " + workflow.getStage())
                  .message("Aplikasi: " + appNumber + ", status ke " + nextStatus)
                  .channel(NotificationChannel.IN_APP)
                  .build());

          if (applicantId != null) {
            String title =
                (nextStatus == ApplicationStatus.APPROVED)
                    ? "Aplikasi KPR Anda disetujui"
                    : "Status aplikasi berubah ke " + nextStatus;
            systemNotificationService.saveNotification(
                SystemNotification.builder()
                    .userId(applicantId)
                    .notificationType(NotificationType.APPLICATION_UPDATE)
                    .title(title)
                    .message("Nomor aplikasi: " + appNumber)
                    .channel(NotificationChannel.IN_APP)
                    .build());
          }
        } catch (Exception notifEx) {
          log.warn("Failed to save verifier approval notifications: {}", notifEx.getMessage());
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

    // Release reserved property if applicable
    try {
      var optApp = kprApplicationRepository.findById(request.getApplicationId());
      if (optApp.isPresent()) {
        var app = optApp.get();
        Integer propertyId = app.getPropertyId();
        if (propertyId != null) {
          propertyRepository
              .findById(propertyId)
              .ifPresent(
                  p -> {
                    try {
                      if (p.getStatus() != null && p.getStatus().name().equals("RESERVED")) {
                        p.setStatus(
                            com.kelompoksatu.griya.entity.Property.PropertyStatus.AVAILABLE);
                        propertyRepository.save(p);
                        log.info(
                            "Property {} released from RESERVED to AVAILABLE due to rejection of application {}",
                            propertyId,
                            request.getApplicationId());
                      }
                    } catch (Exception e) {
                      log.warn(
                          "Failed to update property status for property {}: {}",
                          propertyId,
                          e.getMessage());
                    }
                  });
        }
      }
    } catch (Exception e) {
      log.warn(
          "Error while attempting to release reserved property for application {}: {}",
          request.getApplicationId(),
          e.getMessage());
    }

    // Notifications: actor and applicant for rejection
    try {
      var optApp = kprApplicationRepository.findById(request.getApplicationId());
      Integer applicantId = optApp.map(a -> a.getUser().getId()).orElse(null);
      String appNumber = optApp.map(a -> a.getApplicationNumber()).orElse("N/A");

      systemNotificationService.saveNotification(
          SystemNotification.builder()
              .userId(userID)
              .notificationType(NotificationType.APPLICATION_UPDATE)
              .title(
                  "Anda menolak aplikasi pada tahap "
                      + (workflowStageSafe(request.getApplicationId(), userID)))
              .message("Aplikasi: " + appNumber + ", status REJECTED")
              .channel(NotificationChannel.IN_APP)
              .build());

      if (applicantId != null) {
        systemNotificationService.saveNotification(
            SystemNotification.builder()
                .userId(applicantId)
                .notificationType(NotificationType.APPLICATION_UPDATE)
                .title("Aplikasi KPR Anda ditolak")
                .message("Nomor aplikasi: " + appNumber)
                .channel(NotificationChannel.IN_APP)
                .build());
      }
    } catch (Exception notifEx) {
      log.warn("Failed to save verifier rejection notifications: {}", notifEx.getMessage());
    }
    return updatedRows > 0;
  }

  // Helper to get current pending stage name for rejection message
  private String workflowStageSafe(Integer applicationId, Integer userID) {
    return approvalWorkflowRepository
        .findByApplicationIdAndStatus(applicationId, ApprovalWorkflow.WorkflowStatus.PENDING)
        .stream()
        .filter(w -> w.getAssignedTo().equals(userID))
        .findFirst()
        .map(w -> String.valueOf(w.getStage()))
        .orElse("UNKNOWN_STAGE");
  }

  /** Determine next application status based on current workflow stage */
  private ApplicationStatus determineNextApplicationStatus(
      ApprovalWorkflow.WorkflowStage currentStage) {
    if (currentStage == null) {
      throw new IllegalArgumentException("Current workflow stage is null");
    }

    switch (currentStage) {
      case DOCUMENT_VERIFICATION:
        return ApplicationStatus.PROPERTY_APPRAISAL;
      case PROPERTY_APPRAISAL:
        return ApplicationStatus.CREDIT_ANALYSIS;
      case CREDIT_ANALYSIS:
        return ApplicationStatus.FINAL_APPROVAL;
      case FINAL_APPROVAL:
        return ApplicationStatus.APPROVED;
      default:
        throw new IllegalArgumentException(
            "Unknown or unsupported workflow stage: " + currentStage);
    }
  }
}
