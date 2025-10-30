package com.kelompoksatu.griya.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service untuk generate dan validasi OTP (One Time Password)
 *
 * <p>Service ini menyediakan: - Generate OTP dengan berbagai format (numeric, alphanumeric) -
 * Validasi OTP dengan Redis storage - Rate limiting untuk prevent brute force - Audit logging untuk
 * compliance
 *
 * <p>Security features: - Menggunakan SecureRandom untuk generate OTP - Rate limiting per
 * identifier - TTL untuk OTP sesuai regulasi - Audit trail untuk semua operasi OTP
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

  private final RedisService redisService;
  private final WhatsAppService whatsAppService;

  @Value("${otp.length:6}")
  private int otpLength;

  @Value("${otp.ttl.minutes:5}")
  private int otpTtlMinutes;

  @Value("${otp.max.attempts:3}")
  private int maxAttempts;

  @Value("${otp.rate.limit.window.minutes:60}")
  private int rateLimitWindowMinutes;

  @Value("${otp.rate.limit.max.requests:5}")
  private int maxOtpRequestsPerHour;

  private static final String OTP_ATTEMPTS_PREFIX = "otp_attempts:";
  private static final String OTP_RATE_LIMIT_PREFIX = "otp_rate_limit:";

  private final SecureRandom secureRandom = new SecureRandom();

  /**
   * Generate OTP numeric
   *
   * @param length Panjang OTP
   * @return OTP string
   */
  public String generateNumericOtp(int length) {
    if (length <= 0 || length > 10) {
      throw new IllegalArgumentException("OTP length harus antara 1-10");
    }

    StringBuilder otp = new StringBuilder();
    for (int i = 0; i < length; i++) {
      otp.append(secureRandom.nextInt(10));
    }

    String otpCode = otp.toString();
    log.debug("OTP numeric berhasil di-generate dengan panjang: {}", length);
    return otpCode;
  }

  /**
   * Generate OTP numeric dengan panjang default
   *
   * @return OTP string
   */
  public String generateNumericOtp() {
    return generateNumericOtp(otpLength);
  }

  /**
   * Generate OTP alphanumeric
   *
   * @param length Panjang OTP
   * @return OTP string
   */
  public String generateAlphanumericOtp(int length) {
    if (length <= 0 || length > 20) {
      throw new IllegalArgumentException("OTP length harus antara 1-20");
    }

    String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder otp = new StringBuilder();

    for (int i = 0; i < length; i++) {
      otp.append(characters.charAt(secureRandom.nextInt(characters.length())));
    }

    String otpCode = otp.toString();
    log.debug("OTP alphanumeric berhasil di-generate dengan panjang: {}", length);
    return otpCode;
  }

  /**
   * Generate dan kirim OTP via WhatsApp
   *
   * @param phone Nomor telepon tujuan
   * @param purpose Tujuan OTP (login, register, reset_password, etc)
   * @return OTP yang di-generate atau null jika gagal
   */
  public String generateAndSendOtp(String phone, String purpose) {
    try {
      // Validasi input
      if (phone == null || phone.trim().isEmpty()) {
        log.error("Nomor telepon tidak boleh kosong");
        return null;
      }

      if (purpose == null || purpose.trim().isEmpty()) {
        purpose = "verification";
      }

      // Check rate limiting
      String rateLimitKey = OTP_RATE_LIMIT_PREFIX + phone;
      long requestCount = redisService.incrementRateLimit(phone, rateLimitWindowMinutes);

      if (requestCount > maxOtpRequestsPerHour) {
        log.warn("Rate limit exceeded untuk nomor: {}. Request count: {}", phone, requestCount);
        throw new RuntimeException(
            "Terlalu banyak permintaan OTP. Silakan coba lagi dalam "
                + rateLimitWindowMinutes
                + " menit.");
      }

      // Generate OTP
      String otp = generateNumericOtp();

      // Store OTP di Redis
      String identifier = phone + ":" + purpose;
      redisService.storeOtp(identifier, otp, otpTtlMinutes);

      // Reset attempts counter
      String attemptsKey = OTP_ATTEMPTS_PREFIX + identifier;
      redisService.delete(attemptsKey);

      // Kirim OTP via WhatsApp
      boolean sent = whatsAppService.sendOtp(phone, otp);

      if (sent) {
        log.info("OTP berhasil di-generate dan dikirim ke {} untuk purpose: {}", phone, purpose);

        // Audit log
        auditLog(phone, purpose, "OTP_GENERATED", "SUCCESS");

        return otp;
      } else {
        log.error("Gagal mengirim OTP ke {}", phone);

        // Hapus OTP dari Redis jika gagal kirim
        redisService.delete("otp:" + identifier);

        // Audit log
        auditLog(phone, purpose, "OTP_SEND_FAILED", "FAILED");

        return null;
      }

    } catch (Exception e) {
      log.error("Error saat generate dan kirim OTP ke {}: {}", phone, e.getMessage(), e);
      auditLog(phone, purpose, "OTP_GENERATION_ERROR", "ERROR: " + e.getMessage());
      throw new RuntimeException("Gagal mengirim OTP: " + e.getMessage(), e);
    }
  }

  /**
   * Validasi OTP
   *
   * @param phone Nomor telepon
   * @param otp Kode OTP yang akan divalidasi
   * @param purpose Tujuan OTP
   * @return true jika OTP valid
   */
  public boolean validateOtp(String phone, String otp, String purpose) {
    try {
      // Validasi input
      if (phone == null || phone.trim().isEmpty()) {
        log.error("Nomor telepon tidak boleh kosong");
        return false;
      }

      if (otp == null || otp.trim().isEmpty()) {
        log.error("OTP tidak boleh kosong");
        return false;
      }

      if (purpose == null || purpose.trim().isEmpty()) {
        purpose = "verification";
      }

      String identifier = phone + ":" + purpose;
      String attemptsKey = OTP_ATTEMPTS_PREFIX + identifier;

      // Check attempts
      long attempts = redisService.getRateLimitCount(attemptsKey.replace(OTP_ATTEMPTS_PREFIX, ""));
      if (attempts >= maxAttempts) {
        log.warn("Maksimal attempts tercapai untuk OTP validation: {}", identifier);
        auditLog(phone, purpose, "OTP_MAX_ATTEMPTS", "BLOCKED");
        throw new RuntimeException("Maksimal percobaan OTP tercapai. Silakan request OTP baru.");
      }

      // Increment attempts
      redisService.incrementRateLimit(attemptsKey.replace(OTP_ATTEMPTS_PREFIX, ""), otpTtlMinutes);

      // Validate OTP
      boolean isValid = redisService.validateOtp(identifier, otp.trim());

      if (isValid) {
        // Clear attempts counter jika berhasil
        redisService.delete(attemptsKey);
        log.info("OTP berhasil divalidasi untuk {}, purpose: {}", phone, purpose);
        auditLog(phone, purpose, "OTP_VALIDATED", "SUCCESS");
        return true;
      } else {
        log.warn(
            "OTP tidak valid untuk {}, purpose: {}, attempts: {}", phone, purpose, attempts + 1);
        auditLog(phone, purpose, "OTP_INVALID", "FAILED - Attempt " + (attempts + 1));
        return false;
      }

    } catch (Exception e) {
      log.error("Error saat validasi OTP untuk {}: {}", phone, e.getMessage(), e);
      auditLog(phone, purpose, "OTP_VALIDATION_ERROR", "ERROR: " + e.getMessage());
      return false;
    }
  }

  /**
   * Generate OTP untuk login
   *
   * @param phone Nomor telepon
   * @return OTP code atau null jika gagal
   */
  public String generateLoginOtp(String phone) {
    return generateAndSendOtp(phone, "login");
  }

  /**
   * Generate OTP untuk registrasi
   *
   * @param phone Nomor telepon
   * @return OTP code atau null jika gagal
   */
  public String generateRegistrationOtp(String phone) {
    return generateAndSendOtp(phone, "registration");
  }

  /**
   * Generate OTP untuk reset password
   *
   * @param phone Nomor telepon
   * @return OTP code atau null jika gagal
   */
  public String generateResetPasswordOtp(String phone) {
    return generateAndSendOtp(phone, "reset_password");
  }

  /**
   * Generate OTP untuk verifikasi transaksi
   *
   * @param phone Nomor telepon
   * @return OTP code atau null jika gagal
   */
  public String generateTransactionOtp(String phone) {
    return generateAndSendOtp(phone, "transaction");
  }

  /**
   * Validasi OTP untuk login
   *
   * @param phone Nomor telepon
   * @param otp Kode OTP
   * @return true jika valid
   */
  public boolean validateLoginOtp(String phone, String otp) {
    return validateOtp(phone, otp, "login");
  }

  /**
   * Validasi OTP untuk registrasi
   *
   * @param phone Nomor telepon
   * @param otp Kode OTP
   * @return true jika valid
   */
  public boolean validateRegistrationOtp(String phone, String otp) {
    return validateOtp(phone, otp, "registration");
  }

  /**
   * Validasi OTP untuk reset password
   *
   * @param phone Nomor telepon
   * @param otp Kode OTP
   * @return true jika valid
   */
  public boolean validateResetPasswordOtp(String phone, String otp) {
    return validateOtp(phone, otp, "reset_password");
  }

  /**
   * Validasi OTP untuk transaksi
   *
   * @param phone Nomor telepon
   * @param otp Kode OTP
   * @return true jika valid
   */
  public boolean validateTransactionOtp(String phone, String otp) {
    return validateOtp(phone, otp, "transaction");
  }

  /**
   * Hapus OTP yang sudah tidak digunakan
   *
   * @param phone Nomor telepon
   * @param purpose Tujuan OTP
   * @return true jika berhasil dihapus
   */
  public boolean clearOtp(String phone, String purpose) {
    try {
      String identifier = phone + ":" + purpose;
      boolean deleted = redisService.delete("otp:" + identifier);

      if (deleted) {
        log.info("OTP berhasil dihapus untuk {}, purpose: {}", phone, purpose);
        auditLog(phone, purpose, "OTP_CLEARED", "SUCCESS");
      }

      return deleted;
    } catch (Exception e) {
      log.error("Error saat menghapus OTP untuk {}: {}", phone, e.getMessage(), e);
      return false;
    }
  }

  /**
   * Check apakah masih ada OTP yang valid
   *
   * @param phone Nomor telepon
   * @param purpose Tujuan OTP
   * @return true jika masih ada OTP yang valid
   */
  public boolean hasValidOtp(String phone, String purpose) {
    try {
      String identifier = phone + ":" + purpose;
      return redisService.exists("otp:" + identifier);
    } catch (Exception e) {
      log.error("Error saat check valid OTP untuk {}: {}", phone, e.getMessage(), e);
      return false;
    }
  }

  /**
   * Get remaining TTL untuk OTP
   *
   * @param phone Nomor telepon
   * @param purpose Tujuan OTP
   * @return TTL dalam detik, -1 jika permanent, -2 jika tidak ada
   */
  public long getOtpTtl(String phone, String purpose) {
    try {
      String identifier = phone + ":" + purpose;
      return redisService.getTtl("otp:" + identifier);
    } catch (Exception e) {
      log.error("Error saat get OTP TTL untuk {}: {}", phone, e.getMessage(), e);
      return -2;
    }
  }

  /**
   * Audit logging untuk compliance
   *
   * @param phone Nomor telepon
   * @param purpose Tujuan OTP
   * @param action Action yang dilakukan
   * @param result Result dari action
   */
  private void auditLog(String phone, String purpose, String action, String result) {
    try {
      String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      String maskedPhone = maskPhoneNumber(phone);

      log.info(
          "OTP_AUDIT - Timestamp: {}, Phone: {}, Purpose: {}, Action: {}, Result: {}",
          timestamp,
          maskedPhone,
          purpose,
          action,
          result);

      // Store audit log di Redis untuk compliance (optional)
      String auditKey = "audit:otp:" + timestamp + ":" + phone.hashCode();
      String auditData =
          String.format("%s|%s|%s|%s|%s", timestamp, maskedPhone, purpose, action, result);
      redisService.set(auditKey, auditData, 30 * 24 * 60 * 60); // 30 hari

    } catch (Exception e) {
      log.error("Error saat audit logging: {}", e.getMessage(), e);
    }
  }

  /**
   * Mask nomor telepon untuk logging
   *
   * @param phone Nomor telepon
   * @return Nomor telepon yang di-mask
   */
  private String maskPhoneNumber(String phone) {
    if (phone == null || phone.length() < 4) {
      return "****";
    }

    return phone.substring(0, 2) + "****" + phone.substring(phone.length() - 2);
  }
}
