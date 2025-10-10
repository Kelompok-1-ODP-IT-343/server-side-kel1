package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.CreatePropertyRequest;
import com.kelompoksatu.griya.dto.PropertyResponse;
import com.kelompoksatu.griya.entity.Developer;
import com.kelompoksatu.griya.entity.Property;
import com.kelompoksatu.griya.repository.DeveloperRepository;
import com.kelompoksatu.griya.repository.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Property business logic
 */
@Service
@Transactional
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final DeveloperRepository developerRepository;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository, DeveloperRepository developerRepository) {
        this.propertyRepository = propertyRepository;
        this.developerRepository = developerRepository;
    }

    /**
     * Create a new property
     */
    public PropertyResponse createProperty(CreatePropertyRequest request) {
        // Validate developer exists
        Developer developer = developerRepository.findById(request.getDeveloperId())
                .orElseThrow(() -> new IllegalArgumentException("Developer not found with id: " + request.getDeveloperId()));

        // Validate unique constraints
        if (propertyRepository.existsByPropertyCode(request.getPropertyCode())) {
            throw new IllegalArgumentException("Property code already exists: " + request.getPropertyCode());
        }

        if (propertyRepository.existsBySlug(request.getSlug())) {
            throw new IllegalArgumentException("Slug already exists: " + request.getSlug());
        }

        // Create new property entity
        Property property = new Property();
        property.setPropertyCode(request.getPropertyCode());
        property.setDeveloper(developer);
        property.setPropertyType(request.getPropertyType());
        property.setListingType(request.getListingType());
        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());

        // Location details
        property.setAddress(request.getAddress());
        property.setCity(request.getCity());
        property.setProvince(request.getProvince());
        property.setPostalCode(request.getPostalCode());
        property.setDistrict(request.getDistrict());
        property.setVillage(request.getVillage());
        property.setLatitude(request.getLatitude());
        property.setLongitude(request.getLongitude());

        // Property specifications
        property.setLandArea(request.getLandArea());
        property.setBuildingArea(request.getBuildingArea());
        property.setBedrooms(request.getBedrooms());
        property.setBathrooms(request.getBathrooms());
        property.setFloors(request.getFloors());
        property.setGarage(request.getGarage());
        property.setYearBuilt(request.getYearBuilt());

        // Pricing
        property.setPrice(request.getPrice());
        property.setPricePerSqm(request.getPricePerSqm());
        property.setMaintenanceFee(request.getMaintenanceFee());

        // Legal & certificates
        property.setCertificateType(request.getCertificateType());
        property.setCertificateNumber(request.getCertificateNumber());
        property.setCertificateArea(request.getCertificateArea());
        property.setPbbValue(request.getPbbValue());

        // Availability
        property.setStatus(request.getStatus());
        property.setAvailabilityDate(request.getAvailabilityDate());
        property.setHandoverDate(request.getHandoverDate());

        // Marketing
        property.setIsFeatured(request.getIsFeatured());
        property.setIsKprEligible(request.getIsKprEligible());
        property.setMinDownPaymentPercent(request.getMinDownPaymentPercent());
        property.setMaxLoanTermYears(request.getMaxLoanTermYears());

        // SEO & marketing
        property.setSlug(request.getSlug());
        property.setMetaTitle(request.getMetaTitle());
        property.setMetaDescription(request.getMetaDescription());
        property.setKeywords(request.getKeywords());

        // Tracking (initialize with provided values or defaults)
        property.setViewCount(request.getViewCount() != null ? request.getViewCount() : 0);
        property.setInquiryCount(request.getInquiryCount() != null ? request.getInquiryCount() : 0);
        property.setFavoriteCount(request.getFavoriteCount() != null ? request.getFavoriteCount() : 0);

        // Set published date if status is available
        if (property.getStatus() == Property.PropertyStatus.AVAILABLE) {
            property.setPublishedAt(LocalDateTime.now());
        }

        // Save the property
        Property savedProperty = propertyRepository.save(property);

        return new PropertyResponse(savedProperty);
    }

    /**
     * Get property by ID
     */
    @Transactional(readOnly = true)
    public Optional<PropertyResponse> getPropertyById(Integer id) {
        return propertyRepository.findById(id)
                .map(PropertyResponse::new);
    }

    /**
     * Get property by property code
     */
    @Transactional(readOnly = true)
    public Optional<PropertyResponse> getPropertyByPropertyCode(String propertyCode) {
        return propertyRepository.findByPropertyCode(propertyCode)
                .map(PropertyResponse::new);
    }

    /**
     * Get property by slug
     */
    @Transactional(readOnly = true)
    public Optional<PropertyResponse> getPropertyBySlug(String slug) {
        return propertyRepository.findBySlug(slug)
                .map(PropertyResponse::new);
    }

    /**
     * Get all properties
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getAllProperties() {
        return propertyRepository.findAll().stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get properties by developer ID
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByDeveloperId(Integer developerId) {
        return propertyRepository.findByDeveloperId(developerId).stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get properties by property type
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByPropertyType(Property.PropertyType propertyType) {
        return propertyRepository.findByPropertyType(propertyType).stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get properties by listing type
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByListingType(Property.ListingType listingType) {
        return propertyRepository.findByListingType(listingType).stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get properties by status
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByStatus(Property.PropertyStatus status) {
        return propertyRepository.findByStatus(status).stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get available properties
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getAvailableProperties() {
        return propertyRepository.findAvailableProperties().stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get properties by city
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByCity(String city) {
        return propertyRepository.findByCity(city).stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get properties by province
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByProvince(String province) {
        return propertyRepository.findByProvince(province).stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get properties by price range
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return propertyRepository.findByPriceRange(minPrice, maxPrice).stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get properties by area range
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByAreaRange(BigDecimal minArea, BigDecimal maxArea) {
        return propertyRepository.findByLandAreaRange(minArea, maxArea).stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get properties by bedrooms
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByBedrooms(Integer bedrooms) {
        return propertyRepository.findByBedrooms(bedrooms).stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get properties by bathrooms
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByBathrooms(Integer bathrooms) {
        return propertyRepository.findByBathrooms(bathrooms).stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get featured properties
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getFeaturedProperties() {
        return propertyRepository.findByIsFeaturedTrue().stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get KPR eligible properties
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getKprEligibleProperties() {
        return propertyRepository.findByIsKprEligibleTrue().stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Search properties by title or description
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> searchProperties(String keyword) {
        List<Property> titleResults = propertyRepository.searchByTitle(keyword);
        List<Property> descriptionResults = propertyRepository.searchByDescription(keyword);

        // Combine results and remove duplicates
        titleResults.addAll(descriptionResults);
        return titleResults.stream()
                .distinct()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Get popular properties (sorted by view count)
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPopularProperties(Pageable pageable) {
        return propertyRepository.findMostViewedProperties().stream()
                .map(PropertyResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Update property status
     */
    public PropertyResponse updatePropertyStatus(Integer id, Property.PropertyStatus status) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found with id: " + id));

        property.setStatus(status);

        // Set published date if status is available and not already set
        if (status == Property.PropertyStatus.AVAILABLE && property.getPublishedAt() == null) {
            property.setPublishedAt(LocalDateTime.now());
        }

        Property updatedProperty = propertyRepository.save(property);
        return new PropertyResponse(updatedProperty);
    }

    /**
     * Increment view count
     */
    public PropertyResponse incrementViewCount(Integer id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found with id: " + id));

        property.setViewCount(property.getViewCount() + 1);
        Property updatedProperty = propertyRepository.save(property);
        return new PropertyResponse(updatedProperty);
    }

    /**
     * Increment inquiry count
     */
    public PropertyResponse incrementInquiryCount(Integer id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found with id: " + id));

        property.setInquiryCount(property.getInquiryCount() + 1);
        Property updatedProperty = propertyRepository.save(property);
        return new PropertyResponse(updatedProperty);
    }

    /**
     * Increment favorite count
     */
    public PropertyResponse incrementFavoriteCount(Integer id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found with id: " + id));

        property.setFavoriteCount(property.getFavoriteCount() + 1);
        Property updatedProperty = propertyRepository.save(property);
        return new PropertyResponse(updatedProperty);
    }

    /**
     * Update featured status
     */
    public PropertyResponse updateFeaturedStatus(Integer id, Boolean isFeatured) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found with id: " + id));

        property.setIsFeatured(isFeatured);
        Property updatedProperty = propertyRepository.save(property);
        return new PropertyResponse(updatedProperty);
    }

    /**
     * Delete property
     */
    public void deleteProperty(Integer id) {
        if (!propertyRepository.existsById(id)) {
            throw new IllegalArgumentException("Property not found with id: " + id);
        }
        propertyRepository.deleteById(id);
    }

    /**
     * Check if property code exists
     */
    @Transactional(readOnly = true)
    public boolean existsByPropertyCode(String propertyCode) {
        return propertyRepository.existsByPropertyCode(propertyCode);
    }

    /**
     * Check if slug exists
     */
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return propertyRepository.existsBySlug(slug);
    }
}
