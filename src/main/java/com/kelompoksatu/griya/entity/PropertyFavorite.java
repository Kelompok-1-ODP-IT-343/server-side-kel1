package com.kelompoksatu.griya.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "property_favorites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyFavorite {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Integer userId;

  @Column(name = "property_id", nullable = false)
  private Integer propertyId;

  @Column(
      name = "created_at",
      nullable = false,
      updatable = false,
      columnDefinition = "timestamp default current_timestamp")
  private LocalDateTime createdAt;
}
