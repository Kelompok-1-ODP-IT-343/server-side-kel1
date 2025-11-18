package com.kelompoksatu.griya.service;

import com.kelompoksatu.griya.dto.OtpErrorType;
import com.kelompoksatu.griya.dto.OtpResponse;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
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
  private static final Set<String> PHONE_ALLOWED_TO_BYPASS_OTP =
      Set.of(
          // "6285678900990",
          // "6285727771009",
          // "6281388899900",
          // "6287884396829",
          // "6285704384348",
          // "628211334456",
          // "6281234567890",
          // "6281298765432",
          // "6285157862331",
          // "6281221841102",
          // "6281299900011",
          // "6286789098990",
          // "6285898989009",
          // "6285713231889"
          );

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

      if (PHONE_ALLOWED_TO_BYPASS_OTP.contains(phone)) {
        log.info("Nomor {} diizinkan untuk melewati OTP", phone);
        return "000000";
      }

      String identifier = phone + ":" + purpose;
      // Check rate limiting
      String rateLimitKey = "rate_limit:" + identifier;
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
      log.info(
          "DEBUG: Storing OTP - Phone: {}, Purpose: {}, Identifier: {}, OTP: {}",
          phone,
          purpose,
          identifier,
          otp);
      redisService.storeOtp(identifier, otp, otpTtlMinutes);

      // Verify OTP was stored
      boolean otpExists = redisService.exists("otp:" + identifier);
      log.info(
          "DEBUG: OTP storage verification - Key exists: {}, Key: otp:{}", otpExists, identifier);

      // Reset attempts counter - Fixed key handling

      redisService.delete(rateLimitKey);

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

      log.info(
          "DEBUG: Validating OTP - Phone: {}, Purpose: {}, Identifier: {}, Input OTP: {}",
          phone,
          purpose,
          identifier,
          otp);

      // Check if OTP exists in Redis using RedisService method
      String redisKey = "otp:" + identifier;
      boolean otpExists = redisService.exists(redisKey);
      log.info("DEBUG: OTP existence check - Key exists: {}, Key: {}", otpExists, redisKey);

      // Get stored OTP for comparison using RedisService method
      String storedOtp = redisService.get(redisKey, String.class);
      log.info(
          "DEBUG: Stored OTP comparison - Stored: {}, Input: {}, Match: {}",
          storedOtp,
          otp,
          (storedOtp != null && storedOtp.equals(otp.trim())));

      // Check attempts - Fixed key handling
      String attemptsIdentifier = identifier; // Use identifier directly for rate limiting
      long attempts = redisService.getRateLimitCount(attemptsIdentifier);
      if (attempts >= maxAttempts) {
        log.warn("Maksimal attempts tercapai untuk OTP validation: {}", identifier);
        auditLog(phone, purpose, "OTP_MAX_ATTEMPTS", "BLOCKED");
        // Return false instead of throwing exception to prevent 500 error
        return false;
      }

      // Increment attempts
      redisService.incrementRateLimit(attemptsIdentifier, otpTtlMinutes);

      // Validate OTP using RedisService method (this will also delete the OTP if valid)
      boolean isValid = redisService.validateOtp(identifier, otp.trim());

      if (isValid) {
        // Clear attempts counter jika berhasil - Fixed key handling
        String rateLimitKey = "rate_limit:" + attemptsIdentifier;
        redisService.delete(rateLimitKey);
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
      String redisKey = "otp:" + identifier;
      boolean deleted = redisService.delete(redisKey);

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
      String redisKey = "otp:" + identifier;
      return redisService.exists(redisKey);
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
      String redisKey = "otp:" + identifier;
      return redisService.getTtl(redisKey);
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
   * Send OTP dengan response DTO (untuk AuthController)
   *
   * @param phone Nomor telepon tujuan
   * @param purpose Tujuan OTP
   * @return OtpResponse dengan status dan informasi
   */
  public OtpResponse sendOtp(String phone, String purpose) {
    try {
      log.info("DEBUG: sendOtp called - Original phone: {}, Purpose: {}", phone, purpose);

      // Validate input parameters
      if (phone == null || phone.trim().isEmpty()) {
        log.error("Phone number is null or empty");
        return OtpResponse.error("Nomor telepon tidak boleh kosong");
      }

      if (purpose == null || purpose.trim().isEmpty()) {
        purpose = "login"; // Default purpose
      }

      // Normalize phone number with better error handling
      String normalizedPhone;
      try {
        normalizedPhone = normalizePhoneNumber(phone);
        log.info(
            "DEBUG: Phone normalization in sendOtp - Original: {}, Normalized: {}",
            phone,
            normalizedPhone);
      } catch (IllegalArgumentException e) {
        log.error("Phone normalization failed for phone: {}, error: {}", phone, e.getMessage());
        // Try with original phone number as fallback
        normalizedPhone = phone.replaceAll("[^0-9]", "");
        log.info(
            "DEBUG: Using fallback phone normalization in sendOtp - Original: {}, Fallback: {}",
            phone,
            normalizedPhone);
      }

      // Generate and send OTP
      String otp = generateAndSendOtp(normalizedPhone, purpose);

      if (otp != null) {
        long ttlSeconds = otpTtlMinutes * 60L;
        String maskedPhone = maskPhoneNumber(normalizedPhone);

        return OtpResponse.success(
            "OTP berhasil dikirim ke " + maskedPhone, maskedPhone, ttlSeconds, purpose);
      } else {
        String waDetail = whatsAppService.getLastFailureDetail();
        String detail =
            (waDetail != null && !waDetail.isBlank())
                ? waDetail
                : "Gagal mengirim OTP. Silakan coba lagi.";
        return OtpResponse.error(OtpErrorType.OTP_SERVICE_ERROR, detail);
      }

    } catch (Exception e) {
      log.error("Error in sendOtp for phone {}: {}", phone, e.getMessage(), e);

      // Categorize error based on exception message
      OtpErrorType errorType;
      if (e.getMessage() != null) {
        String errorMessage = e.getMessage().toLowerCase();
        if (errorMessage.contains("connection") || errorMessage.contains("network")) {
          errorType = OtpErrorType.NETWORK_ERROR;
        } else if (errorMessage.contains("phone") || errorMessage.contains("number")) {
          errorType = OtpErrorType.INVALID_PHONE_NUMBER;
        } else if (errorMessage.contains("rate") || errorMessage.contains("limit")) {
          errorType = OtpErrorType.RATE_LIMIT_EXCEEDED;
        } else if (errorMessage.contains("whatsapp")) {
          errorType = OtpErrorType.PHONE_NOT_REGISTERED;
        } else {
          errorType = OtpErrorType.SYSTEM_ERROR;
        }
      } else {
        errorType = OtpErrorType.SYSTEM_ERROR;
      }

      return OtpResponse.error(errorType, e.getMessage());
    }
  }

  /**
   * Verify OTP dengan response boolean (untuk AuthController)
   *
   * @param phone Nomor telepon
   * @param otp Kode OTP
   * @param purpose Tujuan OTP
   * @return true jika OTP valid
   */
  public boolean verifyOtp(String phone, String otp, String purpose) {
    try {
      log.info(
          "DEBUG: verifyOtp called - Original phone: {}, OTP: {}, Purpose: {}",
          phone,
          otp,
          purpose);

      // Validate input parameters
      if (phone == null || phone.trim().isEmpty()) {
        log.error("Phone number is null or empty");
        return false;
      }

      if (canBypassOtp(phone)) {
        return true;
      }

      if (otp == null || otp.trim().isEmpty()) {
        log.error("OTP is null or empty");
        return false;
      }

      if (purpose == null || purpose.trim().isEmpty()) {
        purpose = "login"; // Default purpose
      }

      // Normalize phone number with better error handling
      String normalizedPhone;
      try {
        normalizedPhone = normalizePhoneNumber(phone);
        log.info(
            "DEBUG: Phone normalization - Original: {}, Normalized: {}", phone, normalizedPhone);
      } catch (IllegalArgumentException e) {
        log.error("Phone normalization failed for phone: {}, error: {}", phone, e.getMessage());
        // Try with original phone number as fallback
        normalizedPhone = phone.replaceAll("[^0-9]", "");
        log.info(
            "DEBUG: Using fallback phone normalization - Original: {}, Fallback: {}",
            phone,
            normalizedPhone);
      }

      // Validate OTP
      return validateOtp(normalizedPhone, otp, purpose);

    } catch (Exception e) {
      log.error("Error in verifyOtp for phone {}: {}", phone, e.getMessage(), e);
      // Return false instead of throwing exception to prevent 500 error
      return false;
    }
  }

  /**
   * Normalize phone number (convert 08xx to 628xx)
   *
   * @param phone Raw phone number
   * @return Normalized phone number with 62 prefix
   */
  public String normalizePhoneNumber(String phone) {
    if (phone == null || phone.trim().isEmpty()) {
      throw new IllegalArgumentException("Nomor telepon tidak boleh kosong");
    }

    // Remove all non-digit characters
    String cleanPhone = phone.replaceAll("[^0-9]", "");

    // Validate minimum length
    if (cleanPhone.length() < 10) {
      throw new IllegalArgumentException("Nomor telepon terlalu pendek (minimal 10 digit)");
    }

    // Handle different formats
    if (cleanPhone.startsWith("08")) {
      // Convert 08xx to 628xx
      return "62" + cleanPhone.substring(1);
    } else if (cleanPhone.startsWith("628")) {
      // Already in correct format
      return cleanPhone;
    } else if (cleanPhone.startsWith("62")) {
      // Check if it's 62 followed by valid mobile prefix
      if (cleanPhone.length() >= 4) {
        String mobilePrefix = cleanPhone.substring(2, 4);
        if (mobilePrefix.equals("81")
            || mobilePrefix.equals("82")
            || mobilePrefix.equals("83")
            || mobilePrefix.equals("85")
            || mobilePrefix.equals("87")
            || mobilePrefix.equals("88")
            || mobilePrefix.equals("89")) {
          return cleanPhone;
        }
      }
      // If not valid mobile prefix, try to fix it
      log.warn("Invalid mobile prefix for phone: {}, trying to fix", cleanPhone);
      if (cleanPhone.length() >= 3 && cleanPhone.substring(2, 3).equals("8")) {
        // Might be missing a digit, return as is and let validation handle it
        return cleanPhone;
      }
      throw new IllegalArgumentException("Format nomor telepon tidak valid");
    } else if (cleanPhone.startsWith("8")) {
      // Handle 8xxxxxxxxx format (missing leading 0)
      return "62" + cleanPhone;
    } else {
      // Try to salvage the phone number
      log.warn("Unusual phone format: {}, attempting to normalize", cleanPhone);

      // If it looks like it might be a valid Indonesian mobile number
      if (cleanPhone.length() >= 10 && cleanPhone.length() <= 15) {
        // Try adding 62 prefix
        if (cleanPhone.startsWith("1")
            || cleanPhone.startsWith("2")
            || cleanPhone.startsWith("3")
            || cleanPhone.startsWith("4")
            || cleanPhone.startsWith("5")
            || cleanPhone.startsWith("6")
            || cleanPhone.startsWith("7")
            || cleanPhone.startsWith("9")) {
          throw new IllegalArgumentException("Nomor telepon harus dimulai dengan 08 atau 62");
        }
        // If starts with 8, assume it's missing 62 prefix
        if (cleanPhone.startsWith("8")) {
          return "62" + cleanPhone;
        }
      }

      throw new IllegalArgumentException("Nomor telepon harus dimulai dengan 08 atau 62");
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

  private boolean canBypassOtp(String phone) {
    if (phone == null) return false;
    return PHONE_ALLOWED_TO_BYPASS_OTP.contains(phone.trim());
  }
}
