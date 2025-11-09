package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.KprApplication;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatDeveloperRepository extends JpaRepository<KprApplication, Integer> {

  @Query(
      "select k from KprApplication k join k.property p join p.developer d "
          + "where d.id = :developerId and k.createdAt between :start and :end")
  List<KprApplication> findApplicationsByDeveloperAndCreatedBetween(
      @Param("developerId") Integer developerId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query(
      "select count(k) from KprApplication k join k.property p join p.developer d "
          + "where d.id = :developerId and k.status = 'APPROVED' and k.createdAt between :start and :end")
  long countApprovedByDeveloperAndCreatedBetween(
      @Param("developerId") Integer developerId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query(
      "select count(k) from KprApplication k join k.property p join p.developer d "
          + "where d.id = :developerId and k.status = 'REJECTED' and k.createdAt between :start and :end")
  long countRejectedByDeveloperAndCreatedBetween(
      @Param("developerId") Integer developerId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query(
      "select count(k) from KprApplication k join k.property p join p.developer d "
          + "where d.id = :developerId and k.status in ('SUBMITTED','DOCUMENT_VERIFICATION','PROPERTY_APPRAISAL','CREDIT_ANALYSIS','APPROVAL_PENDING') "
          + "and k.createdAt between :start and :end")
  long countPendingByDeveloperAndCreatedBetween(
      @Param("developerId") Integer developerId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query(
      "select count(distinct k.user.id) from KprApplication k join k.property p join p.developer d "
          + "where d.id = :developerId and k.createdAt between :start and :end")
  long countDistinctUsersByDeveloperAndCreatedBetween(
      @Param("developerId") Integer developerId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);
}
