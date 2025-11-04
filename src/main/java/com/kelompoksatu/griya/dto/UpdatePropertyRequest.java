package com.kelompoksatu.griya.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePropertyRequest {
    private Integer developerId;
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
    private Integer viewCount;
    private Integer inquiryCount;
    private Integer favoriteCount;
}
