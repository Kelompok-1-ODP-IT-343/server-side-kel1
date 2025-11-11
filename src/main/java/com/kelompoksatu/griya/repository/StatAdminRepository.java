package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.KprApplication;
import com.kelompoksatu.griya.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatAdminRepository extends JpaRepository<KprApplication, Integer> {

  @Query("select k from KprApplication k where k.createdAt between :start and :end")
  List<KprApplication> findApplicationsBetween(
      @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Query(
      "select count(k) from KprApplication k where k.status = 'APPROVED' and k.createdAt between :start and :end")
  long countApprovedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Query(
      "select count(k) from KprApplication k where k.status = 'REJECTED' and k.createdAt between :start and :end")
  long countRejectedBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Query(
      "select count(k) from KprApplication k where k.status in ('SUBMITTED','DOCUMENT_VERIFICATION','PROPERTY_APPRAISAL','CREDIT_ANALYSIS','APPROVAL_PENDING') and k.createdAt between :start and :end")
  long countPendingBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Query(
      "select count(distinct k.user.id) from KprApplication k where k.createdAt between :start and :end")
  long countDistinctCustomersBetween(
      @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  @Query("select u from User u where u.createdAt between :start and :end")
  List<User> findUsersCreatedBetween(
      @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
