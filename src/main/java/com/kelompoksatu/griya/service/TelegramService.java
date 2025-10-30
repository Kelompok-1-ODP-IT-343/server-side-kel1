package com.kelompoksatu.griya.service;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/** Service class for Telegram bot integration Handles sending messages to Telegram chat */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "telegram.bot.enabled", havingValue = "true", matchIfMissing = true)
public class TelegramService {

  @Value("${telegram.bot.token}")
  private String botToken;

  @Value("${telegram.bot.chat-id}")
  private String chatId;

  @Value("${telegram.bot.api-url}")
  private String apiUrl;

  @Value("${spring.profiles.active:development}")
  private String activeProfile;

  private final RestTemplate restTemplate;

  /**
   * Send a message to the configured Telegram chat Only sends messages in development or production
   * environments
   *
   * @param message The message to send
   */
  public void sendMessage(String message) {
    if (!isEnvironmentEnabled()) {
      log.debug("Telegram messaging disabled for environment: {}", activeProfile);
      return;
    }

    if (botToken == null || botToken.equals("your-bot-token-here")) {
      log.warn("Telegram bot token not configured properly");
      return;
    }

    try {
      String telegramApiUrl = apiUrl + botToken + "/sendMessage";

      // Prepare request body
      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("chat_id", chatId);
      requestBody.put("text", message);
      requestBody.put("parse_mode", "HTML"); // Enable HTML formatting

      // Set headers
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);

      // Create request entity
      HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

      // Send request
      ResponseEntity<String> response =
          restTemplate.exchange(telegramApiUrl, HttpMethod.POST, requestEntity, String.class);

      if (response.getStatusCode().is2xxSuccessful()) {
        log.info("Telegram message sent successfully. Status: {}", response.getStatusCode());
      } else {
        log.warn("Telegram message sending failed. Status: {}", response.getStatusCode());
      }

    } catch (Exception e) {
      log.error("Error sending Telegram message: {}", e.getMessage(), e);
    }
  }

  /**
   * Send a formatted message with application context
   *
   * @param title The message title
   * @param content The message content
   * @param level The message level (INFO, WARN, ERROR)
   */
  public void sendFormattedMessage(String title, String content, String level) {
    String emoji = getEmojiForLevel(level);
    String formattedMessage =
        String.format(
            "%s <b>%s</b>\n\n%s\n\n<i>Environment: %s</i>", emoji, title, content, activeProfile);
    sendMessage(formattedMessage);
  }

  /**
   * Send notification for KPR application events
   *
   * @param applicationNumber The KPR application number
   * @param event The event type (SUBMITTED, APPROVED, REJECTED, etc.)
   * @param details Additional details
   */
  public void sendKprNotification(String applicationNumber, String event, String details) {
    String title = String.format("KPR Application %s", event);
    String content =
        String.format("Application Number: <code>%s</code>\n%s", applicationNumber, details);
    sendFormattedMessage(title, content, "INFO");
  }

  /**
   * Send error notification
   *
   * @param error The error message
   * @param context Additional context information
   */
  public void sendErrorNotification(String error, String context) {
    String title = "Application Error";
    String content = String.format("Error: <code>%s</code>\nContext: %s", error, context);
    sendFormattedMessage(title, content, "ERROR");
  }

  /**
   * Check if the current environment allows Telegram messaging
   *
   * @return true if messaging is enabled for current environment
   */
  private boolean isEnvironmentEnabled() {
    return "development".equals(activeProfile) || "production".equals(activeProfile);
  }

  /**
   * Get emoji based on message level
   *
   * @param level The message level
   * @return Appropriate emoji
   */
  private String getEmojiForLevel(String level) {
    return switch (level.toUpperCase()) {
      case "ERROR" -> "🚨";
      case "WARN" -> "⚠️";
      case "SUCCESS" -> "✅";
      default -> "ℹ️";
    };
  }
}
