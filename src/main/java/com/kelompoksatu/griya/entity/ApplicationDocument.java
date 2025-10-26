package com.kelompoksatu.griya.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

/**
 * Entity for storing KPR application document metadata Based on ERD specification for
 * application_documents table Compliant with Indonesian banking regulations for document management
 */
@Entity
@Table(name = "application_documents")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationDocument {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "application_id", nullable = false)
  private Integer applicationId;

  @Enumerated(EnumType.STRING)
  @JdbcType(PostgreSQLEnumJdbcType.class)
  @Column(name = "document_type", nullable = false)
  private DocumentType documentType;

  @Column(name = "document_name", nullable = false, length = 255)
  private String documentName;

  @Column(name = "original_filename", length = 255)
  private String originalFilename; // Store original filename from user upload

  @Column(name = "file_path", nullable = false, length = 500)
  private String filePath; // S3 URL for the uploaded file

  @Column(name = "s3_key", length = 500)
  private String s3Key; // S3 object key for direct S3 operations

  @Column(name = "file_size", nullable = false)
  private Integer fileSize; // in bytes

  @Column(name = "mime_type", nullable = false, length = 100)
  private String mimeType;

  @Column(name = "is_verified", nullable = false)
  private Boolean isVerified = false;

  @Column(name = "verified_by")
  private Integer verifiedBy; // user_id of admin who verified

  @Column(name = "verified_at")
  private LocalDateTime verifiedAt;

  @Column(name = "verification_notes", columnDefinition = "TEXT")
  private String verificationNotes;

  @Column(name = "uploaded_at", nullable = false)
  private LocalDateTime uploadedAt = LocalDateTime.now();

  // ========================================
  // RELATIONSHIPS
  // ========================================

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "application_id", insertable = false, updatable = false)
  private KprApplication kprApplication;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "verified_by", insertable = false, updatable = false)
  private User verifier;

  // ========================================
  // ENUMS
  // ========================================

  /**
   * Document types as specified in ERD Covers all required documents for KPR application in
   * Indonesia
   */
  public enum DocumentType {
    KTP("ktp", "Kartu Tanda Penduduk"),
    NPWP("npwp", "Nomor Pokok Wajib Pajak"),
    KK("kk", "Kartu Keluarga"),
    SLIP_GAJI("slip_gaji", "Slip Gaji"),
    REKENING_KORAN("rekening_koran", "Rekening Koran"),
    SERTIFIKAT_TANAH("sertifikat_tanah", "Sertifikat Tanah"),
    IMB("imb", "Izin Mendirikan Bangunan"),
    PBB("pbb", "Pajak Bumi dan Bangunan"),
    AKTA_NIKAH("akta_nikah", "Akta Nikah"),
    SURAT_KETERANGAN_KERJA("surat_keterangan_kerja", "Surat Keterangan Kerja"),
    OTHER("other", "Dokumen Lainnya");

    private final String code;
    private final String description;

    DocumentType(String code, String description) {
      this.code = code;
      this.description = description;
    }

    public String getCode() {
      return code;
    }

    public String getDescription() {
      return description;
    }

    /** Get DocumentType from string code */
    public static DocumentType fromCode(String code) {
      for (DocumentType type : values()) {
        if (type.code.equalsIgnoreCase(code)) {
          return type;
        }
      }
      throw new IllegalArgumentException("Invalid document type code: " + code);
    }
  }

  /** Verification status for documents Tracks the verification process of uploaded documents */
  public enum VerificationStatus {
    PENDING("pending", "Menunggu Verifikasi"),
    VERIFIED("verified", "Terverifikasi"),
    REJECTED("rejected", "Ditolak"),
    EXPIRED("expired", "Kedaluwarsa");

    private final String code;
    private final String description;

    VerificationStatus(String code, String description) {
      this.code = code;
      this.description = description;
    }

    public String getCode() {
      return code;
    }

    public String getDescription() {
      return description;
    }

    /** Get VerificationStatus from string code */
    public static VerificationStatus fromCode(String code) {
      for (VerificationStatus status : values()) {
        if (status.code.equalsIgnoreCase(code)) {
          return status;
        }
      }
      throw new IllegalArgumentException("Invalid verification status code: " + code);
    }
  }

  // ========================================
  // BUSINESS METHODS
  // ========================================

  /** Mark document as verified by admin */
  public void markAsVerified(Integer verifiedBy, String notes) {
    this.isVerified = true;
    this.verifiedBy = verifiedBy;
    this.verifiedAt = LocalDateTime.now();
    this.verificationNotes = notes;
  }

  /** Mark document as rejected by admin */
  public void markAsRejected(Integer verifiedBy, String notes) {
    this.isVerified = false;
    this.verifiedBy = verifiedBy;
    this.verifiedAt = LocalDateTime.now();
    this.verificationNotes = notes;
  }

  /** Check if document is image type */
  public boolean isImageDocument() {
    return mimeType != null && mimeType.startsWith("image/");
  }

  /** Check if document is PDF */
  public boolean isPdfDocument() {
    return "application/pdf".equals(mimeType);
  }

  /** Get file size in KB */
  public double getFileSizeInKB() {
    return fileSize != null ? fileSize / 1024.0 : 0;
  }

  /** Get file size in MB */
  public double getFileSizeInMB() {
    return fileSize != null ? fileSize / (1024.0 * 1024.0) : 0;
  }

  /** Get the S3 object key from file path */
  public String getS3ObjectKey() {
    if (s3Key != null && !s3Key.isEmpty()) {
      return s3Key;
    }
    // Extract from URL if s3Key is not set
    if (filePath != null && filePath.contains("/")) {
      return filePath.substring(filePath.lastIndexOf("/") + 1);
    }
    return null;
  }

  /** Get the public S3 URL */
  public String getPublicUrl() {
    return filePath; // filePath now contains the full S3 URL
  }

  /** Check if document requires verification */
  public boolean requiresVerification() {
    return documentType == DocumentType.KTP
        || documentType == DocumentType.NPWP
        || documentType == DocumentType.SLIP_GAJI
        || documentType == DocumentType.SERTIFIKAT_TANAH;
  }
}
