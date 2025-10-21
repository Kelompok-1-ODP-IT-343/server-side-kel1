package com.kelompoksatu.griya.dto;

import lombok.*;

/** DTO for authentication response */
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class AuthResponse {

  @NonNull private String token;

  @NonNull private String refreshToken;

  private String type = "Bearer";
}
