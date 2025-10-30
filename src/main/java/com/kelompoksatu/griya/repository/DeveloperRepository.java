package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.Developer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository interface for Developer entity */
@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Integer> {

  /** Find developer by company code */
  Optional<Developer> findByCompanyCode(String companyCode);

  /** Find developer by email */
  Optional<Developer> findByEmail(String email);

  /** Find developers by status */
  List<Developer> findByStatus(Developer.DeveloperStatus status);

  /** Find developers by status with pagination */
  Page<Developer> findByStatus(Developer.DeveloperStatus status, Pageable pageable);

  /** Find developers by partnership status */
  List<Developer> findByIsPartner(Boolean isPartner);

  /** Find developers by partnership status with pagination */
  Page<Developer> findByIsPartner(Boolean isPartner, Pageable pageable);

  /** Find developers by specialization */
  List<Developer> findBySpecialization(Developer.Specialization specialization);

  /** Find developers by specialization with pagination */
  Page<Developer> findBySpecialization(Developer.Specialization specialization, Pageable pageable);

  /** Find developers by partnership level */
  List<Developer> findByPartnershipLevel(Developer.PartnershipLevel partnershipLevel);

  /** Find developers by partnership level with pagination */
  Page<Developer> findByPartnershipLevel(
      Developer.PartnershipLevel partnershipLevel, Pageable pageable);

  /** Find developers by city */
  List<Developer> findByCity(String city);

  /** Find developers by city with pagination */
  Page<Developer> findByCity(String city, Pageable pageable);

  /** Find developers by province */
  List<Developer> findByProvince(String province);

  /** Find developers by province with pagination */
  Page<Developer> findByProvince(String province, Pageable pageable);

  /** Check if company code exists */
  boolean existsByCompanyCode(String companyCode);

  /** Check if email exists */
  boolean existsByEmail(String email);

  /** Find active developers */
  @Query("SELECT d FROM Developer d WHERE d.status = 'ACTIVE'")
  List<Developer> findActiveDevelopers();

  /** Find active developers with pagination */
  @Query("SELECT d FROM Developer d WHERE d.status = 'ACTIVE'")
  Page<Developer> findActiveDevelopers(Pageable pageable);

  /** Find verified developers */
  @Query("SELECT d FROM Developer d WHERE d.verifiedAt IS NOT NULL")
  List<Developer> findVerifiedDevelopers();

  /** Find verified developers with pagination */
  @Query("SELECT d FROM Developer d WHERE d.verifiedAt IS NOT NULL")
  Page<Developer> findVerifiedDevelopers(Pageable pageable);

  /** Search developers by company name (case-insensitive) */
  @Query(
      "SELECT d FROM Developer d WHERE LOWER(d.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))")
  List<Developer> searchByCompanyName(@Param("companyName") String companyName);

  /** Search developers by company name with pagination (case-insensitive) */
  @Query(
      "SELECT d FROM Developer d WHERE LOWER(d.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))")
  Page<Developer> searchByCompanyName(@Param("companyName") String companyName, Pageable pageable);

  /** Find developers by established year range */
  @Query("SELECT d FROM Developer d WHERE d.establishedYear BETWEEN :startYear AND :endYear")
  List<Developer> findByEstablishedYearBetween(
      @Param("startYear") Integer startYear, @Param("endYear") Integer endYear);

  /** Find developers by established year range with pagination */
  @Query("SELECT d FROM Developer d WHERE d.establishedYear BETWEEN :startYear AND :endYear")
  Page<Developer> findByEstablishedYearBetween(
      @Param("startYear") Integer startYear, @Param("endYear") Integer endYear, Pageable pageable);

  /** Find partner developers with specific partnership level */
  @Query("SELECT d FROM Developer d WHERE d.isPartner = true AND d.partnershipLevel = :level")
  List<Developer> findPartnersByLevel(@Param("level") Developer.PartnershipLevel level);

  /** Find partner developers with specific partnership level and pagination */
  @Query("SELECT d FROM Developer d WHERE d.isPartner = true AND d.partnershipLevel = :level")
  Page<Developer> findPartnersByLevel(
      @Param("level") Developer.PartnershipLevel level, Pageable pageable);

  /** Validate developer existence */
  @Query("SELECT d FROM Developer d WHERE d.id = :developerId AND d.status = 'ACTIVE'")
  Optional<Developer> validateDeveloper(@Param("developerId") Integer developerId);

  @Modifying
  @Query("delete from Developer d where d.user.id = :userId")
  void deleteByUserId(@Param("userId") Integer userId);
}
