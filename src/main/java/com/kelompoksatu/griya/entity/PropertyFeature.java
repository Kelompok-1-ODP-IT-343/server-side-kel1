package com.kelompoksatu.griya.entity;

import com.kelompoksatu.griya.entity.converter.FeatureCategoryConverter;
import jakarta.persistence.*;
import lombok.*;

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
  @Convert(converter = FeatureCategoryConverter.class)
  @Column(name = "feature_category", nullable = false, columnDefinition = "feature_category")
  private FeatureCategory featureCategory;

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
