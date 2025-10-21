package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.Property;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** DTO for creating a new property based on the provided API specification */
public class CreatePropertyRequest {

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

  // Getters and Setters
  public String getPropertyCode() {
    return propertyCode;
  }

  public void setPropertyCode(String propertyCode) {
    this.propertyCode = propertyCode;
  }

  public Integer getDeveloperId() {
    return developerId;
  }

  public void setDeveloperId(Integer developerId) {
    this.developerId = developerId;
  }

  public Property.PropertyType getPropertyType() {
    return propertyType;
  }

  public void setPropertyType(Property.PropertyType propertyType) {
    this.propertyType = propertyType;
  }

  public Property.ListingType getListingType() {
    return listingType;
  }

  public void setListingType(Property.ListingType listingType) {
    this.listingType = listingType;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public String getProvince() {
    return province;
  }

  public void setProvince(String province) {
    this.province = province;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public String getDistrict() {
    return district;
  }

  public void setDistrict(String district) {
    this.district = district;
  }

  public String getVillage() {
    return village;
  }

  public void setVillage(String village) {
    this.village = village;
  }

  public BigDecimal getLatitude() {
    return latitude;
  }

  public void setLatitude(BigDecimal latitude) {
    this.latitude = latitude;
  }

  public BigDecimal getLongitude() {
    return longitude;
  }

  public void setLongitude(BigDecimal longitude) {
    this.longitude = longitude;
  }

  public BigDecimal getLandArea() {
    return landArea;
  }

  public void setLandArea(BigDecimal landArea) {
    this.landArea = landArea;
  }

  public BigDecimal getBuildingArea() {
    return buildingArea;
  }

  public void setBuildingArea(BigDecimal buildingArea) {
    this.buildingArea = buildingArea;
  }

  public Integer getBedrooms() {
    return bedrooms;
  }

  public void setBedrooms(Integer bedrooms) {
    this.bedrooms = bedrooms;
  }

  public Integer getBathrooms() {
    return bathrooms;
  }

  public void setBathrooms(Integer bathrooms) {
    this.bathrooms = bathrooms;
  }

  public Integer getFloors() {
    return floors;
  }

  public void setFloors(Integer floors) {
    this.floors = floors;
  }

  public Integer getGarage() {
    return garage;
  }

  public void setGarage(Integer garage) {
    this.garage = garage;
  }

  public Integer getYearBuilt() {
    return yearBuilt;
  }

  public void setYearBuilt(Integer yearBuilt) {
    this.yearBuilt = yearBuilt;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public BigDecimal getPricePerSqm() {
    return pricePerSqm;
  }

  public void setPricePerSqm(BigDecimal pricePerSqm) {
    this.pricePerSqm = pricePerSqm;
  }

  public BigDecimal getMaintenanceFee() {
    return maintenanceFee;
  }

  public void setMaintenanceFee(BigDecimal maintenanceFee) {
    this.maintenanceFee = maintenanceFee;
  }

  public Property.CertificateType getCertificateType() {
    return certificateType;
  }

  public void setCertificateType(Property.CertificateType certificateType) {
    this.certificateType = certificateType;
  }

  public String getCertificateNumber() {
    return certificateNumber;
  }

  public void setCertificateNumber(String certificateNumber) {
    this.certificateNumber = certificateNumber;
  }

  public BigDecimal getCertificateArea() {
    return certificateArea;
  }

  public void setCertificateArea(BigDecimal certificateArea) {
    this.certificateArea = certificateArea;
  }

  public BigDecimal getPbbValue() {
    return pbbValue;
  }

  public void setPbbValue(BigDecimal pbbValue) {
    this.pbbValue = pbbValue;
  }

  public Property.PropertyStatus getStatus() {
    return status;
  }

  public void setStatus(Property.PropertyStatus status) {
    this.status = status;
  }

  public LocalDate getAvailabilityDate() {
    return availabilityDate;
  }

  public void setAvailabilityDate(LocalDate availabilityDate) {
    this.availabilityDate = availabilityDate;
  }

  public LocalDate getHandoverDate() {
    return handoverDate;
  }

  public void setHandoverDate(LocalDate handoverDate) {
    this.handoverDate = handoverDate;
  }

  public Boolean getIsFeatured() {
    return isFeatured;
  }

  public void setIsFeatured(Boolean isFeatured) {
    this.isFeatured = isFeatured;
  }

  public Boolean getIsKprEligible() {
    return isKprEligible;
  }

  public void setIsKprEligible(Boolean isKprEligible) {
    this.isKprEligible = isKprEligible;
  }

  public BigDecimal getMinDownPaymentPercent() {
    return minDownPaymentPercent;
  }

  public void setMinDownPaymentPercent(BigDecimal minDownPaymentPercent) {
    this.minDownPaymentPercent = minDownPaymentPercent;
  }

  public Integer getMaxLoanTermYears() {
    return maxLoanTermYears;
  }

  public void setMaxLoanTermYears(Integer maxLoanTermYears) {
    this.maxLoanTermYears = maxLoanTermYears;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public String getMetaTitle() {
    return metaTitle;
  }

  public void setMetaTitle(String metaTitle) {
    this.metaTitle = metaTitle;
  }

  public String getMetaDescription() {
    return metaDescription;
  }

  public void setMetaDescription(String metaDescription) {
    this.metaDescription = metaDescription;
  }

  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  public Integer getViewCount() {
    return viewCount;
  }

  public void setViewCount(Integer viewCount) {
    this.viewCount = viewCount;
  }

  public Integer getInquiryCount() {
    return inquiryCount;
  }

  public void setInquiryCount(Integer inquiryCount) {
    this.inquiryCount = inquiryCount;
  }

  public Integer getFavoriteCount() {
    return favoriteCount;
  }

  public void setFavoriteCount(Integer favoriteCount) {
    this.favoriteCount = favoriteCount;
  }
}
