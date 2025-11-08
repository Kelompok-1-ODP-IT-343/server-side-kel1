package com.kelompoksatu.griya.entity;

// Import Jakarta Persistence
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Import Lombok, Jackson
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Import Hibernate
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode; // <-- PENTING
import org.hibernate.type.SqlTypes;          // <-- PENTING

// Import Java Time
import java.time.LocalDateTime;

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

    // MENGGANTI CARA MAPPING ENUM
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

    // Mapping tipe JSONB PostgreSQL
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode metadata;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}