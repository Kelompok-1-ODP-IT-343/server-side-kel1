package com.kelompoksatu.griya.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

/** Property entity representing comprehensive property listings with detailed specifications. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
  @JdbcType(PostgreSQLEnumJdbcType.class)
  @Column(name = "property_type", nullable = false)
  private PropertyType propertyType;

  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  @Column(name = "listing_type", nullable = false)
  private ListingType listingType;

  @Column(length = 255, nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String description;

  // ===== LOCATION DETAILS =====
  @Column(columnDefinition = "TEXT", nullable = false)
  private String address;

  @Column(length = 100, nullable = false)
  private String city;

  @Column(length = 100, nullable = false)
  private String province;

  @Column(name = "postal_code", length = 10, nullable = false)
  private String postalCode;

  @Column(length = 100, nullable = false)
  private String district; // Kecamatan

  @Column(length = 100, nullable = false)
  private String village; // Kelurahan

  @Column(precision = 10, scale = 8)
  private BigDecimal latitude;

  @Column(precision = 11, scale = 8)
  private BigDecimal longitude;

  // ===== SPECIFICATIONS =====
  @Column(name = "land_area", precision = 8, scale = 2, nullable = false)
  private BigDecimal landArea;

  @Column(name = "building_area", precision = 8, scale = 2, nullable = false)
  private BigDecimal buildingArea;

  private Integer bedrooms;
  private Integer bathrooms;
  private Integer floors = 1;
  private Integer garage = 0;
  private Integer yearBuilt;

  // ===== PRICING =====
  @Column(precision = 15, scale = 2, nullable = false)
  private BigDecimal price;

  @Column(name = "price_per_sqm", precision = 10, scale = 2, nullable = false)
  private BigDecimal pricePerSqm;

  @Column(name = "maintenance_fee", precision = 10, scale = 2)
  private BigDecimal maintenanceFee = BigDecimal.ZERO; // For apartments

  // ===== LEGAL =====
  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  @Column(name = "certificate_type", nullable = false)
  private CertificateType certificateType;

  @Column(length = 100)
  private String certificateNumber;

  @Column(precision = 8, scale = 2)
  private BigDecimal certificateArea;

  @Column(precision = 15, scale = 2)
  private BigDecimal pbbValue;

  // ===== AVAILABILITY =====
  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  private PropertyStatus status = PropertyStatus.AVAILABLE;

  private LocalDate availabilityDate;
  private LocalDate handoverDate;

  // ===== MARKETING =====
  private Boolean isFeatured = false;
  private Boolean isKprEligible = true;

  @Column(precision = 5, scale = 2)
  private BigDecimal minDownPaymentPercent = new BigDecimal("20.00");

  private Integer maxLoanTermYears = 20;

  // ===== SEO =====
  @Column(length = 255, unique = true, nullable = false)
  private String slug;

  private String metaTitle;

  @Column(columnDefinition = "TEXT")
  private String metaDescription;

  @Column(columnDefinition = "TEXT")
  private String keywords;

  // ===== TRACKING =====
  private Integer viewCount = 0;
  private Integer inquiryCount = 0;
  private Integer favoriteCount = 0;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  private LocalDateTime publishedAt;

  // ===== RELATIONSHIPS =====
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "developer_id", insertable = false, updatable = false)
  private Developer developer;

  @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<PropertyImage> images = new ArrayList<>();

  @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<PropertyFeature> features = new ArrayList<>();

  @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<PropertyLocation> locations = new ArrayList<>();

  // ===== ENUMS =====
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
    SECONDARY
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
}
