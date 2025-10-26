package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.UserStatus;
import java.time.LocalDateTime;
import lombok.Data;

@Data
/** DTO for user response (without sensitive information) */
public class UserResponse {

  private Integer id;
  private String username;
  private String email;
  private String phone;
  private Integer roleId;
  private String roleName;
  private UserStatus status;
  private boolean emailVerified;
  private boolean phoneVerified;
  private LocalDateTime lastLoginAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
