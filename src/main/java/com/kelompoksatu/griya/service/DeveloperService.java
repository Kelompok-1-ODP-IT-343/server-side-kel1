package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.CreateDeveloperRequest;
import com.kelompoksatu.griya.dto.DeveloperResponse;
import com.kelompoksatu.griya.dto.PaginatedResponse;
import com.kelompoksatu.griya.dto.PaginationRequest;
import com.kelompoksatu.griya.dto.UpdateDeveloperRequest;
import com.kelompoksatu.griya.entity.Developer;
import com.kelompoksatu.griya.entity.User;
import com.kelompoksatu.griya.mapper.DeveloperMapper;
import com.kelompoksatu.griya.repository.DeveloperRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service class for Developer business logic */
@Service
@Transactional
@RequiredArgsConstructor
public class DeveloperService {

  private final DeveloperRepository developerRepository;
  private final DeveloperMapper developerMapper;

  // ========================================
  // CRUD OPERATIONS
  // ========================================

  /** Create a new developer with user relationship (for registration flow) */
  public DeveloperResponse createDeveloper(CreateDeveloperRequest request, User user) {
    validateCreateDeveloperRequest(request);

    Developer developer = createDeveloperEntity(request);
    developer.setUser(user);

    Developer savedDeveloper = developerRepository.save(developer);
    return developerMapper.toResponse(savedDeveloper);
  }

  /** Create a new developer without user relationship (for admin direct creation) */
  public DeveloperResponse createDeveloper(CreateDeveloperRequest request) {
    validateCreateDeveloperRequest(request);

    Developer developer = createDeveloperEntity(request);

    Developer savedDeveloper = developerRepository.save(developer);
    return developerMapper.toResponse(savedDeveloper);
  }

  /** Update developer information (admin only) */
  public DeveloperResponse updateDeveloper(Integer id, UpdateDeveloperRequest request) {
    Developer developer = validateAndGetDeveloper(id);

    // Update fields using MapStruct - only non-null fields will be updated
    developerMapper.updateDeveloperFromRequest(request, developer);

    Developer updatedDeveloper = developerRepository.save(developer);
    return developerMapper.toResponse(updatedDeveloper);
  }

  /** Delete developer */
  public void deleteDeveloper(Integer id) {
    validateDeveloperExists(id);
    developerRepository.deleteById(id);
  }

  // ========================================
  // STATUS AND PARTNERSHIP MANAGEMENT
  // ========================================

  /** Update developer status */
  public DeveloperResponse updateDeveloperStatus(Integer id, Developer.DeveloperStatus status) {
    Developer developer = validateAndGetDeveloper(id);

    developer.setStatus(status);
    Developer updatedDeveloper = developerRepository.save(developer);

    return developerMapper.toResponse(updatedDeveloper);
  }

  /** Verify developer */
  public DeveloperResponse verifyDeveloper(Integer id, Integer verifiedBy) {
    Developer developer = validateAndGetDeveloper(id);

    developer.setVerifiedAt(LocalDateTime.now());
    developer.setVerifiedBy(verifiedBy);
    Developer updatedDeveloper = developerRepository.save(developer);

    return developerMapper.toResponse(updatedDeveloper);
  }

  /** Update partnership status */
  public DeveloperResponse updatePartnershipStatus(
      Integer id, Boolean isPartner, Developer.PartnershipLevel partnershipLevel) {
    Developer developer = validateAndGetDeveloper(id);

    developer.setIsPartner(isPartner);
    developer.setPartnershipLevel(partnershipLevel);
    Developer updatedDeveloper = developerRepository.save(developer);

    return developerMapper.toResponse(updatedDeveloper);
  }

  // ========================================
  // QUERY METHODS - BASIC RETRIEVAL
  // ========================================

  /** Get developer by ID */
  @Transactional(readOnly = true)
  public Optional<DeveloperResponse> getDeveloperById(Integer id) {
    return developerRepository.findById(id).map(developerMapper::toResponse);
  }

  /** Get developer by company code */
  @Transactional(readOnly = true)
  public Optional<DeveloperResponse> getDeveloperByCompanyCode(String companyCode) {
    return developerRepository.findByCompanyCode(companyCode).map(developerMapper::toResponse);
  }

  /** Get all developers */
  @Transactional(readOnly = true)
  public List<DeveloperResponse> getAllDevelopers() {
    return developerRepository.findAll().stream()
        .map(developerMapper::toResponse)
        .collect(Collectors.toList());
  }

  /** Get all developers with pagination */
  @Transactional(readOnly = true)
  public PaginatedResponse<DeveloperResponse> getAllDevelopers(
      PaginationRequest paginationRequest) {
    Pageable pageable = paginationRequest.toPageable();
    Page<Developer> developerPage = developerRepository.findAll(pageable);

    Page<DeveloperResponse> responsePage = developerPage.map(developerMapper::toResponse);
    return PaginatedResponse.of(responsePage);
  }

  // ========================================
  // QUERY METHODS - BY STATUS
  // ========================================

  /** Get active developers */
  @Transactional(readOnly = true)
  public List<DeveloperResponse> getActiveDevelopers() {
    return developerRepository.findActiveDevelopers().stream()
        .map(developerMapper::toResponse)
        .collect(Collectors.toList());
  }

  /** Get active developers with pagination */
  @Transactional(readOnly = true)
  public PaginatedResponse<DeveloperResponse> getActiveDevelopers(
      PaginationRequest paginationRequest) {
    Pageable pageable = paginationRequest.toPageable();
    Page<Developer> developerPage = developerRepository.findActiveDevelopers(pageable);

    Page<DeveloperResponse> responsePage = developerPage.map(developerMapper::toResponse);
    return PaginatedResponse.of(responsePage);
  }

  /** Get developers by status */
  @Transactional(readOnly = true)
  public List<DeveloperResponse> getDevelopersByStatus(Developer.DeveloperStatus status) {
    return developerRepository.findByStatus(status).stream()
        .map(developerMapper::toResponse)
        .collect(Collectors.toList());
  }

  /** Get developers by status with pagination */
  @Transactional(readOnly = true)
  public PaginatedResponse<DeveloperResponse> getDevelopersByStatus(
      Developer.DeveloperStatus status, PaginationRequest paginationRequest) {
    Pageable pageable = paginationRequest.toPageable();
    Page<Developer> developerPage = developerRepository.findByStatus(status, pageable);

    Page<DeveloperResponse> responsePage = developerPage.map(developerMapper::toResponse);
    return PaginatedResponse.of(responsePage);
  }

  // ========================================
  // QUERY METHODS - BY PARTNERSHIP
  // ========================================

  /** Get partner developers */
  @Transactional(readOnly = true)
  public List<DeveloperResponse> getPartnerDevelopers() {
    return developerRepository.findByIsPartner(true).stream()
        .map(developerMapper::toResponse)
        .collect(Collectors.toList());
  }

  /** Get partner developers with pagination */
  @Transactional(readOnly = true)
  public PaginatedResponse<DeveloperResponse> getPartnerDevelopers(
      PaginationRequest paginationRequest) {
    Pageable pageable = paginationRequest.toPageable();
    Page<Developer> developerPage = developerRepository.findByIsPartner(true, pageable);

    Page<DeveloperResponse> responsePage = developerPage.map(developerMapper::toResponse);
    return PaginatedResponse.of(responsePage);
  }

  // ========================================
  // QUERY METHODS - BY SPECIALIZATION AND LOCATION
  // ========================================

  /** Get developers by specialization */
  @Transactional(readOnly = true)
  public List<DeveloperResponse> getDevelopersBySpecialization(
      Developer.Specialization specialization) {
    return developerRepository.findBySpecialization(specialization).stream()
        .map(developerMapper::toResponse)
        .collect(Collectors.toList());
  }

  /** Get developers by city */
  @Transactional(readOnly = true)
  public List<DeveloperResponse> getDevelopersByCity(String city) {
    return developerRepository.findByCity(city).stream()
        .map(developerMapper::toResponse)
        .collect(Collectors.toList());
  }

  /** Get developers by province */
  @Transactional(readOnly = true)
  public List<DeveloperResponse> getDevelopersByProvince(String province) {
    return developerRepository.findByProvince(province).stream()
        .map(developerMapper::toResponse)
        .collect(Collectors.toList());
  }

  // ========================================
  // SEARCH METHODS
  // ========================================

  /** Search developers by company name */
  @Transactional(readOnly = true)
  public List<DeveloperResponse> searchDevelopersByCompanyName(String companyName) {
    return developerRepository.searchByCompanyName(companyName).stream()
        .map(developerMapper::toResponse)
        .collect(Collectors.toList());
  }

  /** Search developers by company name with pagination */
  @Transactional(readOnly = true)
  public PaginatedResponse<DeveloperResponse> searchDevelopersByCompanyName(
      String companyName, PaginationRequest paginationRequest) {
    Pageable pageable = paginationRequest.toPageable();
    Page<Developer> developerPage = developerRepository.searchByCompanyName(companyName, pageable);

    Page<DeveloperResponse> responsePage = developerPage.map(developerMapper::toResponse);
    return PaginatedResponse.of(responsePage);
  }

  // ========================================
  // VALIDATION AND EXISTENCE CHECKS
  // ========================================

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

  // ========================================
  // PRIVATE VALIDATION METHODS
  // ========================================

  private void validateCreateDeveloperRequest(CreateDeveloperRequest request) {
    if (developerRepository.existsByCompanyCode(request.getCompanyCode())) {
      throw new IllegalArgumentException(
          "Company code already exists: " + request.getCompanyCode());
    }

    if (developerRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email already exists: " + request.getEmail());
    }
  }

  private Developer validateAndGetDeveloper(Integer id) {
    return developerRepository
        .findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Developer not found with id: " + id));
  }

  private void validateDeveloperExists(Integer id) {
    if (!developerRepository.existsById(id)) {
      throw new IllegalArgumentException("Developer not found with id: " + id);
    }
  }

  // ========================================
  // PRIVATE ENTITY CREATION METHODS
  // ========================================

  private Developer createDeveloperEntity(CreateDeveloperRequest request) {
    Developer developer = developerMapper.toEntity(request);
    developer.setStatus(Developer.DeveloperStatus.ACTIVE);
    return developer;
  }
}
