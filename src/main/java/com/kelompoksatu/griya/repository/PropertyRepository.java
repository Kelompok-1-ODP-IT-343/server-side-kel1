package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.Property;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository interface for Property entity */
@Repository
public interface PropertyRepository extends JpaRepository<Property, Integer> {

  /** Find property by property code */
  Optional<Property> findByPropertyCode(String propertyCode);

  /** Find property by slug */
  Optional<Property> findBySlug(String slug);

  /** Find properties by developer ID */
  List<Property> findByDeveloperId(Integer developerId);

  /** Find properties by property type */
  List<Property> findByPropertyType(Property.PropertyType propertyType);

  /** Find properties by listing type */
  List<Property> findByListingType(Property.ListingType listingType);

  /** Find properties by status */
  List<Property> findByStatus(Property.PropertyStatus status);

  /** Find properties by city */
  List<Property> findByCity(String city);

  /** Find properties by province */
  List<Property> findByProvince(String province);

  /** Find properties by certificate type */
  List<Property> findByCertificateType(Property.CertificateType certificateType);

  /** Find featured properties */
  List<Property> findByIsFeaturedTrue();

  /** Find KPR eligible properties */
  List<Property> findByIsKprEligibleTrue();

  /** Check if property code exists */
  boolean existsByPropertyCode(String propertyCode);

  /** Check if slug exists */
  boolean existsBySlug(String slug);

  boolean existsByPropertyCodeAndIdNot(String propertyCode, Integer id);

  boolean existsBySlugAndIdNot(String slug, Integer id);

  /** Find available properties */
  @Query("SELECT p FROM Property p WHERE p.status = 'AVAILABLE'")
  List<Property> findAvailableProperties();

  /** Find properties by price range */
  @Query("SELECT p FROM Property p WHERE p.price BETWEEN :minPrice AND :maxPrice")
  List<Property> findByPriceRange(
      @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

  /** Find properties by bedrooms count */
  List<Property> findByBedrooms(Integer bedrooms);

  /** Find properties by bathrooms count */
  List<Property> findByBathrooms(Integer bathrooms);

  /** Find properties by land area range */
  @Query("SELECT p FROM Property p WHERE p.landArea BETWEEN :minArea AND :maxArea")
  List<Property> findByLandAreaRange(
      @Param("minArea") BigDecimal minArea, @Param("maxArea") BigDecimal maxArea);

  /** Find properties by building area range */
  @Query("SELECT p FROM Property p WHERE p.buildingArea BETWEEN :minArea AND :maxArea")
  List<Property> findByBuildingAreaRange(
      @Param("minArea") BigDecimal minArea, @Param("maxArea") BigDecimal maxArea);

  /** Search properties by title (case-insensitive) */
  @Query("SELECT p FROM Property p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%'))")
  List<Property> searchByTitle(@Param("title") String title);

  /** Search properties by description (case-insensitive) */
  @Query(
      "SELECT p FROM Property p WHERE LOWER(p.description) LIKE LOWER(CONCAT('%', :description, '%'))")
  List<Property> searchByDescription(@Param("description") String description);

  /** Find properties by availability date range */
  @Query("SELECT p FROM Property p WHERE p.availabilityDate BETWEEN :startDate AND :endDate")
  List<Property> findByAvailabilityDateRange(
      @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  /** Find properties by year built range */
  @Query("SELECT p FROM Property p WHERE p.yearBuilt BETWEEN :startYear AND :endYear")
  List<Property> findByYearBuiltRange(
      @Param("startYear") Integer startYear, @Param("endYear") Integer endYear);

  /** Find properties within geographic bounds */
  @Query(
      "SELECT p FROM Property p WHERE p.latitude BETWEEN :minLat AND :maxLat AND p.longitude BETWEEN :minLng AND :maxLng")
  List<Property> findWithinGeographicBounds(
      @Param("minLat") BigDecimal minLatitude,
      @Param("maxLat") BigDecimal maxLatitude,
      @Param("minLng") BigDecimal minLongitude,
      @Param("maxLng") BigDecimal maxLongitude);

  /** Find most viewed properties */
  @Query("SELECT p FROM Property p ORDER BY p.viewCount DESC")
  List<Property> findMostViewedProperties();

  /** Find most inquired properties */
  @Query("SELECT p FROM Property p ORDER BY p.inquiryCount DESC")
  List<Property> findMostInquiredProperties();

  /** Find most favorited properties */
  @Query("SELECT p FROM Property p ORDER BY p.favoriteCount DESC")
  List<Property> findMostFavoritedProperties();

  /** Find recently published properties */
  @Query("SELECT p FROM Property p WHERE p.publishedAt IS NOT NULL ORDER BY p.publishedAt DESC")
  List<Property> findRecentlyPublishedProperties();

  /** Find properties by multiple criteria */
  @Query(
      "SELECT p FROM Property p WHERE "
          + "(:propertyType IS NULL OR p.propertyType = :propertyType) AND "
          + "(:city IS NULL OR p.city = :city) AND "
          + "(:minPrice IS NULL OR p.price >= :minPrice) AND "
          + "(:maxPrice IS NULL OR p.price <= :maxPrice) AND "
          + "(:bedrooms IS NULL OR p.bedrooms = :bedrooms) AND "
          + "(:status IS NULL OR p.status = :status)")
  List<Property> findByCriteria(
      @Param("propertyType") Property.PropertyType propertyType,
      @Param("city") String city,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      @Param("bedrooms") Integer bedrooms,
      @Param("status") Property.PropertyStatus status);

  @Query(
      value =
          """
    SELECT
        p.id,
        p.property_code,
        p.title,
        p.city,
        p.price,
        p.property_type,
        p.listing_type,
        -- ambil nama file utama
        COALESCE(
            (SELECT pi.file_name FROM property_images pi
             WHERE pi.property_id = p.id AND pi.is_primary = TRUE
             ORDER BY pi.id ASC
             LIMIT 1),
            (SELECT pi.file_name FROM property_images pi
             WHERE pi.property_id = p.id
             ORDER BY pi.id ASC
             LIMIT 1)
        ) AS file_name,
        -- ambil file path utama (URL)
        COALESCE(
            (SELECT pi.file_path FROM property_images pi
             WHERE pi.property_id = p.id AND pi.is_primary = TRUE
             ORDER BY pi.id ASC
             LIMIT 1),
            (SELECT pi.file_path FROM property_images pi
             WHERE pi.property_id = p.id
             ORDER BY pi.id ASC
             LIMIT 1)
        ) AS file_path,
        STRING_AGG(DISTINCT pf.feature_name || ' : ' || pf.feature_value, ', ') AS features,
        STRING_AGG(DISTINCT pl.poi_name || ' (' || pl.distance_km || ' km)', ', ') AS nearby_places
    FROM properties p
    LEFT JOIN property_features pf ON pf.property_id = p.id
    LEFT JOIN property_locations pl ON pl.property_id = p.id
        WHERE p.status = 'AVAILABLE'
            AND (:city IS NULL OR p.city ILIKE CONCAT('%', :city, '%'))
      AND (:minPrice IS NULL OR p.price >= :minPrice)
      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
      AND (:propertyType IS NULL OR LOWER(p.property_type::text) = LOWER(:propertyType))
    GROUP BY p.id
    ORDER BY p.id
    LIMIT :limit OFFSET :offset
    """,
      nativeQuery = true)
  List<Map<String, Object>> findPropertiesWithFilter(
      @Param("city") String city,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      @Param("propertyType") String propertyType,
      @Param("offset") int offset,
      @Param("limit") int limit);

  @Query(
      value =
          """
SELECT
    p.id,
    p.property_code,
    p.title,
    p.city,
    p.price,
    p.property_type,
    p.listing_type,
    p.description,
    p.developer_id,
    COALESCE(
        json_agg(DISTINCT pi.file_path) FILTER (WHERE pi.file_path IS NOT NULL),
        '[]'
    ) AS images,
    json_agg(DISTINCT jsonb_build_object('featureName', pf.feature_name, 'featureValue', pf.feature_value)) AS features,
    json_agg(DISTINCT jsonb_build_object('poiName', pl.poi_name, 'distanceKm', pl.distance_km)) AS locations
FROM properties p
LEFT JOIN property_images pi ON pi.property_id = p.id
LEFT JOIN property_features pf ON pf.property_id = p.id
LEFT JOIN property_locations pl ON pl.property_id = p.id
WHERE p.id = :id
GROUP BY p.id
""",
      nativeQuery = true)
  Map<String, Object> findPropertyDetailsById(@Param("id") Integer id);

  @Query(
      value =
          """
    SELECT
      p.id,
      p.property_type,
      p.title,
      p.description,
      p.address,
      p.village AS sub_district,
      p.district,
      p.city,
      p.province,
      p.postal_code,
      p.latitude,
      p.longitude,
      p.land_area,
      p.building_area,
      p.bedrooms,
      p.bathrooms,
      p.floors,
      p.garage,
      p.year_built,
      p.price,
      p.price_per_sqm,
      p.maintenance_fee,
      p.certificate_type,
      p.pbb_value,
      d.company_name AS developer_name,
      COALESCE(
        (SELECT pi.file_path
         FROM property_images pi
         WHERE pi.property_id = p.id AND pi.is_primary = TRUE
         ORDER BY pi.id ASC
         LIMIT 1),
        (SELECT pi.file_path
         FROM property_images pi
         WHERE pi.property_id = p.id
         ORDER BY pi.id ASC
         LIMIT 1)
      ) AS image_url
    FROM properties p
    JOIN developers d ON d.id = p.developer_id
    WHERE p.status = 'AVAILABLE'
      AND (:city IS NULL OR p.city ILIKE CONCAT('%', :city, '%'))
      AND (:minPrice IS NULL OR p.price >= :minPrice)
      AND (:maxPrice IS NULL OR p.price <= :maxPrice)
      AND (:propertyType IS NULL OR LOWER(p.property_type::text) = LOWER(:propertyType))
    ORDER BY p.id
    """,
      nativeQuery = true)
  List<Map<String, Object>> findPropertiesSimpleByFilters(
      @Param("city") String city,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      @Param("propertyType") String propertyType);
}
