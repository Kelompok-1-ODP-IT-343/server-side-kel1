package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.Developer;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO for creating a new developer
 */
public class CreateDeveloperRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String companyName;

    @NotBlank(message = "Company code is required")
    @Size(max = 20, message = "Company code must not exceed 20 characters")
    private String companyCode;

    @NotBlank(message = "Business license is required")
    @Size(max = 100, message = "Business license must not exceed 100 characters")
    private String businessLicense; // SIUP

    @NotBlank(message = "Developer license is required")
    @Size(max = 100, message = "Developer license must not exceed 100 characters")
    private String developerLicense; // Izin Developer

    // Contact Information
    @NotBlank(message = "Contact person is required")
    @Size(max = 100, message = "Contact person must not exceed 100 characters")
    private String contactPerson;

    @NotBlank(message = "Phone is required")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 255, message = "Website must not exceed 255 characters")
    private String website;

    // Address
    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "Province is required")
    @Size(max = 100, message = "Province must not exceed 100 characters")
    private String province;

    @NotBlank(message = "Postal code is required")
    @Size(max = 10, message = "Postal code must not exceed 10 characters")
    private String postalCode;

    // Business Details
    @Min(value = 1900, message = "Established year must be after 1900")
    @Max(value = 2100, message = "Established year must be before 2100")
    private Integer establishedYear;

    private String description;

    private Developer.Specialization specialization;

    // Partnership
    private Boolean isPartner = false;

    private Developer.PartnershipLevel partnershipLevel;

    @DecimalMin(value = "0.0000", message = "Commission rate must be non-negative")
    @DecimalMax(value = "1.0000", message = "Commission rate must not exceed 100%")
    private BigDecimal commissionRate = new BigDecimal("0.0250"); // 2.5% default

    // Default constructor
    public CreateDeveloperRequest() {}

    // Getters and Setters
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
}