package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.ApiResponse;
import com.kelompoksatu.griya.service.TelegramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** Controller for Telegram bot integration Handles webhook endpoints and manual message sending */
@Slf4j
@RestController
@RequestMapping("/api/v1/telegram")
@RequiredArgsConstructor
@Tag(name = "Telegram", description = "Telegram Bot Integration API")
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramController {

  private final TelegramService telegramService;

  /** Send a manual message to Telegram (Admin only) */
  @PostMapping("/send-message")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Send message to Telegram",
      description = "Send a manual message to the configured Telegram chat")
  public ResponseEntity<ApiResponse<String>> sendMessage(
      @Valid @RequestBody SendMessageRequest request) {
    try {
      telegramService.sendMessage(request.getMessage());

      ApiResponse<String> response =
          new ApiResponse<>(true, "Message sent to Telegram successfully", "Message delivered");
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error sending Telegram message: {}", e.getMessage(), e);
      ApiResponse<String> response =
          new ApiResponse<>(false, "Failed to send message to Telegram: " + e.getMessage(), null);
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /** Send a formatted notification to Telegram (Admin only) */
  @PostMapping("/send-notification")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Send formatted notification",
      description = "Send a formatted notification to Telegram")
  public ResponseEntity<ApiResponse<String>> sendNotification(
      @Valid @RequestBody SendNotificationRequest request) {
    try {
      telegramService.sendFormattedMessage(
          request.getTitle(), request.getContent(), request.getLevel());

      ApiResponse<String> response =
          new ApiResponse<>(
              true, "Notification sent to Telegram successfully", "Notification delivered");
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error sending Telegram notification: {}", e.getMessage(), e);
      ApiResponse<String> response =
          new ApiResponse<>(
              false, "Failed to send notification to Telegram: " + e.getMessage(), null);
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /** Send KPR application notification (Internal use) */
  @PostMapping("/kpr-notification")
  @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
  @Operation(
      summary = "Send KPR notification",
      description = "Send KPR application status notification")
  public ResponseEntity<ApiResponse<String>> sendKprNotification(
      @Valid @RequestBody KprNotificationRequest request) {
    try {
      telegramService.sendKprNotification(
          request.getApplicationNumber(), request.getEvent(), request.getDetails());

      ApiResponse<String> response =
          new ApiResponse<>(true, "KPR notification sent successfully", "Notification delivered");
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error sending KPR notification: {}", e.getMessage(), e);
      ApiResponse<String> response =
          new ApiResponse<>(false, "Failed to send KPR notification: " + e.getMessage(), null);
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /**
   * Webhook endpoint for receiving Telegram updates This can be used if you want to handle incoming
   * messages from Telegram
   */
  @PostMapping("/webhook")
  @Operation(
      summary = "Telegram webhook",
      description = "Webhook endpoint for receiving Telegram updates")
  public ResponseEntity<String> webhook(@RequestBody String update) {
    try {
      log.info("Received Telegram webhook update: {}", update);

      // Here you can process incoming messages from Telegram
      // For example, handle commands, respond to messages, etc.

      return ResponseEntity.ok("OK");

    } catch (Exception e) {
      log.error("Error processing Telegram webhook: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().body("Error");
    }
  }

  /** Health check endpoint for Telegram integration */
  @GetMapping("/health")
  @Operation(
      summary = "Telegram health check",
      description = "Check if Telegram integration is working")
  public ResponseEntity<ApiResponse<String>> healthCheck() {
    try {
      // You could add a test message or API call here to verify connectivity
      ApiResponse<String> response =
          new ApiResponse<>(true, "Telegram integration is healthy", "Service is running");
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Telegram health check failed: {}", e.getMessage(), e);
      ApiResponse<String> response =
          new ApiResponse<>(
              false, "Telegram integration health check failed: " + e.getMessage(), null);
      return ResponseEntity.internalServerError().body(response);
    }
  }

  // ========================================
  // REQUEST DTOs
  // ========================================

  @Data
  public static class SendMessageRequest {
    @NotBlank(message = "Message cannot be blank")
    @Parameter(description = "Message to send to Telegram", required = true)
    private String message;
  }

  @Data
  public static class SendNotificationRequest {
    @NotBlank(message = "Title cannot be blank")
    @Parameter(description = "Notification title", required = true)
    private String title;

    @NotBlank(message = "Content cannot be blank")
    @Parameter(description = "Notification content", required = true)
    private String content;

    @Parameter(description = "Notification level (INFO, WARN, ERROR, SUCCESS)", example = "INFO")
    private String level = "INFO";
  }

  @Data
  public static class KprNotificationRequest {
    @NotBlank(message = "Application number cannot be blank")
    @Parameter(description = "KPR application number", required = true)
    private String applicationNumber;

    @NotBlank(message = "Event cannot be blank")
    @Parameter(description = "Event type (SUBMITTED, APPROVED, REJECTED, etc.)", required = true)
    private String event;

    @Parameter(description = "Additional details about the event")
    private String details;
  }
}
