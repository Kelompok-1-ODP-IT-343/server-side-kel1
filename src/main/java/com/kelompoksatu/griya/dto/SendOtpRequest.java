package com.kelompoksatu.griya.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO untuk mengirim OTP */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request untuk mengirim OTP ke nomor telepon")
public class SendOtpRequest {

  @NotBlank(message = "Nomor telepon tidak boleh kosong")
  @Pattern(
      regexp = "^(\\+62|62|0)[0-9]{8,13}$",
      message =
          "Format nomor telepon tidak valid. Gunakan format: 08xxxxxxxxx, 62xxxxxxxxx, atau +62xxxxxxxxx")
  @Schema(description = "Nomor telepon tujuan OTP", example = "081234567890", required = true)
  private String phone;

  @Schema(
      description = "Tujuan OTP (login, registration, reset_password, transaction)",
      example = "login",
      allowableValues = {"login", "registration", "reset_password", "transaction"})
  private String purpose = "login";
}
