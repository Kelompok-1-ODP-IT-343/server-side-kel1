package com.kelompoksatu.griya.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Yearly rate configuration per tenor/year for KPR rates */
@Entity
@Table(name = "kpr_rate_yearly")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KprRateYearly {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "kpr_rate_id", nullable = false)
  private KprRate kprRate;

  @Column(name = "tenor", nullable = false)
  private Integer tenor;

  @Column(name = "year", nullable = false)
  private Integer year;

  @Column(name = "rate", precision = 5, scale = 4, nullable = false)
  private BigDecimal rate;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
