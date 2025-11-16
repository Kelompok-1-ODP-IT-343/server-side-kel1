package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.AdminSimpleResponse;
import com.kelompoksatu.griya.dto.ImageAdminRequest;
import com.kelompoksatu.griya.dto.ImageAdminResponse;
import com.kelompoksatu.griya.dto.UserResponse;
import com.kelompoksatu.griya.entity.ImageCategory;
import com.kelompoksatu.griya.entity.ImageType;
import com.kelompoksatu.griya.entity.PropertyImage;
import com.kelompoksatu.griya.repository.DeveloperRepository;
import com.kelompoksatu.griya.repository.PropertyImageRepository;
import com.kelompoksatu.griya.repository.RoleRepository;
import com.kelompoksatu.griya.repository.UserProfileRepository;
import com.kelompoksatu.griya.repository.UserRepository;
import com.kelompoksatu.griya.repository.UserSessionRepository;
import com.kelompoksatu.griya.util.IDCloudHostS3Util;
import io.micrometer.common.lang.Nullable;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.webjars.NotFoundException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;
  private final UserSessionRepository userSessionRepository;
  private final UserProfileRepository userProfileRepository;
  private final RoleRepository roleRepository;
  private final DeveloperRepository developerRepository;
  private final IDCloudHostS3Util idCloudHostS3Util;
  private final PropertyImageRepository propertyImageRepository;

  public List<AdminSimpleResponse> getAllAdminSimple() {
    return userRepository.findAllAdminSimple();
  }

  @Transactional
  public List<ImageAdminResponse> uploadAdminImages(
          List<MultipartFile> images, Integer propertyId, String caption) throws IOException { // Signature baru

      // 1. Validasi File
      if (images == null || images.isEmpty() || images.stream().allMatch(MultipartFile::isEmpty)) {
          throw new IllegalArgumentException("Image file tidak boleh kosong");
      }

      // 2. Tentukan Nilai Default ENUM (berlaku untuk semua gambar)
      // Default: ImageType = EXTERIOR (karena MISC tidak ada), ImageCategory = GALLERY
      ImageType imageType = ImageType.valueOf("EXTERIOR");
      ImageCategory imageCategory = ImageCategory.valueOf("GALLERY");

      // Tentukan Folder S3
      String folder =
              String.valueOf(propertyId == null ? "misc" : propertyId);

      List<ImageAdminResponse> responses = new ArrayList<>();

      // Looping file
      for (MultipartFile image : images) {

          if (image.isEmpty()) {
              log.warn("Skipping empty file.");
              continue;
          }

          log.info("Mulai upload image: {}", image.getOriginalFilename());

          // Upload ke IDCloudHost
          String imageUrl = idCloudHostS3Util.uploadPropertyImage(image, folder);
          log.info("Upload ke IDCloudHost sukses: {}", imageUrl);

          // Simpan metadata ke database
          PropertyImage propertyImage =
                  PropertyImage.builder()
                          .propertyId(propertyId) // Langsung dari parameter
                          .imageType(imageType) // Default EXTERIOR
                          .imageCategory(imageCategory) // Default GALLERY
                          .fileName(UUID.randomUUID().toString())
                          .filePath(imageUrl)
                          .fileSize((int) image.getSize())
                          .mimeType(image.getContentType())
                          .caption(caption) // Langsung dari parameter (bisa null)
                          .build();

          propertyImageRepository.save(propertyImage);
          log.info("Image berhasil disimpan di DB: {}", propertyImage.getFileName());

          // Build response data
          ImageAdminResponse responseData =
                  ImageAdminResponse.builder()
                          .id(propertyImage.getId())
                          .propertyId(propertyImage.getPropertyId())
                          .imageUrl(imageUrl)
                          .fileName(propertyImage.getFileName())
                          .imageType(imageType.name())
                          .imageCategory(imageCategory.name())
                          .caption(propertyImage.getCaption())
                          .fileSize(propertyImage.getFileSize())
                          .mimeType(propertyImage.getMimeType())
                          .build();

          responses.add(responseData);
      }

      // Check if any file was actually processed successfully
      if (responses.isEmpty()) {
          throw new IllegalArgumentException("Semua file image yang diupload kosong.");
      }

      return responses;
  }

  public void hardDeleteUser(Integer targetUserId, Integer adminId, @Nullable String reason) {
    var user =
        userRepository
            .findById(targetUserId)
            .orElseThrow(() -> new NotFoundException("User tidak ditemukan"));

    // hapus child entities yang punya FK ke users
    userSessionRepository.deleteByUserId(targetUserId);
    userProfileRepository.deleteAllByUserId(targetUserId);
    developerRepository.deleteByUserId(targetUserId);

    // baru hapus user
    userRepository.delete(user);

    log.info("Admin {} hard-delete user {}. reason={}", adminId, targetUserId, reason);
  }

  // Get All Approval POV Admin
  public List<UserResponse> getAllApprovalPovAdmin(Integer userID) {
    var user =
        userRepository
            .findById(userID)
            .orElseThrow(() -> new NotFoundException("User tidak ditemukan"));

    if (!user.getRole().getName().equals("ADMIN")) {
      throw new IllegalArgumentException("User tidak memiliki role ADMIN");
    }

    var role =
        roleRepository
            .findByName("APPROVER")
            .orElseThrow(() -> new NotFoundException("Role APPROVER tidak ditemukan"));

    var userResponses =
        userRepository.findAllUserResponseByRoleId(
            role.getId(), org.springframework.data.domain.Pageable.unpaged());

    if (userResponses.isEmpty()) {
      throw new NotFoundException("Tidak ada user dengan role APPROVER");
    }

    return userResponses.getContent();
  }
}
