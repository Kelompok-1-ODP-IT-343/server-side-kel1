package com.kelompoksatu.griya.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePropertyResponse {
    private Integer id;
    private Integer developerId;
    private String developerName;
    private String propertyType;
    private String listingType;
    private String title;
    private String description;
    private String address;
    private String city;
    private String province;
    private String postalCode;
    private String district;
    private String village;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal landArea;
    private BigDecimal buildingArea;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer floors;
    private Integer garage;
    private Integer yearBuilt;
    private BigDecimal price;
    private BigDecimal pricePerSqm;
    private BigDecimal maintenanceFee;
    private String certificateType;
    private String certificateNumber;
    private BigDecimal certificateArea;
    private BigDecimal pbbValue;
    private String status;
    private LocalDate availabilityDate;
    private LocalDate handoverDate;
    private Boolean isFeatured;
    private Boolean isKprEligible;
    private BigDecimal minDownPaymentPercent;
    private Integer maxLoanTermYears;
    private String metaTitle;
    private String metaDescription;
    private String keywords;
    private LocalDate updatedAt;

    // Nested data kalau nanti mau extend (biar gak ilang)
    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureData {
        private String featureName;
        private String featureValue;
        private String featureCategory;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationData {
        private String poiName;
        private Double distanceKm;
        private String poiType;
        }
}
