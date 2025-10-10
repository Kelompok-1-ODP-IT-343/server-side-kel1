package com.kelompoksatu.griya.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Developer entity representing property developers and real estate companies
 */
@Entity
@Table(name = "developers")
public class Developer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "company_name", length = 255, nullable = false)
    private String companyName;

    @Column(name = "company_code", length = 20, unique = true, nullable = false)
    private String companyCode;

    @Column(name = "business_license", length = 100, nullable = false)
    private String businessLicense; // SIUP

    @Column(name = "developer_license", length = 100, nullable = false)
    private String developerLicense; // Izin Developer

    // Contact Information
    @Column(name = "contact_person", length = 100, nullable = false)
    private String contactPerson;

    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    @Column(name = "email", length = 100, nullable = false)
    private String email;

    @Column(name = "website", length = 255)
    private String website;

    // Address
    @Column(name = "address", columnDefinition = "TEXT", nullable = false)
    private String address;

    @Column(name = "city", length = 100, nullable = false)
    private String city;

    @Column(name = "province", length = 100, nullable = false)
    private String province;

    @Column(name = "postal_code", length = 10, nullable = false)
    private String postalCode;

    // Business Details
    @Column(name = "established_year")
    private Integer establishedYear;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "specialization")
    private Specialization specialization;

    // Partnership
    @Column(name = "is_partner", nullable = false)
    private Boolean isPartner = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "partnership_level")
    private PartnershipLevel partnershipLevel;

    @Column(name = "commission_rate", precision = 5, scale = 4)
    private java.math.BigDecimal commissionRate = new java.math.BigDecimal("0.0250"); // 2.5% default

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeveloperStatus status = DeveloperStatus.ACTIVE;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private Integer verifiedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Enums
    public enum Specialization {
        RESIDENTIAL, COMMERCIAL, MIXED, INDUSTRIAL
    }

    public enum PartnershipLevel {
        BRONZE, SILVER, GOLD, PLATINUM
    }

    public enum DeveloperStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }

    // Default constructor
    public Developer() {}

    // Constructor with essential parameters
    public Developer(String companyName, String companyCode, String businessLicense, 
                    String developerLicense, String contactPerson, String phone, 
                    String email, String address, String city, String province, 
                    String postalCode) {
        this.companyName = companyName;
        this.companyCode = companyCode;
        this.businessLicense = businessLicense;
        this.developerLicense = developerLicense;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.city = city;
        this.province = province;
        this.postalCode = postalCode;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getBusinessLicense() {
        return businessLicense;
    }

    public void setBusinessLicense(String businessLicense) {
        this.businessLicense = businessLicense;
    }

    public String getDeveloperLicense() {
        return developerLicense;
    }

    public void setDeveloperLicense(String developerLicense) {
        this.developerLicense = developerLicense;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
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

    public Integer getEstablishedYear() {
        return establishedYear;
    }

    public void setEstablishedYear(Integer establishedYear) {
        this.establishedYear = establishedYear;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Specialization getSpecialization() {
        return specialization;
    }

    public void setSpecialization(Specialization specialization) {
        this.specialization = specialization;
    }

    public Boolean getIsPartner() {
        return isPartner;
    }

    public void setIsPartner(Boolean isPartner) {
        this.isPartner = isPartner;
    }

    public PartnershipLevel getPartnershipLevel() {
        return partnershipLevel;
    }

    public void setPartnershipLevel(PartnershipLevel partnershipLevel) {
        this.partnershipLevel = partnershipLevel;
    }

    public java.math.BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(java.math.BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public DeveloperStatus getStatus() {
        return status;
    }

    public void setStatus(DeveloperStatus status) {
        this.status = status;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public Integer getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(Integer verifiedBy) {
        this.verifiedBy = verifiedBy;
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
}