package com.kelompoksatu.griya.dto;

import lombok.Data;

@Data
public class TokenRefreshRequest {
  private String refreshToken;
  private String ipAddress;
  private String userAgent;
}
