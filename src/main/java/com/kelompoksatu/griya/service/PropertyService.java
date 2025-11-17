package com.kelompoksatu.griya.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kelompoksatu.griya.dto.*;
import com.kelompoksatu.griya.entity.Developer;
import com.kelompoksatu.griya.entity.Property;
import com.kelompoksatu.griya.repository.DeveloperRepository;
import com.kelompoksatu.griya.repository.PropertyImageRepository;
import com.kelompoksatu.griya.repository.PropertyRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service class for Property business logic */
@Service
@Transactional
public class PropertyService {

  private final PropertyRepository propertyRepository;
  private final DeveloperRepository developerRepository;
  private final PropertyImageRepository propertyImageRepository;
  private final com.kelompoksatu.griya.repository.PropertyFeatureRepository
      propertyFeatureRepository;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  public PropertyService(
      PropertyRepository propertyRepository,
      DeveloperRepository developerRepository,
      PropertyImageRepository propertyImageRepository,
      com.kelompoksatu.griya.repository.PropertyFeatureRepository propertyFeatureRepository) {
    this.propertyRepository = propertyRepository;
    this.developerRepository = developerRepository;
    this.propertyImageRepository = propertyImageRepository;
    this.propertyFeatureRepository = propertyFeatureRepository;
  }

  // ========================================
  // CRUD OPERATIONS
  // ========================================

  /** Create a new property */
  public PropertyResponse createProperty(CreatePropertyRequest request) {
    validateCreatePropertyRequest(request);
    Developer developer = validateAndGetDeveloper(request.getDeveloperId());

    Property property = createPropertyEntity(request, developer);
    Property savedProperty = propertyRepository.save(property);

    createDefaultFeatures(savedProperty);

    return new PropertyResponse(savedProperty);
  }

  /** Delete property */
  public void deleteProperty(Integer id) {
    validatePropertyExists(id);
    propertyRepository.deleteById(id);
  }

  /** Delete image by ID */
  public ImageAdminResponse deleteImageById(Integer id) {
    var image =
        propertyImageRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Image not found with ID: " + id));

    ImageAdminResponse response =
        ImageAdminResponse.builder()
            .id(image.getId())
            .propertyId(image.getPropertyId())
            .imageUrl(image.getFilePath())
            .fileName(image.getFileName())
            .imageType(image.getImageType().name())
            .imageCategory(image.getImageCategory().name())
            .caption(image.getCaption())
            .fileSize(image.getFileSize())
            .mimeType(image.getMimeType())
            .build();

    propertyImageRepository.delete(image);
    return response;
  }

  // ========================================
  // QUERY METHODS - BASIC RETRIEVAL
  // ========================================

  /** Get property by ID */
  @Transactional(readOnly = true)
  public Optional<PropertyResponse> getPropertyById(Integer id) {
    return propertyRepository.findById(id).map(PropertyResponse::new);
  }

  /** Get property by property code */
  @Transactional(readOnly = true)
  public Optional<PropertyResponse> getPropertyByPropertyCode(String propertyCode) {
    return propertyRepository.findByPropertyCode(propertyCode).map(PropertyResponse::new);
  }

  /** Get property by slug */
  @Transactional(readOnly = true)
  public Optional<PropertyResponse> getPropertyBySlug(String slug) {
    return propertyRepository.findBySlug(slug).map(PropertyResponse::new);
  }

  /** Get all properties */
  @Transactional(readOnly = true)
  public List<PropertyResponse> getAllProperties() {
    return propertyRepository.findAll().stream()
        .map(PropertyResponse::new)
        .collect(Collectors.toList());
  }

  // ========================================
  // QUERY METHODS - BY DEVELOPER AND TYPE
  // ========================================

  /** Get properties by developer ID */
  @Transactional(readOnly = true)
  public List<PropertyResponse> getPropertiesByDeveloperId(Integer developerId) {
    return propertyRepository.findByDeveloperId(developerId).stream()
        .map(PropertyResponse::new)
        .collect(Collectors.toList());
  }

  /** Get properties by property type */
  @Transactional(readOnly = true)
  public List<PropertyResponse> getPropertiesByPropertyType(Property.PropertyType propertyType) {
    return propertyRepository.findByPropertyType(propertyType).stream()
        .map(PropertyResponse::new)
        .collect(Collectors.toList());
  }

  /** Get properties by listing type */
  @Transactional(readOnly = true)
  public List<PropertyResponse> getPropertiesByListingType(Property.ListingType listingType) {
    return propertyRepository.findByListingType(listingType).stream()
        .map(PropertyResponse::new)
        .collect(Collectors.toList());
  }

  // ========================================
  // QUERY METHODS - BY STATUS AND AVAILABILITY
  // ========================================

  /** Get properties by status */
  @Transactional(readOnly = true)
  public List<PropertyResponse> getPropertiesByStatus(Property.PropertyStatus status) {
    return propertyRepository.findByStatus(status).stream()
        .map(PropertyResponse::new)
        .collect(Collectors.toList());
  }

  /** Get available properties */
  @Transactional(readOnly = true)
  public List<PropertyResponse> getAvailableProperties() {
    return propertyRepository.findAvailableProperties().stream()
        .map(PropertyResponse::new)
        .collect(Collectors.toList());
  }

  // ========================================
  // QUERY METHODS - BY LOCATION
  // ========================================

  /** Get properties by city */
  @Transactional(readOnly = true)
  public List<PropertyResponse> getPropertiesByCity(String city) {
    return propertyRepository.findByCity(city).stream()
        .map(PropertyResponse::new)
        .collect(Collectors.toList());
  }

  /** Get properties by province */
  @Transactional(readOnly = true)
  public List<PropertyResponse> getPropertiesByProvince(String province) {
    return propertyRepository.findByProvince(province).stream()
        .map(PropertyResponse::new)
        .collect(Collectors.toList());
  }

  // ========================================
  // QUERY METHODS - BY PRICE AND AREA
  // ========================================

  /** Get properties by price range */
  @Transactional(readOnly = true)
  public List<PropertyResponse> getPropertiesByPriceRange(
      BigDecimal minPrice, BigDecimal maxPrice) {
    return propertyRepository.findByPriceRange(minPrice, maxPrice).stream()
        .map(PropertyResponse::new)
        .collect(Collectors.toList());
  }

  /** Get properties by area range */
  @Transactional(readOnly = true)
  public List<PropertyResponse> getPropertiesByAreaRange(BigDecimal minArea, BigDecimal maxArea) {
    return propertyRepository.findByLandAreaRange(minArea, maxArea).stream()
        .map(PropertyResponse::new)
        .collect(Collectors.toList());
  }

  // ========================================
  // QUERY METHODS - BY SPECIFICATIONS
  // ========================================

  @Transactional(readOnly = true)
  public List<Map<String, Object>> getPropertiesSimpleByFilters(
      String city, BigDecimal minPrice, BigDecimal maxPrice, String propertyType) {
    return propertyRepository.findPropertiesSimpleByFilters(city, minPrice, maxPrice, propertyType);
  }

  /** Get properties by bedrooms */
  @Transactional(readOnly = true)
  public List<PropertyResponse> getPropertiesByBedrooms(Integer bedrooms) {
    return propertyRepository.findByBedrooms(bedrooms).stream()
        .map(PropertyResponse::new)
        .collect(Collectors.toList());
  }

  /** Get properties by bathrooms */
  @Transactional(readOnly = true)
  public List<PropertyResponse> getPropertiesByBathrooms(Integer bathrooms) {
    return propertyRepository.findByBathrooms(bathrooms).stream()
        .map(PropertyResponse::new)
        .collect(Collectors.toList());
  }

  // ========================================
  // QUERY METHODS - FEATURED AND SPECIAL
  // ========================================

  /** Get featured properties */
  @Transactional(readOnly = true)
  public List<PropertyResponse> getFeaturedProperties() {
    return propertyRepository.findByIsFeaturedTrue().stream()
        .map(PropertyResponse::new)
        .collect(Collectors.toList());
  }

  /** Get KPR eligible properties */
  @Transactional(readOnly = true)
  public List<PropertyResponse> getKprEligibleProperties() {
    return propertyRepository.findByIsKprEligibleTrue().stream()
        .map(PropertyResponse::new)
        .collect(Collectors.toList());
  }

  /** Get popular properties (sorted by view count) */
  @Transactional(readOnly = true)
  public List<PropertyResponse> getPopularProperties(Pageable pageable) {
    return propertyRepository.findMostViewedProperties().stream()
        .map(PropertyResponse::new)
        .collect(Collectors.toList());
  }

  // ========================================
  // SEARCH METHODS
  // ========================================

  /** Search properties by title or description */
  @Transactional(readOnly = true)
  public List<PropertyResponse> searchProperties(String keyword) {
    List<Property> titleResults = propertyRepository.searchByTitle(keyword);
    List<Property> descriptionResults = propertyRepository.searchByDescription(keyword);

    // Combine results and remove duplicates
    titleResults.addAll(descriptionResults);
    return titleResults.stream().distinct().map(PropertyResponse::new).collect(Collectors.toList());
  }

  // ========================================
  // ADVANCED QUERY METHODS
  // ========================================

  @Transactional(readOnly = true)
  public List<Map<String, Object>> getPropertiesWithFilter(
      String city,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      String propertyType,
      String description,
      String title) {

    List<Map<String, Object>> rows =
        propertyRepository.findPropertiesWithFilter(
            city, minPrice, maxPrice, propertyType, description, title);

    return processFilterResults(rows);
  }

  @Transactional(readOnly = true)
  public Map<String, Object> getPropertyDetails(Integer id) {
    return propertyRepository.findPropertyDetailsById(id);
  }

  // ========================================
  // UPDATE OPERATIONS
  // ========================================

  /** Update property status */
  public PropertyResponse updatePropertyStatus(Integer id, Property.PropertyStatus status) {
    Property property = validateAndGetProperty(id);

    property.setStatus(status);
    setPublishedDateIfAvailable(property, status);

    Property updatedProperty = propertyRepository.save(property);
    return new PropertyResponse(updatedProperty);
  }

  /** Update featured status */
  public PropertyResponse updateFeaturedStatus(Integer id, Boolean isFeatured) {
    Property property = validateAndGetProperty(id);

    property.setIsFeatured(isFeatured);
    Property updatedProperty = propertyRepository.save(property);
    return new PropertyResponse(updatedProperty);
  }

  // ========================================
  // UPDATE PROPERTY (MAIN + RELATIONS)
  // ========================================
  @Transactional
  public UpdatePropertyResponse updateProperty(Integer id, UpdatePropertyRequest request) {
    Property property =
        propertyRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Property not found with id: " + id));

    // ✅ Update field utama (langsung semua kolom dari request)
    if (request.getTitle() != null) property.setTitle(request.getTitle());
    if (request.getDescription() != null) property.setDescription(request.getDescription());
    if (request.getAddress() != null) property.setAddress(request.getAddress());
    if (request.getCity() != null) property.setCity(request.getCity());
    if (request.getProvince() != null) property.setProvince(request.getProvince());
    if (request.getPostalCode() != null) property.setPostalCode(request.getPostalCode());
    if (request.getDistrict() != null) property.setDistrict(request.getDistrict());
    if (request.getVillage() != null) property.setVillage(request.getVillage());
    if (request.getLatitude() != null) property.setLatitude(request.getLatitude());
    if (request.getLongitude() != null) property.setLongitude(request.getLongitude());
    if (request.getLandArea() != null) property.setLandArea(request.getLandArea());
    if (request.getBuildingArea() != null) property.setBuildingArea(request.getBuildingArea());
    if (request.getBedrooms() != null) property.setBedrooms(request.getBedrooms());
    if (request.getBathrooms() != null) property.setBathrooms(request.getBathrooms());
    if (request.getFloors() != null) property.setFloors(request.getFloors());
    if (request.getGarage() != null) property.setGarage(request.getGarage());
    if (request.getYearBuilt() != null) property.setYearBuilt(request.getYearBuilt());
    if (request.getPrice() != null) property.setPrice(request.getPrice());
    if (request.getPricePerSqm() != null) property.setPricePerSqm(request.getPricePerSqm());
    if (request.getMaintenanceFee() != null)
      property.setMaintenanceFee(request.getMaintenanceFee());
    if (request.getCertificateNumber() != null)
      property.setCertificateNumber(request.getCertificateNumber());
    if (request.getCertificateArea() != null)
      property.setCertificateArea(request.getCertificateArea());
    if (request.getPbbValue() != null) property.setPbbValue(request.getPbbValue());
    if (request.getAvailabilityDate() != null)
      property.setAvailabilityDate(request.getAvailabilityDate());
    if (request.getHandoverDate() != null) property.setHandoverDate(request.getHandoverDate());
    if (request.getIsFeatured() != null) property.setIsFeatured(request.getIsFeatured());
    if (request.getIsKprEligible() != null) property.setIsKprEligible(request.getIsKprEligible());
    if (request.getMinDownPaymentPercent() != null)
      property.setMinDownPaymentPercent(request.getMinDownPaymentPercent());
    if (request.getMaxLoanTermYears() != null)
      property.setMaxLoanTermYears(request.getMaxLoanTermYears());
    if (request.getMetaTitle() != null) property.setMetaTitle(request.getMetaTitle());
    if (request.getMetaDescription() != null)
      property.setMetaDescription(request.getMetaDescription());
    if (request.getKeywords() != null) property.setKeywords(request.getKeywords());
    if (request.getViewCount() != null) property.setViewCount(request.getViewCount());
    if (request.getInquiryCount() != null) property.setInquiryCount(request.getInquiryCount());
    if (request.getFavoriteCount() != null) property.setFavoriteCount(request.getFavoriteCount());
    property.setUpdatedAt(LocalDateTime.now());

    // ✅ Enum-safe update
    try {
      if (request.getStatus() != null)
        property.setStatus(
            Property.PropertyStatus.valueOf(request.getStatus().trim().toUpperCase()));
      if (request.getPropertyType() != null)
        property.setPropertyType(
            Property.PropertyType.valueOf(request.getPropertyType().trim().toUpperCase()));
      if (request.getListingType() != null)
        property.setListingType(
            Property.ListingType.valueOf(request.getListingType().trim().toUpperCase()));
      if (request.getCertificateType() != null)
        property.setCertificateType(
            Property.CertificateType.valueOf(request.getCertificateType().trim().toUpperCase()));
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid enum value: " + ex.getMessage());
    }

    // ... (sekitar baris 390 di dalam method updateProperty)

    // ✅ Developer update (optional)
    if (request.getDeveloperId() != null) {
      Developer developer =
          developerRepository
              .findById(request.getDeveloperId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Developer not found with id: " + request.getDeveloperId()));

      // >>> LOGIKA UNTUK UPDATE NAMA DEVELOPER <<<
      if (request.getDeveloperName() != null && !request.getDeveloperName().trim().isEmpty()) {
        // Menggunakan getCompanyName() dan setCompanyName() dari Developer Entity
        if (!request.getDeveloperName().equals(developer.getCompanyName())) {
          developer.setCompanyName(request.getDeveloperName());
          // Simpan perubahan pada Developer Entity
          developerRepository.save(developer);
        }
      }
      // >>> END LOGIKA UPDATE NAMA DEVELOPER <<<

      property.setDeveloper(developer);
    }

    Property updated = propertyRepository.save(property);

    // ✅ Build full response
    // ... (kode building response Anda yang sudah ada sudah benar karena akan mengambil nama
    // terbaru dari relasi Developer)
    // ...

    // ✅ Build full response
    return UpdatePropertyResponse.builder()
        .id(updated.getId())
        .developerId(updated.getDeveloper() != null ? updated.getDeveloper().getId() : null)
        .developerName(
            updated.getDeveloper() != null ? updated.getDeveloper().getCompanyName() : null)
        .propertyType(updated.getPropertyType() != null ? updated.getPropertyType().name() : null)
        .listingType(updated.getListingType() != null ? updated.getListingType().name() : null)
        .title(updated.getTitle())
        .description(updated.getDescription())
        .address(updated.getAddress())
        .city(updated.getCity())
        .province(updated.getProvince())
        .postalCode(updated.getPostalCode())
        .district(updated.getDistrict())
        .village(updated.getVillage())
        .latitude(updated.getLatitude())
        .longitude(updated.getLongitude())
        .landArea(updated.getLandArea())
        .buildingArea(updated.getBuildingArea())
        .bedrooms(updated.getBedrooms())
        .bathrooms(updated.getBathrooms())
        .floors(updated.getFloors())
        .garage(updated.getGarage())
        .yearBuilt(updated.getYearBuilt())
        .price(updated.getPrice())
        .pricePerSqm(updated.getPricePerSqm())
        .maintenanceFee(updated.getMaintenanceFee())
        .certificateType(
            updated.getCertificateType() != null ? updated.getCertificateType().name() : null)
        .certificateNumber(updated.getCertificateNumber())
        .certificateArea(updated.getCertificateArea())
        .pbbValue(updated.getPbbValue())
        .status(updated.getStatus() != null ? updated.getStatus().name() : null)
        .availabilityDate(updated.getAvailabilityDate())
        .handoverDate(updated.getHandoverDate())
        .isFeatured(updated.getIsFeatured())
        .isKprEligible(updated.getIsKprEligible())
        .minDownPaymentPercent(updated.getMinDownPaymentPercent())
        .maxLoanTermYears(updated.getMaxLoanTermYears())
        .metaTitle(updated.getMetaTitle())
        .metaDescription(updated.getMetaDescription())
        .keywords(updated.getKeywords())
        .updatedAt(updated.getUpdatedAt() != null ? updated.getUpdatedAt().toLocalDate() : null)
        .build();
  }

  // ========================================
  // COUNTER OPERATIONS
  // ========================================

  /** Increment view count */
  public PropertyResponse incrementViewCount(Integer id) {
    Property property = validateAndGetProperty(id);

    property.setViewCount(property.getViewCount() + 1);
    Property updatedProperty = propertyRepository.save(property);
    return new PropertyResponse(updatedProperty);
  }

  /** Increment inquiry count */
  public PropertyResponse incrementInquiryCount(Integer id) {
    Property property = validateAndGetProperty(id);

    property.setInquiryCount(property.getInquiryCount() + 1);
    Property updatedProperty = propertyRepository.save(property);
    return new PropertyResponse(updatedProperty);
  }

  /** Increment favorite count */
  public PropertyResponse incrementFavoriteCount(Integer id) {
    Property property = validateAndGetProperty(id);

    property.setFavoriteCount(property.getFavoriteCount() + 1);
    Property updatedProperty = propertyRepository.save(property);
    return new PropertyResponse(updatedProperty);
  }

  // ========================================
  // VALIDATION AND EXISTENCE CHECKS
  // ========================================

  /** Check if property code exists */
  @Transactional(readOnly = true)
  public boolean existsByPropertyCode(String propertyCode) {
    return propertyRepository.existsByPropertyCode(propertyCode);
  }

  /** Check if slug exists */
  @Transactional(readOnly = true)
  public boolean existsBySlug(String slug) {
    return propertyRepository.existsBySlug(slug);
  }

  // ========================================
  // PRIVATE VALIDATION METHODS
  // ========================================

  private void validateCreatePropertyRequest(CreatePropertyRequest request) {
    if (propertyRepository.existsByPropertyCode(request.getPropertyCode())) {
      throw new IllegalArgumentException(
          "Property code already exists: " + request.getPropertyCode());
    }

    if (propertyRepository.existsBySlug(request.getSlug())) {
      throw new IllegalArgumentException("Slug already exists: " + request.getSlug());
    }
  }

  private Developer validateAndGetDeveloper(Integer developerId) {
    return developerRepository
        .findById(developerId)
        .orElseThrow(
            () -> new IllegalArgumentException("Developer not found with id: " + developerId));
  }

  private Property validateAndGetProperty(Integer id) {
    return propertyRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Property not found with id: " + id));
  }

  private void validatePropertyExists(Integer id) {
    if (!propertyRepository.existsById(id)) {
      throw new IllegalArgumentException("Property not found with id: " + id);
    }
  }

  // ========================================
  // PRIVATE BUSINESS LOGIC METHODS
  // ========================================

  private Property createPropertyEntity(CreatePropertyRequest request, Developer developer) {
    Property property = new Property();

    // Basic information
    setBasicPropertyInfo(property, request, developer);

    // Location details
    setLocationDetails(property, request);

    // Property specifications
    setPropertySpecifications(property, request);

    // Pricing information
    setPricingInfo(property, request);

    // Legal & certificates
    setLegalInfo(property, request);

    // Availability information
    setAvailabilityInfo(property, request);

    // Marketing information
    setMarketingInfo(property, request);

    // SEO & marketing
    setSeoInfo(property, request);

    // Initialize tracking counters
    initializeTrackingCounters(property, request);

    // Set published date if status is available
    if (property.getStatus() == Property.PropertyStatus.AVAILABLE) {
      property.setPublishedAt(LocalDateTime.now());
    }

    return property;
  }

  private void setBasicPropertyInfo(
      Property property, CreatePropertyRequest request, Developer developer) {
    property.setDeveloperId(request.getDeveloperId());
    property.setPropertyCode(request.getPropertyCode());
    property.setDeveloper(developer);
    property.setPropertyType(request.getPropertyType());
    property.setListingType(request.getListingType());
    property.setTitle(request.getTitle());
    property.setDescription(request.getDescription());
  }

  private void setLocationDetails(Property property, CreatePropertyRequest request) {
    property.setAddress(request.getAddress());
    property.setCity(request.getCity());
    property.setProvince(request.getProvince());
    property.setPostalCode(request.getPostalCode());
    property.setDistrict(request.getDistrict());
    property.setVillage(request.getVillage());
    property.setLatitude(request.getLatitude());
    property.setLongitude(request.getLongitude());
  }

  private void setPropertySpecifications(Property property, CreatePropertyRequest request) {
    property.setLandArea(request.getLandArea());
    property.setBuildingArea(request.getBuildingArea());
    property.setBedrooms(request.getBedrooms());
    property.setBathrooms(request.getBathrooms());
    property.setFloors(request.getFloors());
    property.setGarage(request.getGarage());
    property.setYearBuilt(request.getYearBuilt());
  }

  private void setPricingInfo(Property property, CreatePropertyRequest request) {
    property.setPrice(request.getPrice());
    property.setPricePerSqm(request.getPricePerSqm());
    property.setMaintenanceFee(request.getMaintenanceFee());
  }

  private void setLegalInfo(Property property, CreatePropertyRequest request) {
    property.setCertificateType(request.getCertificateType());
    property.setCertificateNumber(request.getCertificateNumber());
    property.setCertificateArea(request.getCertificateArea());
    property.setPbbValue(request.getPbbValue());
  }

  private void setAvailabilityInfo(Property property, CreatePropertyRequest request) {
    property.setStatus(request.getStatus());
    property.setAvailabilityDate(request.getAvailabilityDate());
    property.setHandoverDate(request.getHandoverDate());
  }

  private void setMarketingInfo(Property property, CreatePropertyRequest request) {
    property.setIsFeatured(request.getIsFeatured());
    property.setIsKprEligible(request.getIsKprEligible());
    property.setMinDownPaymentPercent(request.getMinDownPaymentPercent());
    property.setMaxLoanTermYears(request.getMaxLoanTermYears());
  }

  private void setSeoInfo(Property property, CreatePropertyRequest request) {
    property.setSlug(request.getSlug());
    property.setMetaTitle(request.getMetaTitle());
    property.setMetaDescription(request.getMetaDescription());
    property.setKeywords(request.getKeywords());
  }

  private void initializeTrackingCounters(Property property, CreatePropertyRequest request) {
    property.setViewCount(request.getViewCount() != null ? request.getViewCount() : 0);
    property.setInquiryCount(request.getInquiryCount() != null ? request.getInquiryCount() : 0);
    property.setFavoriteCount(request.getFavoriteCount() != null ? request.getFavoriteCount() : 0);
  }

  private void createDefaultFeatures(Property p) {
    java.util.List<com.kelompoksatu.griya.entity.PropertyFeature> features =
        new java.util.ArrayList<>();
    addFeature(
        features,
        p,
        com.kelompoksatu.griya.entity.PropertyFeature.FeatureCategory.INTERIOR,
        "bedrooms",
        p.getBedrooms());
    addFeature(
        features,
        p,
        com.kelompoksatu.griya.entity.PropertyFeature.FeatureCategory.INTERIOR,
        "bathrooms",
        p.getBathrooms());
    addFeature(
        features,
        p,
        com.kelompoksatu.griya.entity.PropertyFeature.FeatureCategory.INTERIOR,
        "floors",
        p.getFloors());
    addFeature(
        features,
        p,
        com.kelompoksatu.griya.entity.PropertyFeature.FeatureCategory.EXTERIOR,
        "garage",
        p.getGarage());
    if (!features.isEmpty()) {
      propertyFeatureRepository.saveAll(features);
    }
  }

  private void addFeature(
      java.util.List<com.kelompoksatu.griya.entity.PropertyFeature> list,
      Property p,
      com.kelompoksatu.griya.entity.PropertyFeature.FeatureCategory category,
      String name,
      Object value) {
    if (value == null) return;
    String v;
    if (value instanceof java.time.LocalDate) {
      v = value.toString();
    } else if (value instanceof java.lang.Enum<?> en) {
      v = en.name();
    } else {
      v = String.valueOf(value);
    }
    com.kelompoksatu.griya.entity.PropertyFeature pf =
        com.kelompoksatu.griya.entity.PropertyFeature.builder()
            .featureCategory(category)
            .featureName(name)
            .featureValue(v)
            .property(p)
            .build();
    list.add(pf);
  }

  private void setPublishedDateIfAvailable(Property property, Property.PropertyStatus status) {
    if (status == Property.PropertyStatus.AVAILABLE && property.getPublishedAt() == null) {
      property.setPublishedAt(LocalDateTime.now());
    }
  }

  // ========================================
  // PRIVATE UTILITY METHODS
  // ========================================

  private List<Map<String, Object>> processFilterResults(List<Map<String, Object>> rows) {
    List<Map<String, Object>> processedRows = new ArrayList<>();

    for (Map<String, Object> row : rows) {
      // Create a new mutable HashMap from the immutable TupleBackedMap
      Map<String, Object> mutableRow = new HashMap<>(row);

      Object fn = mutableRow.get("file_name");
      if (fn != null) mutableRow.put("fileName", fn);

      Object fp = mutableRow.get("file_path");
      if (fp != null) mutableRow.put("filePath", fp);

      Object la = mutableRow.get("land_area");
      if (la != null) mutableRow.put("landArea", la);

      Object ba = mutableRow.get("building_area");
      if (ba != null) mutableRow.put("buildingArea", ba);

      Object featuresJson = mutableRow.get("features_json");
      if (featuresJson != null) {
        try {
          Object parsed =
              (featuresJson instanceof String)
                  ? objectMapper.readValue(
                      (String) featuresJson, new TypeReference<List<Map<String, Object>>>() {})
                  : objectMapper.readValue(
                      featuresJson.toString(), new TypeReference<List<Map<String, Object>>>() {});
          mutableRow.put("featuresDetail", parsed);
        } catch (Exception ignore) {
          mutableRow.put("featuresDetail", new ArrayList<>());
        }
        mutableRow.remove("features_json");
      }

      processedRows.add(mutableRow);
    }
    return processedRows;
  }
}
