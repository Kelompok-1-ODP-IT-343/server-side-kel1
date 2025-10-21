package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.KprApplication;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository for KPR Application operations */
@Repository
public interface KprApplicationRepository extends JpaRepository<KprApplication, Integer> {

  /** Find application by application number */
  Optional<KprApplication> findByApplicationNumber(String applicationNumber);

  /** Find all applications by user ID */
  List<KprApplication> findByUserIdOrderByCreatedAtDesc(Integer userId);

  /** Find applications by status */
  List<KprApplication> findByStatusOrderByCreatedAtDesc(KprApplication.ApplicationStatus status);

  /** Find applications by user ID and status */
  List<KprApplication> findByUserIdAndStatusOrderByCreatedAtDesc(
      Integer userId, KprApplication.ApplicationStatus status);

  /** Check if user has any pending applications for the same property */
  @Query(
      "SELECT COUNT(k) > 0 FROM KprApplication k WHERE k.userId = :userId AND k.propertyId = :propertyId "
          + "AND k.status IN ('SUBMITTED', 'DOCUMENT_VERIFICATION', 'PROPERTY_APPRAISAL', 'CREDIT_ANALYSIS', 'APPROVAL_PENDING')")
  boolean existsPendingApplicationByUserAndProperty(
      @Param("userId") Integer userId, @Param("propertyId") Integer propertyId);

  /** Get next sequence number for application number generation */
  @Query(
      "SELECT COALESCE(MAX(CAST(SUBSTRING(k.applicationNumber, 10) AS integer)), 0) + 1 "
          + "FROM KprApplication k WHERE k.applicationNumber LIKE :yearPrefix")
  Integer getNextSequenceNumber(@Param("yearPrefix") String yearPrefix);

  /** Find applications requiring approval at specific level */
  @Query(
      "SELECT k FROM KprApplication k WHERE k.currentApprovalLevel = :levelId AND k.status = 'APPROVAL_PENDING'")
  List<KprApplication> findApplicationsRequiringApproval(@Param("levelId") Integer levelId);
}
