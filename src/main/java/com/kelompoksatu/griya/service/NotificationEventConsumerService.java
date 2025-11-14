package com.kelompoksatu.griya.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kelompoksatu.griya.entity.NotificationStatus;
import com.kelompoksatu.griya.entity.SystemNotification;
import com.kelompoksatu.griya.entity.User;
import com.kelompoksatu.griya.repository.SystemNotificationRepository;
import com.kelompoksatu.griya.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumerService {

  private final RabbitMQService rabbitMQService;
  private final WhatsAppService whatsAppService;
  private final UserRepository userRepository;
  private final SystemNotificationRepository notificationRepository;

  @Value("${app.rabbitmq.notificationQueue:notifications.events}")
  private String notificationQueue;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private String consumerTag;

  @PostConstruct
  public void start() {
    try {
      consumerTag = rabbitMQService.subscribe(notificationQueue, this::onMessage);
    } catch (Exception e) {
      log.warn("Failed to subscribe notification consumer: {}", e.getMessage());
    }
  }

  @PreDestroy
  public void stop() {
    if (consumerTag != null) {
      try {
        rabbitMQService.unsubscribe(consumerTag);
      } catch (Exception e) {
        log.warn("Failed to unsubscribe notification consumer: {}", e.getMessage());
      }
    }
  }

  private void onMessage(String body) {
    try {
      JsonNode node = objectMapper.readTree(body);
      String event = text(node, "event");
      if (!"SYSTEM_NOTIFICATION".equalsIgnoreCase(event)) {
        return;
      }

      long notificationId = longValue(node, "notificationId");
      int userId = intValue(node, "userId");
      String title = text(node, "title");
      String message = text(node, "message");
      String channel = text(node, "channel");

      if (channel == null || !"WHATSAPP".equalsIgnoreCase(channel)) {
        return;
      }

      Optional<User> userOpt = userRepository.findById(userId);
      if (userOpt.isEmpty()) {
        updateStatus(notificationId, NotificationStatus.FAILED, null);
        return;
      }
      String phone = userOpt.get().getPhone();
      if (phone == null || phone.isBlank()) {
        updateStatus(notificationId, NotificationStatus.FAILED, null);
        return;
      }

      String combined = (title != null && !title.isBlank()) ? (title + "\n\n" + message) : message;
      boolean sent = whatsAppService.sendMessage(phone, combined, "notif", null);
      if (sent) {
        updateStatus(notificationId, NotificationStatus.SENT, LocalDateTime.now());
      } else {
        updateStatus(notificationId, NotificationStatus.FAILED, null);
      }
    } catch (Exception e) {
      try {
        JsonNode node = objectMapper.readTree(body);
        long notificationId = longValue(node, "notificationId");
        updateStatus(notificationId, NotificationStatus.FAILED, null);
      } catch (Exception ignore) {
      }
    }
  }

  private String text(JsonNode node, String field) {
    JsonNode v = node.get(field);
    return v != null && !v.isNull() ? v.asText() : null;
  }

  private int intValue(JsonNode node, String field) {
    JsonNode v = node.get(field);
    return v != null && v.isInt() ? v.intValue() : v != null ? v.asInt() : 0;
  }

  private long longValue(JsonNode node, String field) {
    JsonNode v = node.get(field);
    return v != null && v.isLong() ? v.longValue() : v != null ? v.asLong() : 0L;
  }

  private void updateStatus(long id, NotificationStatus status, LocalDateTime sentAt) {
    if (id <= 0) return;
    try {
      Optional<SystemNotification> opt = notificationRepository.findById(id);
      if (opt.isPresent()) {
        SystemNotification n = opt.get();
        n.setStatus(status);
        if (sentAt != null) {
          n.setSentAt(sentAt);
        }
        notificationRepository.save(n);
      }
    } catch (Exception e) {
    }
  }
}
