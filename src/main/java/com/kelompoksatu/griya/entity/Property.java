package com.kelompoksatu.griya.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** Property entity representing comprehensive property listings with detailed specifications */
@Entity
@Table(name = "properties")
public class Property {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "property_code", length = 20, unique = true, nullable = false)
  private String propertyCode; // Format: PROP-YYYY-XXXXXX

  @Column(name = "developer_id", nullable = false)
  private Integer developerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "property_type", nullable = false)
  private PropertyType propertyType;

  @Enumerated(EnumType.STRING)
  @Column(name = "listing_type", nullable = false)
  private ListingType listingType;

  @Column(name = "title", length = 255, nullable = false)
  private String title;

  @Column(name = "description", columnDefinition = "TEXT", nullable = false)
  private String description;

  // Location Details
  @Column(name = "address", columnDefinition = "TEXT", nullable = false)
  private String address;

  @Column(name = "city", length = 100, nullable = false)
  private String city;

  @Column(name = "province", length = 100, nullable = false)
  private String province;

  @Column(name = "postal_code", length = 10, nullable = false)
  private String postalCode;

  @Column(name = "district", length = 100, nullable = false)
  private String district; // Kecamatan

  @Column(name = "village", length = 100, nullable = false)
  private String village; // Kelurahan

  @Column(name = "latitude", precision = 10, scale = 8)
  private BigDecimal latitude;

  @Column(name = "longitude", precision = 11, scale = 8)
  private BigDecimal longitude;

  // Property Specifications
  @Column(name = "land_area", precision = 8, scale = 2, nullable = false)
  private BigDecimal landArea; // m2

  @Column(name = "building_area", precision = 8, scale = 2, nullable = false)
  private BigDecimal buildingArea; // m2

  @Column(name = "bedrooms", nullable = false)
  private Integer bedrooms;

  @Column(name = "bathrooms", nullable = false)
  private Integer bathrooms;

  @Column(name = "floors")
  private Integer floors = 1;

  @Column(name = "garage")
  private Integer garage = 0;

  @Column(name = "year_built")
  private Integer yearBuilt;

  // Pricing
  @Column(name = "price", precision = 15, scale = 2, nullable = false)
  private BigDecimal price;

  @Column(name = "price_per_sqm", precision = 10, scale = 2, nullable = false)
  private BigDecimal pricePerSqm;

  @Column(name = "maintenance_fee", precision = 10, scale = 2)
  private BigDecimal maintenanceFee = BigDecimal.ZERO; // For apartments

  // Legal & Certificates
  @Enumerated(EnumType.STRING)
  @Column(name = "certificate_type", nullable = false)
  private CertificateType certificateType;

  @Column(name = "certificate_number", length = 100)
  private String certificateNumber;

  @Column(name = "certificate_area", precision = 8, scale = 2)
  private BigDecimal certificateArea;

  @Column(name = "pbb_value", precision = 15, scale = 2)
  private BigDecimal pbbValue; // Nilai PBB

  // Availability
  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private PropertyStatus status = PropertyStatus.AVAILABLE;

  @Column(name = "availability_date")
  private LocalDate availabilityDate;

  @Column(name = "handover_date")
  private LocalDate handoverDate;

  // Marketing
  @Column(name = "is_featured")
  private Boolean isFeatured = false;

  @Column(name = "is_kpr_eligible")
  private Boolean isKprEligible = true;

  @Column(name = "min_down_payment_percent", precision = 5, scale = 2)
  private BigDecimal minDownPaymentPercent = new BigDecimal("20.00");

  @Column(name = "max_loan_term_years")
  private Integer maxLoanTermYears = 20;

  // SEO & Marketing
  @Column(name = "slug", length = 255, unique = true, nullable = false)
  private String slug;

  @Column(name = "meta_title", length = 255)
  private String metaTitle;

  @Column(name = "meta_description", columnDefinition = "TEXT")
  private String metaDescription;

  @Column(name = "keywords", columnDefinition = "TEXT")
  private String keywords;

  // Tracking
  @Column(name = "view_count")
  private Integer viewCount = 0;

  @Column(name = "inquiry_count")
  private Integer inquiryCount = 0;

  @Column(name = "favorite_count")
  private Integer favoriteCount = 0;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "published_at")
  private LocalDateTime publishedAt;

  // Relationship with Developer
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "developer_id", insertable = false, updatable = false)
  private Developer developer;

  // Enums
  public enum PropertyType {
    RUMAH,
    APARTEMEN,
    RUKO,
    TANAH,
    TOWNHOUSE,
    VILLA
  }

  public enum ListingType {
    PRIMARY,
    SECONDARY // Primary dari developer, Secondary dari individu
  }

  public enum CertificateType {
    SHM,
    HGB,
    HGU,
    HP,
    GIRIK,
    PETOK_D
  }

  public enum PropertyStatus {
    AVAILABLE,
    RESERVED,
    SOLD,
    OFF_MARKET,
    UNDER_CONSTRUCTION
  }

  // Default constructor
  public Property() {}

  // Constructor with essential parameters
  public Property(
      String propertyCode,
      Integer developerId,
      PropertyType propertyType,
      ListingType listingType,
      String title,
      String description,
      String address,
      String city,
      String province,
      String postalCode,
      String district,
      String village,
      BigDecimal landArea,
      BigDecimal buildingArea,
      Integer bedrooms,
      Integer bathrooms,
      BigDecimal price,
      BigDecimal pricePerSqm,
      CertificateType certificateType,
      String slug) {
    this.propertyCode = propertyCode;
    this.developerId = developerId;
    this.propertyType = propertyType;
    this.listingType = listingType;
    this.title = title;
    this.description = description;
    this.address = address;
    this.city = city;
    this.province = province;
    this.postalCode = postalCode;
    this.district = district;
    this.village = village;
    this.landArea = landArea;
    this.buildingArea = buildingArea;
    this.bedrooms = bedrooms;
    this.bathrooms = bathrooms;
    this.price = price;
    this.pricePerSqm = pricePerSqm;
    this.certificateType = certificateType;
    this.slug = slug;
  }

  // Getters and Setters
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

  public PropertyType getPropertyType() {
    return propertyType;
  }

  public void setPropertyType(PropertyType propertyType) {
    this.propertyType = propertyType;
  }

  public ListingType getListingType() {
    return listingType;
  }

  public void setListingType(ListingType listingType) {
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

  public CertificateType getCertificateType() {
    return certificateType;
  }

  public void setCertificateType(CertificateType certificateType) {
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

  public PropertyStatus getStatus() {
    return status;
  }

  public void setStatus(PropertyStatus status) {
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

  public Developer getDeveloper() {
    return developer;
  }

  public void setDeveloper(Developer developer) {
    this.developer = developer;
  }
}
