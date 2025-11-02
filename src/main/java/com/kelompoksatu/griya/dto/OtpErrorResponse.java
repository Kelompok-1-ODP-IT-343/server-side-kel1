package com.kelompoksatu.griya.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO untuk error OTP yang lebih detail */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response detail untuk error OTP")
public class OtpErrorResponse {

  @Schema(description = "Tipe error yang spesifik", example = "USER_NOT_FOUND")
  private OtpErrorType errorType;

  @Schema(description = "Pesan error yang user-friendly", example = "User tidak ditemukan")
  private String message;

  @Schema(
      description = "Detail error untuk developer/logging",
      example = "User with identifier 'user123' not found in database")
  private String detail;

  @Schema(description = "Kode error numerik untuk reference", example = "4001")
  private Integer errorCode;

  @Schema(description = "Timestamp error terjadi", example = "2025-11-01T10:30:00")
  private String timestamp;

  @Schema(
      description = "Saran aksi yang bisa dilakukan user",
      example = "Silakan periksa kembali username atau email Anda")
  private String suggestion;

  /** Constructor untuk membuat OtpErrorResponse dengan mudah */
  public static OtpErrorResponse create(OtpErrorType errorType, String detail, String suggestion) {
    return new OtpErrorResponse(
        errorType,
        errorType.getMessage(),
        detail,
        errorType.ordinal() + 4001, // Start from error code 4001
        java.time.LocalDateTime.now().toString(),
        suggestion);
  }

  /** Constructor sederhana tanpa suggestion */
  public static OtpErrorResponse create(OtpErrorType errorType, String detail) {
    return create(errorType, detail, getDefaultSuggestion(errorType));
  }

  /** Mendapatkan saran default berdasarkan tipe error */
  private static String getDefaultSuggestion(OtpErrorType errorType) {
    return switch (errorType) {
      case USER_NOT_FOUND ->
          "Silakan periksa kembali username atau email Anda, atau daftar akun baru";
      case USER_SUSPENDED -> "Hubungi administrator untuk mengaktifkan kembali akun Anda";
      case USER_INACTIVE -> "Silakan aktivasi akun Anda terlebih dahulu";
      case INVALID_PHONE_NUMBER ->
          "Gunakan nomor telepon yang valid dengan format Indonesia (08xx atau +62)";
      case OTP_SERVICE_ERROR -> "Coba lagi dalam beberapa menit atau hubungi support";
      case RATE_LIMIT_EXCEEDED -> "Tunggu 5-10 menit sebelum mencoba lagi";
      case DUPLICATE_USER -> "Gunakan username atau email yang berbeda";
      case INVALID_CREDENTIALS -> "Periksa kembali username/email dan password Anda";
      case NETWORK_ERROR -> "Periksa koneksi internet dan coba lagi";
      case PHONE_NOT_REGISTERED -> "Pastikan nomor WhatsApp Anda aktif dan terdaftar";
      case VALIDATION_ERROR -> "Periksa kembali data yang Anda masukkan";
      case SYSTEM_ERROR -> "Coba lagi dalam beberapa saat atau hubungi support";
      case ACCOUNT_LOCKED -> "Tunggu 30 menit atau hubungi support untuk membuka kunci akun";
    };
  }
}
