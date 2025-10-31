package com.kelompoksatu.griya.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePropertyResponse {
  private Integer id;
  private String title;
  private String description;
  private BigDecimal price;
  private Integer bedrooms;
  private Integer bathrooms;
  private String status;
  private String propertyType;
  private String city;
  private String developerName; // <- akan diisi companyName dari Developer
  private String address;

  @Builder
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FeatureData {
    private String featureName;
    private String featureValue;
  }

  @Builder
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LocationData {
    private String poiName;
    private Double distanceKm;
  }

  private List<FeatureData> features;
  private List<LocationData> locations;
}
