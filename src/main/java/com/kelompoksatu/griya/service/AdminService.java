package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.AdminSimpleResponse;
import com.kelompoksatu.griya.dto.UserResponse;
import com.kelompoksatu.griya.repository.DeveloperRepository;
import com.kelompoksatu.griya.repository.RoleRepository;
import com.kelompoksatu.griya.repository.UserProfileRepository;
import com.kelompoksatu.griya.repository.UserRepository;
import com.kelompoksatu.griya.repository.UserSessionRepository;
import io.micrometer.common.lang.Nullable;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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

  public List<AdminSimpleResponse> getAllAdminSimple() {
    return userRepository.findAllAdminSimple();
  }

  @Transactional
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
