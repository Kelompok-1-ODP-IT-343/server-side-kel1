package com.kelompoksatu.griya.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO untuk operasi OTP */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response untuk operasi OTP")
public class OtpResponse {

  @Schema(description = "Status operasi OTP", example = "true")
  private boolean success;

  @Schema(description = "Pesan response", example = "OTP berhasil dikirim")
  private String message;

  @Schema(description = "Nomor telepon tujuan (masked)", example = "08****7890")
  private String maskedPhone;

  @Schema(description = "TTL OTP dalam detik", example = "300")
  private Long ttlSeconds;

  @Schema(description = "Tujuan OTP", example = "login")
  private String purpose;

  @Schema(description = "Detail error jika operasi gagal")
  private OtpErrorResponse errorDetail;

  /** Constructor untuk response sukses */
  public static OtpResponse success(
      String message, String maskedPhone, Long ttlSeconds, String purpose) {
    return new OtpResponse(true, message, maskedPhone, ttlSeconds, purpose, null);
  }

  /** Constructor untuk response error sederhana */
  public static OtpResponse error(String message) {
    return new OtpResponse(false, message, null, null, null, null);
  }

  /** Constructor untuk response error dengan detail */
  public static OtpResponse error(String message, OtpErrorResponse errorDetail) {
    return new OtpResponse(false, message, null, null, null, errorDetail);
  }

  /** Constructor untuk response error dengan OtpErrorType */
  public static OtpResponse error(OtpErrorType errorType, String detail) {
    OtpErrorResponse errorDetail = OtpErrorResponse.create(errorType, detail);
    return new OtpResponse(false, errorType.getMessage(), null, null, null, errorDetail);
  }

  /** Constructor untuk response error dengan OtpErrorType dan custom message */
  public static OtpResponse error(OtpErrorType errorType, String customMessage, String detail) {
    OtpErrorResponse errorDetail = OtpErrorResponse.create(errorType, detail);
    return new OtpResponse(false, customMessage, null, null, null, errorDetail);
  }
}
