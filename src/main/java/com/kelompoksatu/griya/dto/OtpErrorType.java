package com.kelompoksatu.griya.dto;

/** Enum untuk tipe error OTP yang spesifik */
public enum OtpErrorType {
  USER_NOT_FOUND("User tidak ditemukan"),
  USER_SUSPENDED("Akun pengguna telah disuspend"),
  USER_INACTIVE("Akun pengguna tidak aktif"),
  INVALID_PHONE_NUMBER("Nomor telepon tidak valid"),
  OTP_SERVICE_ERROR("Layanan OTP mengalami gangguan"),
  RATE_LIMIT_EXCEEDED("Terlalu banyak permintaan OTP. Silakan tunggu beberapa saat"),
  DUPLICATE_USER("Username atau email sudah terdaftar"),
  INVALID_CREDENTIALS("Username/email atau password tidak valid"),
  NETWORK_ERROR("Gagal terhubung ke layanan WhatsApp"),
  PHONE_NOT_REGISTERED("Nomor telepon belum terdaftar di WhatsApp"),
  VALIDATION_ERROR("Data yang dikirim tidak valid"),
  SYSTEM_ERROR("Terjadi kesalahan sistem"),
  ACCOUNT_LOCKED("Akun terkunci sementara karena terlalu banyak percobaan login yang gagal");

  private final String message;

  OtpErrorType(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
