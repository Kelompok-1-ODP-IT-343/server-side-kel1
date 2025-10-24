package com.kelompoksatu.griya.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;

@Entity
@Table(name = "verification_token")
@Data
public class VerificationToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 128)
  private String token; // atau tokenHash (lihat catatan security)

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "used_at")
  private Instant usedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  public boolean isUsed() {
    return usedAt != null;
  }
}
