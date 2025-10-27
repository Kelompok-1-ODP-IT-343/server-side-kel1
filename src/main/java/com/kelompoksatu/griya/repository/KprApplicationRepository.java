package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.dto.KprHistoryListResponse;
import com.kelompoksatu.griya.dto.KprInProgress;
import com.kelompoksatu.griya.entity.ApprovalWorkflow;
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

  List<KprApplication> findKprApplicationsByUserId(Integer userId);

  /** Find KPR applications by developer ID history */
  @Query(
      "SELECT k FROM KprApplication k WHERE k.propertyId IN "
          + "(SELECT p.id FROM Property p WHERE p.developerId = :developerID)")
  List<KprApplication> findKprApplicationsByDeveloperIDHistory(Integer developerID);

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
  Long getNextSequenceNumber(@Param("yearPrefix") String yearPrefix);

  /** Find applications requiring approval at specific level */
  @Query(
      "SELECT k FROM KprApplication k WHERE k.currentApprovalLevel = :levelId AND k.status = 'APPROVAL_PENDING'")
  List<KprApplication> findApplicationsRequiringApproval(@Param("levelId") Integer levelId);

  /** Find KPR applications history handled by developer (completed workflows) */
  @Query(
      "SELECT DISTINCT k FROM KprApplication k "
          + "JOIN ApprovalWorkflow aw ON k.id = aw.applicationId "
          + "WHERE aw.assignedTo = :developerId "
          + "AND aw.status IN ('APPROVED', 'REJECTED', 'SKIPPED') "
          + "ORDER BY aw.completedAt DESC")
  List<KprApplication> findKprApplicationsHistoryByDeveloper(
      @Param("developerId") Integer developerId);

  /** Find KPR applications currently on-progress for developer */
  @Query(
      "SELECT DISTINCT k FROM KprApplication k "
          + "JOIN ApprovalWorkflow aw ON k.id = aw.applicationId "
          + "WHERE aw.assignedTo = :developerId "
          + "AND aw.status IN ('PENDING', 'IN_PROGRESS') "
          + "ORDER BY aw.createdAt ASC")
  List<KprApplication> findKprApplicationsOnProgressByDeveloper(
      @Param("developerId") Integer developerId);

  /** Find approval workflow details by developer ID */
  @Query(
      "SELECT aw FROM ApprovalWorkflow aw "
          + "WHERE aw.assignedTo = :developerId "
          + "ORDER BY aw.createdAt DESC")
  List<ApprovalWorkflow> findApprovalWorkflowByDeveloper(@Param("developerId") Integer developerId);

  /** Find approval workflow details by developer ID and status */
  @Query(
      "SELECT aw FROM ApprovalWorkflow aw "
          + "WHERE aw.assignedTo = :developerId "
          + "AND aw.status = :status "
          + "ORDER BY aw.createdAt DESC")
  List<ApprovalWorkflow> findApprovalWorkflowByDeveloperAndStatus(
      @Param("developerId") Integer developerId,
      @Param("status") ApprovalWorkflow.WorkflowStatus status);

  // Show list semua KPR untuk superadmin
  @Query("SELECT k FROM KprApplication k ORDER BY k.createdAt DESC")
  List<KprApplication> findAllKprApplications();

  // Show list KprApplication history by userID from ApprovalWorkflow
  @Query(
      "SELECT new com.kelompoksatu.griya.dto.KprHistoryListResponse("
          + "k.property.title, "
          + "CAST(k.status AS string), "
          + "CONCAT(k.property.district, ', ', k.property.city, ', ', k.property.province), "
          + "k.applicationNumber, "
          + "k.loanAmount, "
          + "CAST(k.createdAt AS string), "
          + "'') "
          + "FROM KprApplication k WHERE k.id IN "
          + "(SELECT aw.applicationId FROM ApprovalWorkflow aw WHERE aw.assignedTo = :userId) "
          + "AND k.status IN ('APPROVED', 'REJECTED', 'SKIPPED') "
          + "ORDER BY k.createdAt DESC")
  List<KprHistoryListResponse> findKprApplicationsHistoryByUserID(@Param("userId") Integer userId);

  // Show list KprApplication on progress by userID from ApprovalWorkflow
  @Query(
      "SELECT new com.kelompoksatu.griya.dto.KprInProgress("
          + "k.id, "
          + "k.applicationNumber, "
          + "k.property.title, "
          + "k.property.address, "
          + "k.loanAmount, "
          + "CAST(k.createdAt AS string), "
          + "k.kprRate.rateName) "
          + "FROM KprApplication k WHERE k.id IN "
          + "(SELECT aw.applicationId FROM ApprovalWorkflow aw WHERE aw.assignedTo = :userId) "
          + "AND k.status IN ('PENDING', 'IN_PROGRESS') "
          + "ORDER BY k.createdAt ASC")
  List<KprInProgress> findKprApplicationsOnProgressByUserID(@Param("userId") Integer userId);
}
