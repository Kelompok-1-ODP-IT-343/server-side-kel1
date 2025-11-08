package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.entity.SystemNotification;
import com.kelompoksatu.griya.entity.NotificationStatus;
import com.kelompoksatu.griya.repository.SystemNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemNotificationService {

    private final SystemNotificationRepository notificationRepository;

    /**
     * Menyimpan notifikasi baru ke database.
     */
    public SystemNotification saveNotification(SystemNotification notification) {
        log.info("SERVICE: Saving new notification with title: {}", notification.getTitle());

        if (notification.getStatus() == null) {
            notification.setStatus(NotificationStatus.PENDING);
        }

        SystemNotification savedNotification = notificationRepository.save(notification);
        log.info("SERVICE: Notification ID {} successfully saved.", savedNotification.getId());
        return savedNotification;
    }

    /**
     * Mengambil semua notifikasi untuk user tertentu.
     */
    public List<SystemNotification> getNotificationsByUserId(Integer userId) {
        log.debug("SERVICE: Fetching notifications for user ID: {}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Menandai notifikasi sebagai sudah dibaca (read).
     */
    public SystemNotification markAsRead(Long notificationId) {
        log.info("SERVICE: Attempting to mark notification ID {} as read.", notificationId);

        Optional<SystemNotification> optionalNotification = notificationRepository.findById(notificationId);

        if (optionalNotification.isPresent()) {
            SystemNotification notification = optionalNotification.get();
            if (notification.getReadAt() == null) {
                notification.setReadAt(LocalDateTime.now());

                SystemNotification updatedNotification = notificationRepository.save(notification);
                log.info("SERVICE: Notification ID {} successfully marked as read.", notificationId);
                return updatedNotification;
            }
            log.warn("SERVICE: Notification ID {} was already read (Skipped update).", notificationId);
            return notification;
        } else {
            log.error("SERVICE ERROR: Notification not found with ID: {}", notificationId);
            throw new RuntimeException("Notification not found with ID: " + notificationId);
        }
    }
}