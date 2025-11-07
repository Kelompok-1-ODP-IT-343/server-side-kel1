package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.Property;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** DTO for creating a new property based on the provided API specification */
@Setter
@Getter
public class CreatePropertyRequest {

  // Getters and Setters
  @NotBlank(message = "Property code is required")
  @Size(max = 20, message = "Property code must not exceed 20 characters")
  private String propertyCode;

  @NotNull(message = "Developer ID is required") private Integer developerId;

  @NotNull(message = "Property type is required") private Property.PropertyType propertyType;

  @NotNull(message = "Listing type is required") private Property.ListingType listingType;

  @NotBlank(message = "Title is required")
  @Size(max = 255, message = "Title must not exceed 255 characters")
  private String title;

  @NotBlank(message = "Description is required")
  private String description;

  // Location Details
  @NotBlank(message = "Address is required")
  private String address;

  @NotBlank(message = "City is required")
  @Size(max = 100, message = "City must not exceed 100 characters")
  private String city;

  @NotBlank(message = "Province is required")
  @Size(max = 100, message = "Province must not exceed 100 characters")
  private String province;

  @NotBlank(message = "Postal code is required")
  @Size(max = 10, message = "Postal code must not exceed 10 characters")
  private String postalCode;

  @NotBlank(message = "District is required")
  @Size(max = 100, message = "District must not exceed 100 characters")
  private String district;

  @NotBlank(message = "Village is required")
  @Size(max = 100, message = "Village must not exceed 100 characters")
  private String village;

  @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
  @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
  private BigDecimal latitude;

  @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
  @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
  private BigDecimal longitude;

  // Property Specifications
  @NotNull(message = "Land area is required") @DecimalMin(value = "0.01", message = "Land area must be greater than 0")
  private BigDecimal landArea;

  @NotNull(message = "Building area is required") @DecimalMin(value = "0.01", message = "Building area must be greater than 0")
  private BigDecimal buildingArea;

  @NotNull(message = "Number of bedrooms is required") @Min(value = 0, message = "Number of bedrooms must be non-negative")
  private Integer bedrooms;

  @NotNull(message = "Number of bathrooms is required") @Min(value = 0, message = "Number of bathrooms must be non-negative")
  private Integer bathrooms;

  @Min(value = 1, message = "Number of floors must be at least 1")
  private Integer floors = 1;

  @Min(value = 0, message = "Number of garage spaces must be non-negative")
  private Integer garage = 0;

  @Min(value = 1900, message = "Year built must be after 1900")
  @Max(value = 2100, message = "Year built must be before 2100")
  private Integer yearBuilt;

  // Pricing
  @NotNull(message = "Price is required") @DecimalMin(value = "0.01", message = "Price must be greater than 0")
  private BigDecimal price;

  @NotNull(message = "Price per sqm is required") @DecimalMin(value = "0.01", message = "Price per sqm must be greater than 0")
  private BigDecimal pricePerSqm;

  @DecimalMin(value = "0.00", message = "Maintenance fee must be non-negative")
  private BigDecimal maintenanceFee = BigDecimal.ZERO;

  // Legal & Certificates
  @NotNull(message = "Certificate type is required") private Property.CertificateType certificateType;

  @Size(max = 100, message = "Certificate number must not exceed 100 characters")
  private String certificateNumber;

  @DecimalMin(value = "0.01", message = "Certificate area must be greater than 0")
  private BigDecimal certificateArea;

  @DecimalMin(value = "0.00", message = "PBB value must be non-negative")
  private BigDecimal pbbValue;

  // Availability
  private Property.PropertyStatus status = Property.PropertyStatus.AVAILABLE;

  private LocalDate availabilityDate;

  private LocalDate handoverDate;

  // Marketing
  private Boolean isFeatured = false;

  private Boolean isKprEligible = true;

  @DecimalMin(value = "0.00", message = "Min down payment percent must be non-negative")
  @DecimalMax(value = "100.00", message = "Min down payment percent must not exceed 100")
  private BigDecimal minDownPaymentPercent = new BigDecimal("20.00");

  @Min(value = 1, message = "Max loan term years must be at least 1")
  @Max(value = 50, message = "Max loan term years must not exceed 50")
  private Integer maxLoanTermYears = 20;

  // SEO & Marketing
  @NotBlank(message = "Slug is required")
  @Size(max = 255, message = "Slug must not exceed 255 characters")
  private String slug;

  @Size(max = 255, message = "Meta title must not exceed 255 characters")
  private String metaTitle;

  private String metaDescription;

  private String keywords;

  // Tracking
  @Min(value = 0, message = "View count must be non-negative")
  private Integer viewCount = 0;

  @Min(value = 0, message = "Inquiry count must be non-negative")
  private Integer inquiryCount = 0;

  @Min(value = 0, message = "Favorite count must be non-negative")
  private Integer favoriteCount = 0;

  // Default constructor
  public CreatePropertyRequest() {}
}
