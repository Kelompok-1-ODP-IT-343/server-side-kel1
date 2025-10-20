package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.ApiResponse;
import com.kelompoksatu.griya.dto.CreatePropertyRequest;
import com.kelompoksatu.griya.dto.PropertyResponse;
import com.kelompoksatu.griya.entity.Property;
import com.kelompoksatu.griya.service.PropertyService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** REST Controller for Property operations */
@RestController
@RequestMapping("/api/v1/properties")
@Validated
public class PropertyController {

  private final PropertyService propertyService;

  @Autowired
  public PropertyController(PropertyService propertyService) {
    this.propertyService = propertyService;
  }

  /** Create a new property */
  @PostMapping
  public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
      @Valid @RequestBody CreatePropertyRequest request) {
    try {
      PropertyResponse property = propertyService.createProperty(request);
      ApiResponse<PropertyResponse> response =
          new ApiResponse<>(true, "Property created successfully", property);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (IllegalArgumentException e) {
      ApiResponse<PropertyResponse> response = new ApiResponse<>(false, e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (Exception e) {
      ApiResponse<PropertyResponse> response =
          new ApiResponse<>(false, "Failed to create property: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * Get all properties, optionally filtered by query parameters Example:
   * /api/properties?city=Jakarta&status=AVAILABLE&minPrice=500000000&maxPrice=2000000000
   */
  @GetMapping
  public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllProperties(
      @RequestParam(required = false) String city,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(required = false, name = "propertyType") String propertyType,
      @RequestParam(defaultValue = "0") int offset,
      @RequestParam(defaultValue = "10") int limit) {

    try {
      List<Map<String, Object>> properties =
          propertyService.getPropertiesWithFilter(
              city, minPrice, maxPrice, propertyType, offset, limit);

      ApiResponse<List<Map<String, Object>>> response =
          new ApiResponse<>(true, "Properties retrieved successfully", properties);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      ApiResponse<List<Map<String, Object>>> response =
          new ApiResponse<>(false, "Failed to retrieve properties: " + e.getMessage(), null);

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get property by ID */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PropertyResponse>> getPropertyById(@PathVariable Integer id) {
    try {
      Optional<PropertyResponse> property = propertyService.getPropertyById(id);
      if (property.isPresent()) {
        ApiResponse<PropertyResponse> response =
            new ApiResponse<>(true, "Property retrieved successfully", property.get());
        return ResponseEntity.ok(response);
      } else {
        ApiResponse<PropertyResponse> response =
            new ApiResponse<>(false, "Property not found with id: " + id, null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }
    } catch (Exception e) {
      ApiResponse<PropertyResponse> response =
          new ApiResponse<>(false, "Failed to retrieve property: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get property by property code */
  @GetMapping("/code/{propertyCode}")
  public ResponseEntity<ApiResponse<PropertyResponse>> getPropertyByPropertyCode(
      @PathVariable String propertyCode) {
    try {
      Optional<PropertyResponse> property = propertyService.getPropertyByPropertyCode(propertyCode);
      if (property.isPresent()) {
        ApiResponse<PropertyResponse> response =
            new ApiResponse<>(true, "Property retrieved successfully", property.get());
        return ResponseEntity.ok(response);
      } else {
        ApiResponse<PropertyResponse> response =
            new ApiResponse<>(false, "Property not found with code: " + propertyCode, null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }
    } catch (Exception e) {
      ApiResponse<PropertyResponse> response =
          new ApiResponse<>(false, "Failed to retrieve property: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get property by slug */
  @GetMapping("/slug/{slug}")
  public ResponseEntity<ApiResponse<PropertyResponse>> getPropertyBySlug(
      @PathVariable String slug) {
    try {
      Optional<PropertyResponse> property = propertyService.getPropertyBySlug(slug);
      if (property.isPresent()) {
        // Increment view count when property is accessed by slug
        propertyService.incrementViewCount(property.get().getId());

        ApiResponse<PropertyResponse> response =
            new ApiResponse<>(true, "Property retrieved successfully", property.get());
        return ResponseEntity.ok(response);
      } else {
        ApiResponse<PropertyResponse> response =
            new ApiResponse<>(false, "Property not found with slug: " + slug, null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
      }
    } catch (Exception e) {
      ApiResponse<PropertyResponse> response =
          new ApiResponse<>(false, "Failed to retrieve property: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get properties by developer ID */
  @GetMapping("/developer/{developerId}")
  public ResponseEntity<ApiResponse<List<PropertyResponse>>> getPropertiesByDeveloperId(
      @PathVariable Integer developerId) {
    try {
      List<PropertyResponse> properties = propertyService.getPropertiesByDeveloperId(developerId);
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(
              true,
              "Properties for developer " + developerId + " retrieved successfully",
              properties);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(false, "Failed to retrieve properties: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get properties by property type */
  @GetMapping("/type/{propertyType}")
  public ResponseEntity<ApiResponse<List<PropertyResponse>>> getPropertiesByPropertyType(
      @PathVariable String propertyType) {
    try {
      Property.PropertyType type = Property.PropertyType.valueOf(propertyType.toUpperCase());
      List<PropertyResponse> properties = propertyService.getPropertiesByPropertyType(type);
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(
              true, "Properties of type " + propertyType + " retrieved successfully", properties);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(false, "Invalid property type: " + propertyType, null);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (Exception e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(false, "Failed to retrieve properties: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get properties by listing type */
  @GetMapping("/listing-type/{listingType}")
  public ResponseEntity<ApiResponse<List<PropertyResponse>>> getPropertiesByListingType(
      @PathVariable String listingType) {
    try {
      Property.ListingType type = Property.ListingType.valueOf(listingType.toUpperCase());
      List<PropertyResponse> properties = propertyService.getPropertiesByListingType(type);
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(
              true,
              "Properties with listing type " + listingType + " retrieved successfully",
              properties);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(false, "Invalid listing type: " + listingType, null);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (Exception e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(false, "Failed to retrieve properties: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get properties by status */
  @GetMapping("/status/{status}")
  public ResponseEntity<ApiResponse<List<PropertyResponse>>> getPropertiesByStatus(
      @PathVariable String status) {
    try {
      Property.PropertyStatus propertyStatus =
          Property.PropertyStatus.valueOf(status.toUpperCase());
      List<PropertyResponse> properties = propertyService.getPropertiesByStatus(propertyStatus);
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(
              true, "Properties with status " + status + " retrieved successfully", properties);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(false, "Invalid status: " + status, null);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (Exception e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(false, "Failed to retrieve properties: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get available properties */
  @GetMapping("/available")
  public ResponseEntity<ApiResponse<List<PropertyResponse>>> getAvailableProperties() {
    try {
      List<PropertyResponse> properties = propertyService.getAvailableProperties();
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(true, "Available properties retrieved successfully", properties);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(
              false, "Failed to retrieve available properties: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get properties by city */
  @GetMapping("/city/{city}")
  public ResponseEntity<ApiResponse<List<PropertyResponse>>> getPropertiesByCity(
      @PathVariable String city) {
    try {
      List<PropertyResponse> properties = propertyService.getPropertiesByCity(city);
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(true, "Properties in " + city + " retrieved successfully", properties);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(false, "Failed to retrieve properties: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get properties by province */
  @GetMapping("/province/{province}")
  public ResponseEntity<ApiResponse<List<PropertyResponse>>> getPropertiesByProvince(
      @PathVariable String province) {
    try {
      List<PropertyResponse> properties = propertyService.getPropertiesByProvince(province);
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(
              true, "Properties in " + province + " retrieved successfully", properties);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(false, "Failed to retrieve properties: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get properties by price range */
  @GetMapping("/price-range")
  public ResponseEntity<ApiResponse<List<PropertyResponse>>> getPropertiesByPriceRange(
      @RequestParam BigDecimal minPrice, @RequestParam BigDecimal maxPrice) {
    try {
      List<PropertyResponse> properties =
          propertyService.getPropertiesByPriceRange(minPrice, maxPrice);
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(
              true,
              "Properties in price range "
                  + minPrice
                  + " - "
                  + maxPrice
                  + " retrieved successfully",
              properties);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(false, "Failed to retrieve properties: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /**
   * Get properties with optional filters (city, status, minPrice, maxPrice) Example:
   * /api/properties/filter?city=Jakarta&status=AVAILABLE&minPrice=500000000&maxPrice=2000000000
   */

  /** Get properties by area range */
  @GetMapping("/area-range")
  public ResponseEntity<ApiResponse<List<PropertyResponse>>> getPropertiesByAreaRange(
      @RequestParam BigDecimal minArea, @RequestParam BigDecimal maxArea) {
    try {
      List<PropertyResponse> properties =
          propertyService.getPropertiesByAreaRange(minArea, maxArea);
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(
              true,
              "Properties in area range "
                  + minArea
                  + " - "
                  + maxArea
                  + " m² retrieved successfully",
              properties);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(false, "Failed to retrieve properties: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get properties by bedrooms */
  @GetMapping("/bedrooms/{bedrooms}")
  public ResponseEntity<ApiResponse<List<PropertyResponse>>> getPropertiesByBedrooms(
      @PathVariable Integer bedrooms) {
    try {
      List<PropertyResponse> properties = propertyService.getPropertiesByBedrooms(bedrooms);
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(
              true, "Properties with " + bedrooms + " bedrooms retrieved successfully", properties);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(false, "Failed to retrieve properties: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get properties by bathrooms */
  @GetMapping("/bathrooms/{bathrooms}")
  public ResponseEntity<ApiResponse<List<PropertyResponse>>> getPropertiesByBathrooms(
      @PathVariable Integer bathrooms) {
    try {
      List<PropertyResponse> properties = propertyService.getPropertiesByBathrooms(bathrooms);
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(
              true,
              "Properties with " + bathrooms + " bathrooms retrieved successfully",
              properties);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(false, "Failed to retrieve properties: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get featured properties */
  @GetMapping("/featured")
  public ResponseEntity<ApiResponse<List<PropertyResponse>>> getFeaturedProperties() {
    try {
      List<PropertyResponse> properties = propertyService.getFeaturedProperties();
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(true, "Featured properties retrieved successfully", properties);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(
              false, "Failed to retrieve featured properties: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get KPR eligible properties */
  @GetMapping("/kpr-eligible")
  public ResponseEntity<ApiResponse<List<PropertyResponse>>> getKprEligibleProperties() {
    try {
      List<PropertyResponse> properties = propertyService.getKprEligibleProperties();
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(true, "KPR eligible properties retrieved successfully", properties);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(
              false, "Failed to retrieve KPR eligible properties: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Search properties by title or description */
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<PropertyResponse>>> searchProperties(
      @RequestParam String keyword) {
    try {
      List<PropertyResponse> properties = propertyService.searchProperties(keyword);
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(
              true, "Search results for '" + keyword + "' retrieved successfully", properties);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(false, "Failed to search properties: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Get popular properties (sorted by view count) */
  @GetMapping("/popular")
  public ResponseEntity<ApiResponse<List<PropertyResponse>>> getPopularProperties(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    try {
      Pageable pageable = PageRequest.of(page, size);
      List<PropertyResponse> properties = propertyService.getPopularProperties(pageable);
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(true, "Popular properties retrieved successfully", properties);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<List<PropertyResponse>> response =
          new ApiResponse<>(
              false, "Failed to retrieve popular properties: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Update property status */
  @PutMapping("/{id}/status")
  public ResponseEntity<ApiResponse<PropertyResponse>> updatePropertyStatus(
      @PathVariable Integer id, @RequestParam String status) {
    try {
      Property.PropertyStatus propertyStatus =
          Property.PropertyStatus.valueOf(status.toUpperCase());
      PropertyResponse property = propertyService.updatePropertyStatus(id, propertyStatus);
      ApiResponse<PropertyResponse> response =
          new ApiResponse<>(true, "Property status updated successfully", property);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      ApiResponse<PropertyResponse> response = new ApiResponse<>(false, e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (Exception e) {
      ApiResponse<PropertyResponse> response =
          new ApiResponse<>(false, "Failed to update property status: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Increment inquiry count */
  @PostMapping("/{id}/inquiry")
  public ResponseEntity<ApiResponse<PropertyResponse>> incrementInquiryCount(
      @PathVariable Integer id) {
    try {
      PropertyResponse property = propertyService.incrementInquiryCount(id);
      ApiResponse<PropertyResponse> response =
          new ApiResponse<>(true, "Inquiry count incremented successfully", property);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      ApiResponse<PropertyResponse> response = new ApiResponse<>(false, e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (Exception e) {
      ApiResponse<PropertyResponse> response =
          new ApiResponse<>(false, "Failed to increment inquiry count: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Increment favorite count */
  @PostMapping("/{id}/favorite")
  public ResponseEntity<ApiResponse<PropertyResponse>> incrementFavoriteCount(
      @PathVariable Integer id) {
    try {
      PropertyResponse property = propertyService.incrementFavoriteCount(id);
      ApiResponse<PropertyResponse> response =
          new ApiResponse<>(true, "Favorite count incremented successfully", property);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      ApiResponse<PropertyResponse> response = new ApiResponse<>(false, e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (Exception e) {
      ApiResponse<PropertyResponse> response =
          new ApiResponse<>(false, "Failed to increment favorite count: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Update featured status */
  @PutMapping("/{id}/featured")
  public ResponseEntity<ApiResponse<PropertyResponse>> updateFeaturedStatus(
      @PathVariable Integer id, @RequestParam Boolean isFeatured) {
    try {
      PropertyResponse property = propertyService.updateFeaturedStatus(id, isFeatured);
      ApiResponse<PropertyResponse> response =
          new ApiResponse<>(true, "Property featured status updated successfully", property);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      ApiResponse<PropertyResponse> response = new ApiResponse<>(false, e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    } catch (Exception e) {
      ApiResponse<PropertyResponse> response =
          new ApiResponse<>(false, "Failed to update featured status: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Delete property */
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteProperty(@PathVariable Integer id) {
    try {
      propertyService.deleteProperty(id);
      ApiResponse<Void> response = new ApiResponse<>(true, "Property deleted successfully", null);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      ApiResponse<Void> response = new ApiResponse<>(false, e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    } catch (Exception e) {
      ApiResponse<Void> response =
          new ApiResponse<>(false, "Failed to delete property: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Check if property code exists */
  @GetMapping("/exists/code/{propertyCode}")
  public ResponseEntity<ApiResponse<Boolean>> checkPropertyCodeExists(
      @PathVariable String propertyCode) {
    try {
      boolean exists = propertyService.existsByPropertyCode(propertyCode);
      ApiResponse<Boolean> response =
          new ApiResponse<>(true, "Property code existence check completed", exists);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<Boolean> response =
          new ApiResponse<>(
              false, "Failed to check property code existence: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  /** Check if slug exists */
  @GetMapping("/exists/slug/{slug}")
  public ResponseEntity<ApiResponse<Boolean>> checkSlugExists(@PathVariable String slug) {
    try {
      boolean exists = propertyService.existsBySlug(slug);
      ApiResponse<Boolean> response =
          new ApiResponse<>(true, "Slug existence check completed", exists);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<Boolean> response =
          new ApiResponse<>(false, "Failed to check slug existence: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @GetMapping("/{id}/details")
  public ResponseEntity<ApiResponse<Map<String, Object>>> getPropertyDetails(
      @PathVariable Integer id) {
    try {
      Map<String, Object> propertyDetail = propertyService.getPropertyDetails(id);
      ApiResponse<Map<String, Object>> response =
          new ApiResponse<>(true, "Property detail retrieved successfully", propertyDetail);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      ApiResponse<Map<String, Object>> response =
          new ApiResponse<>(false, "Failed to retrieve property detail: " + e.getMessage(), null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }
}
