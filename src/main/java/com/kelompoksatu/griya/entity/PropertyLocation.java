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
@Table(name = "property_locations")
public class PropertyLocation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "poi_name", nullable = false)
  private String poiName;

  @Column(name = "distance_km", nullable = false)
  private Double distanceKm;

  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  @Column(name = "poi_type", nullable = false)
  private PropertyLocationType poiType = PropertyLocationType.OFFICE; // default biar gak null

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "property_id", nullable = false)
  private Property property;

  public enum PropertyLocationType {
    SCHOOL,
    HOSPITAL,
    MALL,
    BANK,
    MOSQUE,
    CHURCH,
    PARK,
    TRANSPORT,
    OFFICE
  }
}
