package com.kelompoksatu.griya.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO untuk verifikasi OTP */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request untuk verifikasi OTP")
public class VerifyOtpRequest {

  @NotBlank(message = "Username atau email tidak boleh kosong")
  @Size(max = 100, message = "Username atau email tidak boleh lebih dari 100 karakter")
  @Schema(description = "Username atau email pengguna", example = "super_admin", required = true)
  private String identifier;

  @NotBlank(message = "Kode OTP tidak boleh kosong")
  @Size(min = 4, max = 8, message = "Kode OTP harus antara 4-8 karakter")
  @Pattern(regexp = "^[0-9A-Z]+$", message = "Kode OTP hanya boleh berisi angka dan huruf kapital")
  @Schema(description = "Kode OTP yang diterima", example = "123456", required = true)
  private String otp;

  @Schema(
      description = "Tujuan OTP (login, registration, reset_password, transaction)",
      example = "login",
      allowableValues = {"login", "registration", "reset_password", "transaction"})
  private String purpose = "login";
}
