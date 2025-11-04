package com.kelompoksatu.griya.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "property_features")
public class PropertyFeature {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  @Column(name = "feature_category", nullable = false)
  private FeatureCategory featureCategory; //

  @Column(name = "feature_name", nullable = false)
  private String featureName;

  @Column(name = "feature_value", nullable = false)
  private String featureValue;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "property_id", nullable = false)
  private Property property;

  public enum FeatureCategory {
    AMENITIES,
    INTERIOR,
    EXTERIOR,
    LOCATION,
    SECURITY,
    UTILITIES
  }
}
