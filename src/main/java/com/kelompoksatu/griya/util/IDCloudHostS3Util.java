package com.kelompoksatu.griya.util;

import jakarta.annotation.PostConstruct; // Import for @PostConstruct
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set; // Import Set
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Utility class untuk upload file ke IDCloudHost Object Storage menggunakan S3-compatible API
 *
 * <p>IDCloudHost Object Storage S3-compatible endpoint: https://is3.cloudhost.id
 *
 * <p>Features:
 * - Creates a single S3Client bean instance (thread-safe) on startup via @PostConstruct.
 * - Adds MIME type validation for enhanced security.
 * - Uses Set for more efficient extension validation.
 * - Uploads files with public-read permissions for direct URL access.
 * - Generates publicly accessible URLs for uploaded files.
 */
@Slf4j
@Component
public class IDCloudHostS3Util {

  // IDCloudHost Object Storage Configuration
  @Value("${IDCLOUDHOST_S3_ACCESS_KEY:}")
  private String accessKey;

  @Value("${IDCLOUDHOST_S3_SECRET_KEY:}")
  private String secretKey;

  @Value("${IDCLOUDHOST_S3_BUCKET_NAME:}")
  private String bucketName;

  @Value("${IDCLOUDHOST_S3_REGION:id-jkt-1}")
  private String region;

  @Value("${IDCLOUDHOST_S3_ENDPOINT:https://is3.cloudhost.id}")
  private String endpoint;

  // --- REFACTORED: S3Client is now an instance variable ---
  /**
   * S3Client is thread-safe and should be created once and reused.
   * This client is initialized on bean startup by the initClient() method.
   */
  private S3Client s3Client;

  // Allowed file extensions
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
          ".pdf", ".jpg", ".jpeg", ".png", ".doc", ".docx", ".xls", ".xlsx", ".zip"
  );

  // Allowed MIME types for corresponding extensions
  private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
          "application/pdf",
          "image/jpeg",
          "image/png",
          "application/msword",
          "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
          "application/vnd.ms-excel",
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
          "application/zip"
  );

  // Maximum file size (1MB)
  private static final long MAX_FILE_SIZE = 1 * 1024 * 1024;

  /**
   * REFACTORED: Initializes the S3Client bean once after all @Value properties are injected.
   * This avoids creating a new client for every upload, which is critical for performance.
   */
  @PostConstruct
  public void initClient() {
    if (isConfigured()) {
      this.s3Client = createIDCloudHostS3Client();
      testConnection(); // Optional: Test connection on startup
    }
  }

  /**
   * Upload file ke IDCloudHost Object Storage dengan public-read permission
   *
   * @param file MultipartFile yang akan diupload
   * @param folder Folder tujuan di bucket (contoh: "documents", "images", "kpr-docs")
   * @return URL file yang berhasil diupload (publicly accessible)
   * @throws IOException jika terjadi error saat upload
   * @throws IllegalArgumentException jika file tidak valid
   * @throws IllegalStateException jika client not configured
   */
  public String uploadFile(MultipartFile file, String folder) throws IOException {
    if (this.s3Client == null) {
      throw new IllegalStateException("Object Storage (S3) is not configured.");
    }

    // Validasi file (now includes MIME type check)
    validateFile(file);

    // Generate unique filename
    String fileName = generateSecureFileName(file.getOriginalFilename(), folder);

    try {
      // S3 client is now reused
      PutObjectRequest putObjectRequest =
              PutObjectRequest.builder()
                      .bucket(bucketName)
                      .key(fileName)
                      .contentType(file.getContentType())
                      .contentLength(file.getSize())
                      .acl(ObjectCannedACL.PUBLIC_READ) // Set public-read permission
                      .metadata(
                              java.util.Map.of(
                                      "uploaded-at", LocalDateTime.now().toString(),
                                      "original-name", file.getOriginalFilename(),
                                      "file-size", String.valueOf(file.getSize())))
                      .build();

      // Upload file
      PutObjectResponse response =
              s3Client.putObject(
                      putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

      String fileUrl = generateIDCloudHostFileUrl(fileName);


      return fileUrl;

    } catch (S3Exception e) {

      throw new IOException("Failed to upload file to IDCloudHost: " + e.getMessage(), e);
    } catch (Exception e) {
      throw new IOException("Unexpected error during file upload: " + e.getMessage(), e);
    }
  }

  /** Upload file tanpa folder (ke "uploads" bucket) */
  public String uploadFile(MultipartFile file) throws IOException {
    return uploadFile(file, "uploads");
  }

  /** Upload file untuk dokumen KPR dengan folder khusus */
  public String uploadKprDocument(MultipartFile file, String documentType) throws IOException {
    String folder = "kpr-documents/" + documentType;
    return uploadFile(file, folder);
  }

  /** Upload file untuk property images */
  public String uploadPropertyImage(MultipartFile file, String propertyId) throws IOException {
    String folder = "property-images/" + propertyId;
    return uploadFile(file, folder);
  }

  /** Validasi file (compliance dengan regulasi keamanan) */
  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File tidak boleh kosong");
    }

    if (file.getSize() > MAX_FILE_SIZE) {
      throw new IllegalArgumentException("Ukuran file terlalu besar. Maksimal 20MB");
    }

    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || originalFilename.trim().isEmpty()) {
      throw new IllegalArgumentException("Nama file tidak valid");
    }

    // Validasi nama file untuk mencegah path traversal
    if (originalFilename.contains("..")
            || originalFilename.contains("/")
            || originalFilename.contains("\\")) {
      throw new IllegalArgumentException("Nama file mengandung karakter yang tidak diizinkan");
    }

    // REFACTORED: Validate extension using Set
    String extension = getFileExtension(originalFilename).toLowerCase();
    if (!ALLOWED_EXTENSIONS.contains(extension)) {
      throw new IllegalArgumentException(
              "Tipe file tidak diizinkan (ekstensi). Hanya mendukung: " + String.join(", ", ALLOWED_EXTENSIONS));
    }

    // REFACTORED: Validate MIME type using Set
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
      throw new IllegalArgumentException(
              "Tipe file tidak diizinkan (MIME type).");
    }
  }

  /** Generate nama file yang aman dan unik */
  private String generateSecureFileName(String originalFilename, String folder) {
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String uuid = UUID.randomUUID().toString().replace("-", ""); // Full UUID tanpa dash untuk uniqueness maksimal

    String cleanName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
    String extension = getFileExtension(cleanName);
    String nameWithoutExt = cleanName.substring(0, cleanName.lastIndexOf('.'));

    if (nameWithoutExt.length() > 20) {
      nameWithoutExt = nameWithoutExt.substring(0, 20);
    }

    String fileName = timestamp + "_" + uuid + "_" + nameWithoutExt + extension;

    if (folder != null && !folder.trim().isEmpty()) {
      folder = folder.replaceAll("[^a-zA-Z0-9/_-]", "");
      if (!folder.endsWith("/")) {
        folder += "/";
      }
      fileName = folder + fileName;
    }

    return fileName;
  }

  /** Extract file extension dari nama file */
  private String getFileExtension(String filename) {
    if (filename == null || filename.lastIndexOf('.') == -1) {
      return "";
    }
    return filename.substring(filename.lastIndexOf('.'));
  }

  /** Create S3 client khusus untuk IDCloudHost Object Storage */
  private S3Client createIDCloudHostS3Client() {
    return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(
                    StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .endpointOverride(URI.create(endpoint))
            .serviceConfiguration(
                    S3Configuration.builder()
                            .pathStyleAccessEnabled(true) // IDCloudHost menggunakan path-style access
                            .build())
            .build();
  }

  /** Generate URL untuk mengakses file di IDCloudHost */
  private String generateIDCloudHostFileUrl(String fileName) {
    return String.format("%s/%s/%s", endpoint, bucketName, fileName);
  }

  /** Check apakah IDCloudHost S3 client sudah dikonfigurasi dengan benar */
  public boolean isConfigured() {
    return accessKey != null
            && !accessKey.trim().isEmpty()
            && secretKey != null
            && !secretKey.trim().isEmpty()
            && bucketName != null
            && !bucketName.trim().isEmpty();
  }

  /** Get bucket name yang sedang digunakan */
  public String getBucketName() {
    return bucketName;
  }

  /** Get endpoint IDCloudHost yang digunakan */
  public String getEndpoint() {
    return endpoint;
  }

  /** Test koneksi ke IDCloudHost Object Storage */
  public boolean testConnection() {
    if (this.s3Client == null) {
      return false;
    }
    try {
      s3Client.headBucket(builder -> builder.bucket(bucketName));
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
