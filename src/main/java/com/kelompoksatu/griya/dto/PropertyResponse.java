package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.Property;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** DTO for property response */
public class PropertyResponse {

  private Integer id;

  public Boolean getFeatured() {
    return isFeatured;
  }

  public void setFeatured(Boolean featured) {
    isFeatured = featured;
  }

  public Boolean getKprEligible() {
    return isKprEligible;
  }

  public void setKprEligible(Boolean kprEligible) {
    isKprEligible = kprEligible;
  }

  private String fileName;
  private String filePath;

  private String propertyCode;
  private Integer developerId;
  private Property.PropertyType propertyType;
  private Property.ListingType listingType;
  private String title;
  private String description;

  // Location Details
  private String address;
  private String city;
  private String province;
  private String postalCode;
  private String district;
  private String village;
  private BigDecimal latitude;
  private BigDecimal longitude;

  // Property Specifications
  private BigDecimal landArea;
  private BigDecimal buildingArea;
  private Integer bedrooms;
  private Integer bathrooms;
  private Integer floors;
  private Integer garage;
  private Integer yearBuilt;

  // Pricing
  private BigDecimal price;
  private BigDecimal pricePerSqm;
  private BigDecimal maintenanceFee;

  // Legal & Certificates
  private Property.CertificateType certificateType;
  private String certificateNumber;
  private BigDecimal certificateArea;
  private BigDecimal pbbValue;

  // Availability
  private Property.PropertyStatus status;
  private LocalDate availabilityDate;
  private LocalDate handoverDate;

  // Marketing
  private Boolean isFeatured;
  private Boolean isKprEligible;
  private BigDecimal minDownPaymentPercent;
  private Integer maxLoanTermYears;

  // SEO & Marketing
  private String slug;
  private String metaTitle;
  private String metaDescription;
  private String keywords;

  // Tracking
  private Integer viewCount;
  private Integer inquiryCount;
  private Integer favoriteCount;

  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime publishedAt;

  // Developer information (optional)
  private DeveloperResponse developer;

  // Default constructor
  public PropertyResponse() {}

  // Constructor from Property entity
  public PropertyResponse(Property property) {
    this.id = property.getId();
    this.propertyCode = property.getPropertyCode();
    this.developerId = property.getDeveloperId();
    this.propertyType = property.getPropertyType();
    this.listingType = property.getListingType();
    this.title = property.getTitle();
    this.description = property.getDescription();
    this.address = property.getAddress();
    this.city = property.getCity();
    this.province = property.getProvince();
    this.postalCode = property.getPostalCode();
    this.district = property.getDistrict();
    this.village = property.getVillage();
    this.latitude = property.getLatitude();
    this.longitude = property.getLongitude();
    this.landArea = property.getLandArea();
    this.buildingArea = property.getBuildingArea();
    this.bedrooms = property.getBedrooms();
    this.bathrooms = property.getBathrooms();
    this.floors = property.getFloors();
    this.garage = property.getGarage();
    this.yearBuilt = property.getYearBuilt();
    this.price = property.getPrice();
    this.pricePerSqm = property.getPricePerSqm();
    this.maintenanceFee = property.getMaintenanceFee();
    this.certificateType = property.getCertificateType();
    this.certificateNumber = property.getCertificateNumber();
    this.certificateArea = property.getCertificateArea();
    this.pbbValue = property.getPbbValue();
    this.status = property.getStatus();
    this.availabilityDate = property.getAvailabilityDate();
    this.handoverDate = property.getHandoverDate();
    this.isFeatured = property.getIsFeatured();
    this.isKprEligible = property.getIsKprEligible();
    this.minDownPaymentPercent = property.getMinDownPaymentPercent();
    this.maxLoanTermYears = property.getMaxLoanTermYears();
    this.slug = property.getSlug();
    this.metaTitle = property.getMetaTitle();
    this.metaDescription = property.getMetaDescription();
    this.keywords = property.getKeywords();
    this.viewCount = property.getViewCount();
    this.inquiryCount = property.getInquiryCount();
    this.favoriteCount = property.getFavoriteCount();
    this.createdAt = property.getCreatedAt();
    this.updatedAt = property.getUpdatedAt();
    this.publishedAt = property.getPublishedAt();

    // Include developer information if available
    if (property.getDeveloper() != null) {
      this.developer = new DeveloperResponse(property.getDeveloper());
    }
  }

  // Getters and Setters

  public String getFilePath() {
    return filePath;
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

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

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public LocalDateTime getPublishedAt() {
    return publishedAt;
  }

  public void setPublishedAt(LocalDateTime publishedAt) {
    this.publishedAt = publishedAt;
  }

  public DeveloperResponse getDeveloper() {
    return developer;
  }

  public void setDeveloper(DeveloperResponse developer) {
    this.developer = developer;
  }
}
