package com.kelompoksatu.griya.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Session management for security tracking
 */
@Entity
@Table(name = "user_sessions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSession {
    
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "ip_address", length = 45, nullable = false)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT", nullable = false)
    private String userAgent;
    
    @Column(name = "payload", columnDefinition = "TEXT", nullable = false)
    private String payload;
    
    @Column(name = "last_activity", nullable = false)
    private LocalDateTime lastActivity;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_valid", nullable = false)
    private boolean isValid = false;
    
    // Relationship with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}