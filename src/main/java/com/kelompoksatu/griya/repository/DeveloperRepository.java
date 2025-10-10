package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.Developer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Developer entity
 */
@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Integer> {

    /**
     * Find developer by company code
     */
    Optional<Developer> findByCompanyCode(String companyCode);

    /**
     * Find developer by email
     */
    Optional<Developer> findByEmail(String email);

    /**
     * Find developers by status
     */
    List<Developer> findByStatus(Developer.DeveloperStatus status);

    /**
     * Find developers by partnership status
     */
    List<Developer> findByIsPartner(Boolean isPartner);

    /**
     * Find developers by specialization
     */
    List<Developer> findBySpecialization(Developer.Specialization specialization);

    /**
     * Find developers by partnership level
     */
    List<Developer> findByPartnershipLevel(Developer.PartnershipLevel partnershipLevel);

    /**
     * Find developers by city
     */
    List<Developer> findByCity(String city);

    /**
     * Find developers by province
     */
    List<Developer> findByProvince(String province);

    /**
     * Check if company code exists
     */
    boolean existsByCompanyCode(String companyCode);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Find active developers
     */
    @Query("SELECT d FROM Developer d WHERE d.status = 'ACTIVE'")
    List<Developer> findActiveDevelopers();

    /**
     * Find verified developers
     */
    @Query("SELECT d FROM Developer d WHERE d.verifiedAt IS NOT NULL")
    List<Developer> findVerifiedDevelopers();

    /**
     * Search developers by company name (case-insensitive)
     */
    @Query("SELECT d FROM Developer d WHERE LOWER(d.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))")
    List<Developer> searchByCompanyName(@Param("companyName") String companyName);

    /**
     * Find developers by established year range
     */
    @Query("SELECT d FROM Developer d WHERE d.establishedYear BETWEEN :startYear AND :endYear")
    List<Developer> findByEstablishedYearBetween(@Param("startYear") Integer startYear, @Param("endYear") Integer endYear);

    /**
     * Find partner developers with specific partnership level
     */
    @Query("SELECT d FROM Developer d WHERE d.isPartner = true AND d.partnershipLevel = :level")
    List<Developer> findPartnersByLevel(@Param("level") Developer.PartnershipLevel level);
}