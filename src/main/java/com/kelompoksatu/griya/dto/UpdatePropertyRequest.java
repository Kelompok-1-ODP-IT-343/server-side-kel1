package com.kelompoksatu.griya.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePropertyRequest {
  private String title;
  private String description;
  private BigDecimal price;
  private Integer bedrooms;
  private Integer bathrooms;
  private String status; // AVAILABLE/RESERVED/SOLD/...
   // or enum kalau kamu mau mapping langsung



  @Builder
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor

  public static class FeatureReq {
    private String featureName;
    private String featureValue;
    private String featureCategory;
  }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocationReq {
        private String poiName;
        private Double distanceKm;
        private String poiType;
    }




  private List<FeatureReq> features;
  private List<LocationReq> locations;
}
