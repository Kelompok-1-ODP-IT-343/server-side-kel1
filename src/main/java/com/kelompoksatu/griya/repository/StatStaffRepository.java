package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.ApprovalWorkflow;
import com.kelompoksatu.griya.entity.KprApplication;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatStaffRepository extends JpaRepository<KprApplication, Integer> {

  /** Applications assigned to a staff within date range (distinct by application). */
  @Query(
      "select k from KprApplication k where k.id in (select aw.applicationId from ApprovalWorkflow aw "
          + "where aw.assignedTo = :staffId) and k.createdAt between :start and :end")
  List<KprApplication> findApplicationsAssignedToStaffBetween(
      @Param("staffId") Integer staffId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  /** Count approved applications assigned to staff in date range by application createdAt. */
  @Query(
      "select count(k) from KprApplication k where k.id in (select aw.applicationId from ApprovalWorkflow aw "
          + "where aw.assignedTo = :staffId) and k.status = 'APPROVED' and k.createdAt between :start and :end")
  long countApprovedAssignedToStaffBetween(
      @Param("staffId") Integer staffId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  /** Count rejected applications assigned to staff in date range by application createdAt. */
  @Query(
      "select count(k) from KprApplication k where k.id in (select aw.applicationId from ApprovalWorkflow aw "
          + "where aw.assignedTo = :staffId) and k.status = 'REJECTED' and k.createdAt between :start and :end")
  long countRejectedAssignedToStaffBetween(
      @Param("staffId") Integer staffId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  /** Count pending applications (not approved/rejected) assigned to staff in date range. */
  @Query(
      "select count(k) from KprApplication k where k.id in (select aw.applicationId from ApprovalWorkflow aw "
          + "where aw.assignedTo = :staffId) and k.status in ('SUBMITTED','DOCUMENT_VERIFICATION','PROPERTY_APPRAISAL','CREDIT_ANALYSIS','APPROVAL_PENDING') "
          + "and k.createdAt between :start and :end")
  long countPendingAssignedToStaffBetween(
      @Param("staffId") Integer staffId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  /** Count distinct users for applications assigned to staff in date range. */
  @Query(
      "select count(distinct k.user.id) from KprApplication k where k.id in (select aw.applicationId from ApprovalWorkflow aw "
          + "where aw.assignedTo = :staffId) and k.createdAt between :start and :end")
  long countDistinctUsersAssignedToStaffBetween(
      @Param("staffId") Integer staffId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  /** Workflows assigned to staff within date range for funnel/SLA (optional use). */
  @Query(
      "select aw from ApprovalWorkflow aw where aw.assignedTo = :staffId and aw.createdAt between :start and :end")
  List<ApprovalWorkflow> findWorkflowsAssignedToStaffBetween(
      @Param("staffId") Integer staffId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);
}
