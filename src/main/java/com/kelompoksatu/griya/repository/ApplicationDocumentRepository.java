package com.kelompoksatu.griya.repository;

import com.kelompoksatu.griya.entity.ApplicationDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for ApplicationDocument entity Handles database operations for KPR
 * application documents
 *
 * <p>Compliance: - Indonesian Personal Data Protection Law (UU No. 27/2022) - ISO 27001 Security
 * Standards
 */
@Repository
public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, Integer> {

  /**
   * Find all documents for a specific KPR application
   *
   * @param applicationId KPR application ID
   * @return List of documents for the application
   */
  List<ApplicationDocument> findByApplicationIdOrderByUploadedAtDesc(Integer applicationId);

  /**
   * Find documents by application ID and document type
   *
   * @param applicationId KPR application ID
   * @param documentType Type of document
   * @return List of documents matching criteria
   */
  List<ApplicationDocument> findByApplicationIdAndDocumentType(
      Integer applicationId, ApplicationDocument.DocumentType documentType);

  /**
   * Find a specific document by application ID and document type Useful for documents that should
   * be unique per application (like KTP)
   *
   * @param applicationId KPR application ID
   * @param documentType Type of document
   * @return Optional document
   */
  Optional<ApplicationDocument> findFirstByApplicationIdAndDocumentTypeOrderByUploadedAtDesc(
      Integer applicationId, ApplicationDocument.DocumentType documentType);

  /**
   * Check if a document exists for application and type
   *
   * @param applicationId KPR application ID
   * @param documentType Type of document
   * @return true if document exists
   */
  boolean existsByApplicationIdAndDocumentType(
      Integer applicationId, ApplicationDocument.DocumentType documentType);

  /**
   * Find documents that need verification
   *
   * @return List of unverified documents
   */
  @Query(
      "SELECT ad FROM ApplicationDocument ad WHERE ad.isVerified = false AND ad.verifiedAt IS NULL ORDER BY ad.uploadedAt ASC")
  List<ApplicationDocument> findDocumentsNeedingVerification();

  /**
   * Find verified documents for an application
   *
   * @param applicationId KPR application ID
   * @return List of verified documents
   */
  @Query(
      "SELECT ad FROM ApplicationDocument ad WHERE ad.applicationId = :applicationId AND ad.isVerified = true ORDER BY ad.documentType")
  List<ApplicationDocument> findVerifiedDocumentsByApplicationId(
      @Param("applicationId") Integer applicationId);

  /**
   * Find rejected documents for an application
   *
   * @param applicationId KPR application ID
   * @return List of rejected documents
   */
  @Query(
      "SELECT ad FROM ApplicationDocument ad WHERE ad.applicationId = :applicationId AND ad.isVerified = false AND ad.verifiedAt IS NOT NULL ORDER BY ad.uploadedAt DESC")
  List<ApplicationDocument> findRejectedDocumentsByApplicationId(
      @Param("applicationId") Integer applicationId);

  /**
   * Count documents by verification status for an application
   *
   * @param applicationId KPR application ID
   * @param verificationStatus Verification status
   * @return Count of documents
   */
  @Query(
      "SELECT COUNT(ad) FROM ApplicationDocument ad WHERE ad.applicationId = :applicationId AND ("
          + ":verificationStatus = com.kelompoksatu.griya.entity.ApplicationDocument$VerificationStatus.VERIFIED AND ad.isVerified = true AND ad.verifiedAt IS NOT NULL OR "
          + ":verificationStatus = com.kelompoksatu.griya.entity.ApplicationDocument$VerificationStatus.REJECTED AND ad.isVerified = false AND ad.verifiedAt IS NOT NULL OR "
          + ":verificationStatus = com.kelompoksatu.griya.entity.ApplicationDocument$VerificationStatus.PENDING AND ad.isVerified = false AND ad.verifiedAt IS NULL)")
  long countByApplicationIdAndVerificationStatus(
      @Param("applicationId") Integer applicationId,
      @Param("verificationStatus") ApplicationDocument.VerificationStatus verificationStatus);

  /**
   * Find documents by file path (useful for cleanup operations)
   *
   * @param filePath File path to search
   * @return Optional document
   */
  Optional<ApplicationDocument> findByFilePath(String filePath);

  /**
   * Find documents uploaded within a date range
   *
   * @param startDate Start date
   * @param endDate End date
   * @return List of documents
   */
  @Query(
      "SELECT ad FROM ApplicationDocument ad WHERE ad.uploadedAt BETWEEN :startDate AND :endDate ORDER BY ad.uploadedAt DESC")
  List<ApplicationDocument> findDocumentsUploadedBetween(
      @Param("startDate") java.time.LocalDateTime startDate,
      @Param("endDate") java.time.LocalDateTime endDate);

  /**
   * Find documents larger than specified size (for cleanup/optimization)
   *
   * @param minSize Minimum file size in bytes
   * @return List of large documents
   */
  @Query(
      "SELECT ad FROM ApplicationDocument ad WHERE ad.fileSize > :minSize ORDER BY ad.fileSize DESC")
  List<ApplicationDocument> findDocumentsLargerThan(@Param("minSize") Integer minSize);

  /**
   * Get total storage used by an application's documents
   *
   * @param applicationId KPR application ID
   * @return Total file size in bytes
   */
  @Query(
      "SELECT COALESCE(SUM(ad.fileSize), 0) FROM ApplicationDocument ad WHERE ad.applicationId = :applicationId")
  Long getTotalStorageByApplicationId(@Param("applicationId") Integer applicationId);

  /**
   * Find documents by MIME type (useful for security audits)
   *
   * @param mimeType MIME type to search
   * @return List of documents with specified MIME type
   */
  List<ApplicationDocument> findByMimeType(String mimeType);

  /**
   * Delete documents by application ID (cascade delete)
   *
   * @param applicationId KPR application ID
   */
  void deleteByApplicationId(Integer applicationId);
}
