package com.kelompoksatu.griya.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "system_notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemNotification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id")
  private Integer userId;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM) // <-- Menggunakan Type Mapping dari PropertyImage.java
  @Column(name = "notification_type", nullable = false, columnDefinition = "notification_type")
  private NotificationType notificationType;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String message;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM) // <-- Menggunakan Type Mapping dari PropertyImage.java
  @Column(nullable = false, columnDefinition = "notification_channel")
  private NotificationChannel channel;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM) // <-- Menggunakan Type Mapping dari PropertyImage.java
  @Column(name = "status", columnDefinition = "notification_status")
  @Builder.Default
  private NotificationStatus status = NotificationStatus.PENDING;

  @Column(name = "scheduled_at")
  private LocalDateTime scheduledAt;

  @Column(name = "sent_at")
  private LocalDateTime sentAt;

  @Column(name = "delivered_at")
  private LocalDateTime deliveredAt;

  @Column(name = "read_at")
  private LocalDateTime readAt;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private JsonNode metadata;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}
