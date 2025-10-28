package com.kelompoksatu.griya.service;

import lombok.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Service class for handling application notifications Integrates with TelegramService to send
 * notifications for various events
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationService {

  private final TelegramService telegramService;

  /**
   * Send notification when a new user registers
   *
   * @param username The username of the new user
   * @param email The email of the new user
   */
  public void sendUserRegistrationNotification(String username, String email) {
    try {
      String message =
          String.format(
              "🎉 <b>New User Registration</b>\n\n"
                  + "Username: <code>%s</code>\n"
                  + "Email: <code>%s</code>\n"
                  + "Time: <i>%s</i>",
              username, email, java.time.LocalDateTime.now().toString());

      telegramService.sendMessage(message);
      log.info("User registration notification sent for user: {}", username);

    } catch (Exception e) {
      log.error("Failed to send user registration notification: {}", e.getMessage(), e);
    }
  }

  /**
   * Send notification when a KPR application is submitted
   *
   * @param applicationNumber The KPR application number
   * @param userName The name of the applicant
   * @param loanAmount The loan amount requested
   */
  public void sendKprApplicationSubmittedNotification(
      String applicationNumber, String userName, String loanAmount) {
    try {
      String details =
          String.format(
              "Applicant: <b>%s</b>\n"
                  + "Loan Amount: <code>Rp %s</code>\n"
                  + "Status: <i>Submitted for Review</i>",
              userName, loanAmount);

      telegramService.sendKprNotification(applicationNumber, "SUBMITTED", details);
      log.info("KPR application submitted notification sent for: {}", applicationNumber);

    } catch (Exception e) {
      log.error("Failed to send KPR application submitted notification: {}", e.getMessage(), e);
    }
  }

  /**
   * Send notification when a KPR application is approved
   *
   * @param applicationNumber The KPR application number
   * @param userName The name of the applicant
   * @param approvedBy The name of the approver
   */
  public void sendKprApplicationApprovedNotification(
      String applicationNumber, String userName, String approvedBy) {
    try {
      String details =
          String.format(
              "Applicant: <b>%s</b>\n"
                  + "Approved by: <code>%s</code>\n"
                  + "Status: <i>✅ APPROVED</i>",
              userName, approvedBy);

      telegramService.sendKprNotification(applicationNumber, "APPROVED", details);
      log.info("KPR application approved notification sent for: {}", applicationNumber);

    } catch (Exception e) {
      log.error("Failed to send KPR application approved notification: {}", e.getMessage(), e);
    }
  }

  /**
   * Send notification when a KPR application is rejected
   *
   * @param applicationNumber The KPR application number
   * @param userName The name of the applicant
   * @param rejectionReason The reason for rejection
   */
  public void sendKprApplicationRejectedNotification(
      String applicationNumber, String userName, String rejectionReason) {
    try {
      String details =
          String.format(
              "Applicant: <b>%s</b>\n" + "Reason: <code>%s</code>\n" + "Status: <i>❌ REJECTED</i>",
              userName, rejectionReason);

      telegramService.sendKprNotification(applicationNumber, "REJECTED", details);
      log.info("KPR application rejected notification sent for: {}", applicationNumber);

    } catch (Exception e) {
      log.error("Failed to send KPR application rejected notification: {}", e.getMessage(), e);
    }
  }

  /**
   * Send notification for system errors
   *
   * @param errorMessage The error message
   * @param context Additional context about the error
   */
  public void sendSystemErrorNotification(String errorMessage, String context) {
    try {
      telegramService.sendErrorNotification(errorMessage, context);
      log.info("System error notification sent");

    } catch (Exception e) {
      log.error("Failed to send system error notification: {}", e.getMessage(), e);
    }
  }

  /**
   * Send notification when email verification fails
   *
   * @param email The email that failed verification
   * @param reason The reason for failure
   */
  public void sendEmailVerificationFailedNotification(String email, String reason) {
    try {
      String message =
          String.format(
              "⚠️ <b>Email Verification Failed</b>\n\n"
                  + "Email: <code>%s</code>\n"
                  + "Reason: <i>%s</i>\n"
                  + "Time: <i>%s</i>",
              email, reason, java.time.LocalDateTime.now().toString());

      telegramService.sendMessage(message);
      log.info("Email verification failed notification sent for: {}", email);

    } catch (Exception e) {
      log.error("Failed to send email verification failed notification: {}", e.getMessage(), e);
    }
  }

  /**
   * Send daily summary notification
   *
   * @param newUsers Number of new users registered today
   * @param newApplications Number of new KPR applications today
   * @param approvedApplications Number of approved applications today
   */
  public void sendDailySummaryNotification(
      int newUsers, int newApplications, int approvedApplications) {
    try {
      String message =
          String.format(
              "📊 <b>Daily Summary Report</b>\n\n"
                  + "New Users: <code>%d</code>\n"
                  + "New KPR Applications: <code>%d</code>\n"
                  + "Approved Applications: <code>%d</code>\n"
                  + "Date: <i>%s</i>",
              newUsers,
              newApplications,
              approvedApplications,
              java.time.LocalDate.now().toString());

      telegramService.sendFormattedMessage("Daily Summary", message, "INFO");
      log.info("Daily summary notification sent");

    } catch (Exception e) {
      log.error("Failed to send daily summary notification: {}", e.getMessage(), e);
    }
  }

  /**
   * Send notification when an endpoint encounters an error for monitoring purposes
   *
   * @param endpoint The endpoint that encountered the error
   * @param httpMethod The HTTP method (GET, POST, PUT, DELETE, etc.)
   * @param statusCode The HTTP status code returned
   * @param errorMessage The error message
   * @param userAgent The user agent of the request (optional)
   * @param ipAddress The IP address of the request (optional)
   */
  public void sendEndpointErrorNotification(
      String endpoint,
      String httpMethod,
      int statusCode,
      String errorMessage,
      String userAgent,
      String ipAddress) {
    try {
      String message =
          String.format(
              "🚨 <b>Endpoint Error Alert</b>\n\n"
                  + "Endpoint: <code>%s %s</code>\n"
                  + "Status Code: <code>%d</code>\n"
                  + "Error: <i>%s</i>\n"
                  + "IP Address: <code>%s</code>\n"
                  + "User Agent: <code>%s</code>\n"
                  + "Time: <i>%s</i>",
              httpMethod,
              endpoint,
              statusCode,
              errorMessage != null ? errorMessage : "Unknown error",
              ipAddress != null ? ipAddress : "Unknown",
              userAgent != null
                  ? (userAgent.length() > 50 ? userAgent.substring(0, 50) + "..." : userAgent)
                  : "Unknown",
              java.time.LocalDateTime.now().toString());

      telegramService.sendFormattedMessage("Endpoint Error", message, "ERROR");
      log.info("Endpoint error notification sent for: {} {}", httpMethod, endpoint);

    } catch (Exception e) {
      log.error("Failed to send endpoint error notification: {}", e.getMessage(), e);
    }
  }

  /**
   * Send notification when an endpoint encounters an error (simplified version)
   *
   * @param endpoint The endpoint that encountered the error
   * @param httpMethod The HTTP method (GET, POST, PUT, DELETE, etc.)
   * @param statusCode The HTTP status code returned
   * @param errorMessage The error message
   */
  public void sendEndpointErrorNotification(
      String endpoint, String httpMethod, int statusCode, String errorMessage) {
    sendEndpointErrorNotification(endpoint, httpMethod, statusCode, errorMessage, null, null);
  }
}
