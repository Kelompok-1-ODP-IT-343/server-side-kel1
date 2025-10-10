package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.Developer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for developer response
 */
public class DeveloperResponse {

    private Integer id;
    private String companyName;
    private String companyCode;
    private String businessLicense;
    private String developerLicense;

    // Contact Information
    private String contactPerson;
    private String phone;
    private String email;
    private String website;

    // Address
    private String address;
    private String city;
    private String province;
    private String postalCode;

    // Business Details
    private Integer establishedYear;
    private String description;
    private Developer.Specialization specialization;

    // Partnership
    private Boolean isPartner;
    private Developer.PartnershipLevel partnershipLevel;
    private BigDecimal commissionRate;

    // Status
    private Developer.DeveloperStatus status;
    private LocalDateTime verifiedAt;
    private Integer verifiedBy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public DeveloperResponse() {}

    // Constructor from Developer entity
    public DeveloperResponse(Developer developer) {
        this.id = developer.getId();
        this.companyName = developer.getCompanyName();
        this.companyCode = developer.getCompanyCode();
        this.businessLicense = developer.getBusinessLicense();
        this.developerLicense = developer.getDeveloperLicense();
        this.contactPerson = developer.getContactPerson();
        this.phone = developer.getPhone();
        this.email = developer.getEmail();
        this.website = developer.getWebsite();
        this.address = developer.getAddress();
        this.city = developer.getCity();
        this.province = developer.getProvince();
        this.postalCode = developer.getPostalCode();
        this.establishedYear = developer.getEstablishedYear();
        this.description = developer.getDescription();
        this.specialization = developer.getSpecialization();
        this.isPartner = developer.getIsPartner();
        this.partnershipLevel = developer.getPartnershipLevel();
        this.commissionRate = developer.getCommissionRate();
        this.status = developer.getStatus();
        this.verifiedAt = developer.getVerifiedAt();
        this.verifiedBy = developer.getVerifiedBy();
        this.createdAt = developer.getCreatedAt();
        this.updatedAt = developer.getUpdatedAt();
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

    public Developer.Specialization getSpecialization() {
        return specialization;
    }

    public void setSpecialization(Developer.Specialization specialization) {
        this.specialization = specialization;
    }

    public Boolean getIsPartner() {
        return isPartner;
    }

    public void setIsPartner(Boolean isPartner) {
        this.isPartner = isPartner;
    }

    public Developer.PartnershipLevel getPartnershipLevel() {
        return partnershipLevel;
    }

    public void setPartnershipLevel(Developer.PartnershipLevel partnershipLevel) {
        this.partnershipLevel = partnershipLevel;
    }

    public BigDecimal getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }

    public Developer.DeveloperStatus getStatus() {
        return status;
    }

    public void setStatus(Developer.DeveloperStatus status) {
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