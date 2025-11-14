package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.entity.NotificationStatus;
import com.kelompoksatu.griya.entity.SystemNotification;
import com.kelompoksatu.griya.repository.SystemNotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemNotificationService {

  private final SystemNotificationRepository notificationRepository;
  private final RabbitMQService rabbitMQService;

  @Value("${app.rabbitmq.notificationQueue:notifications.events}")
  private String notificationQueue;

  /** Menyimpan notifikasi baru ke database. */
  public SystemNotification saveNotification(SystemNotification notification) {
    log.info("SERVICE: Saving new notification with title: {}", notification.getTitle());

    if (notification.getStatus() == null) {
      notification.setStatus(NotificationStatus.PENDING);
    }

    SystemNotification savedNotification = notificationRepository.save(notification);
    log.info("SERVICE: Notification ID {} successfully saved.", savedNotification.getId());

    // Publish event to RabbitMQ (non-blocking best-effort)
    try {
      publishEvent(savedNotification);
      log.info(
          "SERVICE: Published notification event to queue '{}' with id {}",
          notificationQueue,
          savedNotification.getId());
    } catch (Exception ex) {
      // Do not fail business flow if messaging fails; log safely
      log.warn("SERVICE: Failed to publish notification event: {}", ex.getMessage());
    }
    return savedNotification;
  }

  /** Mengambil semua notifikasi untuk user tertentu. */
  public List<SystemNotification> getNotificationsByUserId(Integer userId) {
    log.debug("SERVICE: Fetching notifications for user ID: {}", userId);
    return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
  }

  /** Menandai notifikasi sebagai sudah dibaca (read). */
  public SystemNotification markAsRead(Long notificationId) {
    log.info("SERVICE: Attempting to mark notification ID {} as read.", notificationId);

    Optional<SystemNotification> optionalNotification =
        notificationRepository.findById(notificationId);

    if (optionalNotification.isPresent()) {
      SystemNotification notification = optionalNotification.get();
      if (notification.getReadAt() == null) {
        notification.setReadAt(LocalDateTime.now());

        SystemNotification updatedNotification = notificationRepository.save(notification);
        log.info("SERVICE: Notification ID {} successfully marked as read.", notificationId);

        // Publish a read event (optional, useful for analytics)
        try {
          publishEvent(updatedNotification);
        } catch (Exception ex) {
          log.warn("SERVICE: Failed to publish notification read event: {}", ex.getMessage());
        }
        return updatedNotification;
      }
      log.warn("SERVICE: Notification ID {} was already read (Skipped update).", notificationId);
      return notification;
    } else {
      log.error("SERVICE ERROR: Notification not found with ID: {}", notificationId);
      throw new RuntimeException("Notification not found with ID: " + notificationId);
    }
  }

  // =============================
  // RabbitMQ Event Publishing
  // =============================
  private void publishEvent(SystemNotification n) {
    if (n == null) return;
    String type = (n.getNotificationType() != null) ? n.getNotificationType().name() : "UNKNOWN";
    String channel = (n.getChannel() != null) ? n.getChannel().name() : "UNKNOWN";
    String status = (n.getStatus() != null) ? n.getStatus().name() : "PENDING";
    String createdAt =
        (n.getCreatedAt() != null) ? n.getCreatedAt().toString() : LocalDateTime.now().toString();

    // Minimal JSON payload without external dependencies
    String payload =
        "{"
            + "\"event\":\"SYSTEM_NOTIFICATION\","
            + "\"notificationId\":"
            + n.getId()
            + ","
            + "\"userId\":"
            + n.getUserId()
            + ","
            + "\"type\":\""
            + type
            + "\","
            + "\"channel\":\""
            + channel
            + "\","
            + "\"title\":\""
            + escape(n.getTitle())
            + "\","
            + "\"message\":\""
            + escape(n.getMessage())
            + "\","
            + "\"status\":\""
            + status
            + "\","
            + "\"createdAt\":\""
            + createdAt
            + "\"}";

    rabbitMQService.publishToQueue(notificationQueue, payload);
  }

  private String escape(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
  }
}
