package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.CreateDeveloperRequest;
import com.kelompoksatu.griya.dto.DeveloperResponse;
import com.kelompoksatu.griya.entity.Developer;
import com.kelompoksatu.griya.repository.DeveloperRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service class for Developer business logic */
@Service
@Transactional
public class DeveloperService {

  private final DeveloperRepository developerRepository;

  @Autowired
  public DeveloperService(DeveloperRepository developerRepository) {
    this.developerRepository = developerRepository;
  }

  /** Create a new developer */
  public DeveloperResponse createDeveloper(CreateDeveloperRequest request) {
    // Validate unique constraints
    if (developerRepository.existsByCompanyCode(request.getCompanyCode())) {
      throw new IllegalArgumentException(
          "Company code already exists: " + request.getCompanyCode());
    }

    if (developerRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email already exists: " + request.getEmail());
    }

    // Create new developer entity
    Developer developer = new Developer();
    developer.setCompanyName(request.getCompanyName());
    developer.setCompanyCode(request.getCompanyCode());
    developer.setBusinessLicense(request.getBusinessLicense());
    developer.setDeveloperLicense(request.getDeveloperLicense());
    developer.setContactPerson(request.getContactPerson());
    developer.setPhone(request.getPhone());
    developer.setEmail(request.getEmail());
    developer.setWebsite(request.getWebsite());
    developer.setAddress(request.getAddress());
    developer.setCity(request.getCity());
    developer.setProvince(request.getProvince());
    developer.setPostalCode(request.getPostalCode());
    developer.setEstablishedYear(request.getEstablishedYear());
    developer.setDescription(request.getDescription());
    developer.setSpecialization(request.getSpecialization());
    developer.setIsPartner(request.getIsPartner());
    developer.setPartnershipLevel(request.getPartnershipLevel());
    developer.setCommissionRate(request.getCommissionRate());

    // Set default status as ACTIVE
    developer.setStatus(Developer.DeveloperStatus.ACTIVE);

    // Save the developer
    Developer savedDeveloper = developerRepository.save(developer);

    return new DeveloperResponse(savedDeveloper);
  }

  /** Get developer by ID */
  @Transactional(readOnly = true)
  public Optional<DeveloperResponse> getDeveloperById(Integer id) {
    return developerRepository.findById(id).map(DeveloperResponse::new);
  }

  /** Get developer by company code */
  @Transactional(readOnly = true)
  public Optional<DeveloperResponse> getDeveloperByCompanyCode(String companyCode) {
    return developerRepository.findByCompanyCode(companyCode).map(DeveloperResponse::new);
  }

  /** Get all developers */
  @Transactional(readOnly = true)
  public List<DeveloperResponse> getAllDevelopers() {
    return developerRepository.findAll().stream()
        .map(DeveloperResponse::new)
        .collect(Collectors.toList());
  }

  /** Get active developers */
  @Transactional(readOnly = true)
  public List<DeveloperResponse> getActiveDevelopers() {
    return developerRepository.findActiveDevelopers().stream()
        .map(DeveloperResponse::new)
        .collect(Collectors.toList());
  }

  /** Get developers by status */
  @Transactional(readOnly = true)
  public List<DeveloperResponse> getDevelopersByStatus(Developer.DeveloperStatus status) {
    return developerRepository.findByStatus(status).stream()
        .map(DeveloperResponse::new)
        .collect(Collectors.toList());
  }

  /** Get partner developers */
  @Transactional(readOnly = true)
  public List<DeveloperResponse> getPartnerDevelopers() {
    return developerRepository.findByIsPartner(true).stream()
        .map(DeveloperResponse::new)
        .collect(Collectors.toList());
  }

  /** Get developers by specialization */
  @Transactional(readOnly = true)
  public List<DeveloperResponse> getDevelopersBySpecialization(
      Developer.Specialization specialization) {
    return developerRepository.findBySpecialization(specialization).stream()
        .map(DeveloperResponse::new)
        .collect(Collectors.toList());
  }

  /** Get developers by city */
  @Transactional(readOnly = true)
  public List<DeveloperResponse> getDevelopersByCity(String city) {
    return developerRepository.findByCity(city).stream()
        .map(DeveloperResponse::new)
        .collect(Collectors.toList());
  }

  /** Get developers by province */
  @Transactional(readOnly = true)
  public List<DeveloperResponse> getDevelopersByProvince(String province) {
    return developerRepository.findByProvince(province).stream()
        .map(DeveloperResponse::new)
        .collect(Collectors.toList());
  }

  /** Search developers by company name */
  @Transactional(readOnly = true)
  public List<DeveloperResponse> searchDevelopersByCompanyName(String companyName) {
    return developerRepository.searchByCompanyName(companyName).stream()
        .map(DeveloperResponse::new)
        .collect(Collectors.toList());
  }

  /** Update developer status */
  public DeveloperResponse updateDeveloperStatus(Integer id, Developer.DeveloperStatus status) {
    Developer developer =
        developerRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Developer not found with id: " + id));

    developer.setStatus(status);
    Developer updatedDeveloper = developerRepository.save(developer);

    return new DeveloperResponse(updatedDeveloper);
  }

  /** Verify developer */
  public DeveloperResponse verifyDeveloper(Integer id, Integer verifiedBy) {
    Developer developer =
        developerRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Developer not found with id: " + id));

    developer.setVerifiedAt(LocalDateTime.now());
    developer.setVerifiedBy(verifiedBy);
    Developer updatedDeveloper = developerRepository.save(developer);

    return new DeveloperResponse(updatedDeveloper);
  }

  /** Update partnership status */
  public DeveloperResponse updatePartnershipStatus(
      Integer id, Boolean isPartner, Developer.PartnershipLevel partnershipLevel) {
    Developer developer =
        developerRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Developer not found with id: " + id));

    developer.setIsPartner(isPartner);
    developer.setPartnershipLevel(partnershipLevel);
    Developer updatedDeveloper = developerRepository.save(developer);

    return new DeveloperResponse(updatedDeveloper);
  }

  /** Delete developer */
  public void deleteDeveloper(Integer id) {
    if (!developerRepository.existsById(id)) {
      throw new IllegalArgumentException("Developer not found with id: " + id);
    }
    developerRepository.deleteById(id);
  }

  /** Check if company code exists */
  @Transactional(readOnly = true)
  public boolean existsByCompanyCode(String companyCode) {
    return developerRepository.existsByCompanyCode(companyCode);
  }

  /** Check if email exists */
  @Transactional(readOnly = true)
  public boolean existsByEmail(String email) {
    return developerRepository.existsByEmail(email);
  }
}
