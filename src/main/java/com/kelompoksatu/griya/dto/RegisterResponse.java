package com.kelompoksatu.griya.dto;

import lombok.*;

/** DTO for authentication response */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
  private UserResponse user;
}
