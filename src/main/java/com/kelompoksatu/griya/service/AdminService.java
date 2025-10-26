package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.AdminSimpleResponse;
import com.kelompoksatu.griya.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;

  public List<AdminSimpleResponse> getAllAdminSimple() {
    return userRepository.findAllAdminSimple();
  }
}
