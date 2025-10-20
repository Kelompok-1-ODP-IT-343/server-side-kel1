package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.entity.ApplicationDocument;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for handling file storage operations for KPR application documents Implements static file
 * storage with security and compliance features Compliant with Indonesian data protection
 * regulations (UU PDP)
 */
@Service
@Slf4j
public class FileStorageService {

  // ========================================
  // CONFIGURATION
  // ========================================

  @Value("${app.file.upload-dir:uploads}")
  private String uploadDir;

  @Value("${app.file.max-size:10485760}") // 10MB default
  private long maxFileSize;

  // Allowed file types for security
  private static final List<String> ALLOWED_IMAGE_TYPES =
      Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/gif");

  private static final List<String> ALLOWED_DOCUMENT_TYPES =
      Arrays.asList(
          "application/pdf",
          "application/msword",
          "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

  private static final List<String> ALLOWED_EXTENSIONS =
      Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".pdf", ".doc", ".docx");

  // ========================================
  // PUBLIC METHODS
  // ========================================

  /**
   * Store uploaded file and return file metadata
   *
   * @param file MultipartFile to store
   * @param documentType Type of document being uploaded
   * @param applicationId ID of the KPR application
   * @return ApplicationDocument metadata
   * @throws IOException if file storage fails
   * @throws IllegalArgumentException if file validation fails
   */
  public ApplicationDocument storeFile(
      MultipartFile file, ApplicationDocument.DocumentType documentType, Integer applicationId)
      throws IOException {

    log.info("Storing file: {} for application: {}", file.getOriginalFilename(), applicationId);

    // Validate file
    validateFile(file);

    // Create upload directory if it doesn't exist
    Path uploadPath = createUploadDirectory(applicationId);

    // Generate unique filename
    String fileName = generateUniqueFileName(file.getOriginalFilename());
    Path filePath = uploadPath.resolve(fileName);

    // Store file
    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

    // Create document metadata
    ApplicationDocument document = new ApplicationDocument();
    document.setApplicationId(applicationId);
    document.setDocumentType(documentType);
    document.setDocumentName(file.getOriginalFilename());
    document.setFilePath(filePath.toString());
    document.setFileSize((int) file.getSize());
    document.setMimeType(file.getContentType());
    document.setUploadedAt(LocalDateTime.now());

    log.info("File stored successfully: {} -> {}", file.getOriginalFilename(), filePath);
    return document;
  }

  /**
   * Delete file from storage
   *
   * @param filePath Path to file to delete
   * @return true if deleted successfully, false otherwise
   */
  public boolean deleteFile(String filePath) {
    try {
      Path path = Paths.get(filePath);
      boolean deleted = Files.deleteIfExists(path);

      if (deleted) {
        log.info("File deleted successfully: {}", filePath);
      } else {
        log.warn("File not found for deletion: {}", filePath);
      }

      return deleted;
    } catch (IOException e) {
      log.error("Error deleting file: {}", filePath, e);
      return false;
    }
  }

  /**
   * Check if file exists
   *
   * @param filePath Path to check
   * @return true if file exists
   */
  public boolean fileExists(String filePath) {
    return Files.exists(Paths.get(filePath));
  }

  /**
   * Get file size in bytes
   *
   * @param filePath Path to file
   * @return file size in bytes, -1 if file doesn't exist
   */
  public long getFileSize(String filePath) {
    try {
      return Files.size(Paths.get(filePath));
    } catch (IOException e) {
      log.error("Error getting file size: {}", filePath, e);
      return -1;
    }
  }

  // ========================================
  // PRIVATE METHODS
  // ========================================

  /** Validate uploaded file for security and compliance */
  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File tidak boleh kosong");
    }

    // Check file size
    if (file.getSize() > maxFileSize) {
      throw new IllegalArgumentException(
          String.format("Ukuran file terlalu besar. Maksimal: %d MB", maxFileSize / (1024 * 1024)));
    }

    // Check file type
    String contentType = file.getContentType();
    if (contentType == null
        || (!ALLOWED_IMAGE_TYPES.contains(contentType)
            && !ALLOWED_DOCUMENT_TYPES.contains(contentType))) {
      throw new IllegalArgumentException(
          "Tipe file tidak diizinkan. Hanya mendukung: JPG, PNG, PDF, DOC, DOCX");
    }

    // Check file extension
    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null || !hasAllowedExtension(originalFilename)) {
      throw new IllegalArgumentException(
          "Ekstensi file tidak diizinkan. Hanya mendukung: .jpg, .png, .pdf, .doc, .docx");
    }

    // Additional security check - prevent path traversal
    if (originalFilename.contains("..")
        || originalFilename.contains("/")
        || originalFilename.contains("\\")) {
      throw new IllegalArgumentException("Nama file tidak valid");
    }
  }

  /** Check if filename has allowed extension */
  private boolean hasAllowedExtension(String filename) {
    String lowerFilename = filename.toLowerCase();
    return ALLOWED_EXTENSIONS.stream().anyMatch(lowerFilename::endsWith);
  }

  /** Create upload directory structure */
  private Path createUploadDirectory(Integer applicationId) throws IOException {
    // Create directory structure: uploads/kpr-applications/YYYY/MM/application-{id}/
    LocalDateTime now = LocalDateTime.now();
    String year = now.format(DateTimeFormatter.ofPattern("yyyy"));
    String month = now.format(DateTimeFormatter.ofPattern("MM"));

    Path uploadPath =
        Paths.get(uploadDir, "kpr-applications", year, month, "application-" + applicationId);

    Files.createDirectories(uploadPath);
    log.debug("Created upload directory: {}", uploadPath);

    return uploadPath;
  }

  /** Generate unique filename to prevent conflicts */
  private String generateUniqueFileName(String originalFilename) {
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    String uuid = UUID.randomUUID().toString().substring(0, 8);

    // Extract extension
    String extension = "";
    int lastDotIndex = originalFilename.lastIndexOf('.');
    if (lastDotIndex > 0) {
      extension = originalFilename.substring(lastDotIndex);
    }

    return String.format("%s-%s%s", timestamp, uuid, extension);
  }

  /** Get relative path for database storage */
  public String getRelativePath(String absolutePath) {
    Path uploadPath = Paths.get(uploadDir);
    Path filePath = Paths.get(absolutePath);

    try {
      return uploadPath.relativize(filePath).toString();
    } catch (Exception e) {
      log.warn("Could not create relative path for: {}", absolutePath);
      return absolutePath;
    }
  }

  /** Get absolute path from relative path */
  public String getAbsolutePath(String relativePath) {
    return Paths.get(uploadDir, relativePath).toString();
  }
}
