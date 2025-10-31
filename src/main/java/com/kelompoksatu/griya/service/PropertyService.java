package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.CreatePropertyRequest;
import com.kelompoksatu.griya.dto.PropertyResponse;
import com.kelompoksatu.griya.dto.UpdatePropertyRequest;
import com.kelompoksatu.griya.dto.UpdatePropertyResponse;
import com.kelompoksatu.griya.entity.Developer;
import com.kelompoksatu.griya.entity.Property;
import com.kelompoksatu.griya.entity.PropertyFeature;
import com.kelompoksatu.griya.entity.PropertyImage;
import com.kelompoksatu.griya.entity.PropertyLocation;
import com.kelompoksatu.griya.repository.DeveloperRepository;
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

  @Autowired
  public PropertyService(
      PropertyRepository propertyRepository, DeveloperRepository developerRepository) {
    this.propertyRepository = propertyRepository;
    this.developerRepository = developerRepository;
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

    return new PropertyResponse(savedProperty);
  }

  /** Delete property */
  public void deleteProperty(Integer id) {
    validatePropertyExists(id);
    propertyRepository.deleteById(id);
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
      int offset,
      int limit) {

    List<Map<String, Object>> rows =
        propertyRepository.findPropertiesWithFilter(
            city, minPrice, maxPrice, propertyType, offset, limit);

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

    // 1) update field utama
    updateMainFields(property, request);

    // 2) update relasi
    //  updateImages(property, request);
    updateFeatures(property, request);
    updateLocations(property, request);

    // 3) simpan
    Property updated = propertyRepository.save(property);

    // 4) bangun response
    return buildUpdatePropertyResponse(updated);
  }

  private void updateMainFields(Property property, UpdatePropertyRequest request) {
    if (request.getTitle() != null) property.setTitle(request.getTitle());
    if (request.getDescription() != null) property.setDescription(request.getDescription());
    if (request.getPrice() != null) property.setPrice(request.getPrice());
    if (request.getBedrooms() != null) property.setBedrooms(request.getBedrooms());
    if (request.getBathrooms() != null) property.setBathrooms(request.getBathrooms());

    if (request.getStatus() != null && !request.getStatus().isBlank()) {
      try {
        property.setStatus(
            Property.PropertyStatus.valueOf(request.getStatus().trim().toUpperCase()));
      } catch (IllegalArgumentException ex) {
        throw new IllegalArgumentException("Invalid status: " + request.getStatus());
      }
    }

    property.setUpdatedAt(LocalDateTime.now());
  }

  //  private void updateImages(Property property, UpdatePropertyRequest request) {
  //    if (request.getImages() == null) return;
  //
  //    // kosongin list lama (otomatis hapus di DB karena orphanRemoval = true)
  //    property.getImages().clear();
  //
  //    // tambahin image baru dari request
  //    request
  //        .getImages()
  //        .forEach(
  //            imgReq -> {
  //              PropertyImage img =
  //                  PropertyImage.builder()
  //                      .filePath(imgReq.getFilePath())
  //                      .isPrimary(Boolean.TRUE.equals(imgReq.getIsPrimary()))
  //                      .property(property) // penting! relasi balik
  //                      .build();
  //              property.getImages().add(img);
  //            });
  //
  //    // pastiin cuma ada 1 primary
  //    enforceSinglePrimaryImage(property);
  //  }

  private void enforceSinglePrimaryImage(Property property) {
    long primaries =
        property.getImages().stream().filter(i -> Boolean.TRUE.equals(i.getIsPrimary())).count();

    if (primaries == 0 && !property.getImages().isEmpty()) {
      // kalau gak ada primary, set image pertama jadi primary
      property.getImages().get(0).setIsPrimary(true);
    } else if (primaries > 1) {
      // kalau lebih dari 1 primary, sisain yang pertama aja
      boolean kept = false;
      for (PropertyImage i : property.getImages()) {
        if (Boolean.TRUE.equals(i.getIsPrimary())) {
          if (!kept) kept = true;
          else i.setIsPrimary(false);
        }
      }
    }
  }

  private void updateFeatures(Property property, UpdatePropertyRequest request) {
    if (request.getFeatures() == null) return;
    property.getFeatures().clear();

    request
        .getFeatures()
        .forEach(
            featReq -> {
              PropertyFeature.FeatureCategory category;
              try {
                category =
                    featReq.getFeatureCategory() != null
                        ? PropertyFeature.FeatureCategory.valueOf(
                            featReq.getFeatureCategory().toUpperCase())
                        : PropertyFeature.FeatureCategory.INTERIOR;
              } catch (IllegalArgumentException e) {
                category = PropertyFeature.FeatureCategory.INTERIOR;
              }

              PropertyFeature feat =
                  PropertyFeature.builder()
                      .featureCategory(category)
                      .featureName(featReq.getFeatureName())
                      .featureValue(featReq.getFeatureValue())
                      .property(property)
                      .build();

              property.getFeatures().add(feat);
            });
  }

  private void updateLocations(Property property, UpdatePropertyRequest request) {
    if (request.getLocations() == null) return;

    property.getLocations().clear();

    request
        .getLocations()
        .forEach(
            locReq -> {
              PropertyLocation.PropertyLocationType type;
              try {
                type =
                    locReq.getPoiType() != null
                        ? PropertyLocation.PropertyLocationType.valueOf(
                            locReq.getPoiType().toUpperCase())
                        : PropertyLocation.PropertyLocationType.OFFICE; // default biar aman
              } catch (IllegalArgumentException e) {
                type = PropertyLocation.PropertyLocationType.OFFICE;
              }

              PropertyLocation loc =
                  PropertyLocation.builder()
                      .poiName(locReq.getPoiName())
                      .distanceKm(locReq.getDistanceKm())
                      .poiType(type)
                      .property(property)
                      .build();

              property.getLocations().add(loc);
            });
  }

  private UpdatePropertyResponse buildUpdatePropertyResponse(Property updated) {
    String developerName =
        (updated.getDeveloper() != null) ? updated.getDeveloper().getCompanyName() : null;

    return UpdatePropertyResponse.builder()
        .id(updated.getId())
        .title(updated.getTitle())
        .description(updated.getDescription())
        .price(updated.getPrice())
        .bedrooms(updated.getBedrooms())
        .bathrooms(updated.getBathrooms())
        .status(updated.getStatus() != null ? updated.getStatus().name() : null)
        .propertyType(updated.getPropertyType() != null ? updated.getPropertyType().name() : null)
        .city(updated.getCity())
        .developerName(developerName)
        .address(updated.getAddress())
        //        .images(
        //            updated.getImages() == null
        //                ? List.of()
        //                : updated.getImages().stream()
        //                    .map(
        //                        img ->
        //                            UpdatePropertyResponse.ImageData.builder()
        //                                .filePath(img.getFilePath())
        //                                .isPrimary(Boolean.TRUE.equals(img.getIsPrimary()))
        //                                .build())
        //                    .toList())
        .features(
            updated.getFeatures() == null
                ? List.of()
                : updated.getFeatures().stream()
                    .map(
                        f ->
                            UpdatePropertyResponse.FeatureData.builder()
                                .featureName(f.getFeatureName())
                                .featureValue(f.getFeatureValue())
                                .build())
                    .toList())
        .locations(
            updated.getLocations() == null
                ? List.of()
                : updated.getLocations().stream()
                    .map(
                        l ->
                            UpdatePropertyResponse.LocationData.builder()
                                .poiName(l.getPoiName())
                                .distanceKm(l.getDistanceKm())
                                .build())
                    .toList())
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

      processedRows.add(mutableRow);
    }
    return processedRows;
  }
}
