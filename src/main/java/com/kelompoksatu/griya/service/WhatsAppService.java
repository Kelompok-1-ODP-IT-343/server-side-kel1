package com.kelompoksatu.griya.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service untuk mengirim pesan WhatsApp melalui API
 *
 * <p>Service ini menggunakan HTTP client untuk berkomunikasi dengan WhatsApp API dan mengirim pesan
 * ke nomor telepon yang ditentukan.
 *
 * <p>Features: - Mengirim pesan WhatsApp via HTTP POST request - Validasi nomor telepon Indonesia -
 * Error handling dan logging - Timeout configuration untuk request
 */
@Slf4j
@Service
public class WhatsAppService {

  @Value("${whatsapp.api.url:http://localhost:8080/api/send-message}")
  private String whatsappApiUrl;

  @Value("${whatsapp.api.key:test123}")
  private String apiKey;

  @Value("${whatsapp.api.header-name:X-API-Key}")
  private String apiKeyHeaderName;

  @Value("${whatsapp.api.timeout:30}")
  private int timeoutSeconds;

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  // Store last failure information to enrich upstream error responses
  private volatile String lastFailureDetail;
  private volatile int lastStatusCode;

  public WhatsAppService() {
    this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Mengirim pesan WhatsApp ke nomor telepon yang ditentukan
   *
   * @param phone Nomor telepon tujuan (format: 6281234567890)
   * @param message Pesan yang akan dikirim
   * @return true jika berhasil, false jika gagal
   */
  public boolean sendMessage(String phone, String message) {
    try {
      // reset last error info for this attempt
      lastFailureDetail = null;
      lastStatusCode = 0;
      // Validasi input
      if (phone == null || phone.trim().isEmpty()) {
        log.error("Nomor telepon tidak boleh kosong");
        return false;
      }

      if (message == null || message.trim().isEmpty()) {
        log.error("Pesan tidak boleh kosong");
        return false;
      }

      // Format nomor telepon Indonesia
      String formattedPhone = formatPhoneNumber(phone);
      if (formattedPhone == null) {
        log.error("Format nomor telepon tidak valid: {}", phone);
        return false;
      }

      // Buat request body
      Map<String, String> requestBody = Map.of("phone", formattedPhone, "message", message.trim());

      String jsonBody = objectMapper.writeValueAsString(requestBody);

      // Buat HTTP request
      log.info(
          "Sending WhatsApp message to {} via {} (timeout {}s)",
          formattedPhone,
          whatsappApiUrl,
          timeoutSeconds);
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(whatsappApiUrl))
              .timeout(Duration.ofSeconds(timeoutSeconds))
              .header("Content-Type", "application/json")
              .header(apiKeyHeaderName, apiKey)
              .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
              .build();

      // Kirim request
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      // Log response
      log.info(
          "WhatsApp API response - Status: {}, Body: {}", response.statusCode(), response.body());

      // Check response status
      if (response.statusCode() >= 200 && response.statusCode() < 300) {
        log.info("Pesan WhatsApp berhasil dikirim ke {}", formattedPhone);
        return true;
      } else {
        lastStatusCode = response.statusCode();
        if (response.statusCode() == 401) {
          lastFailureDetail =
              String.format(
                  "WhatsApp API unauthorized (401). Periksa API key dan header '%s'. Response: %s",
                  apiKeyHeaderName, response.body());
        } else if (response.statusCode() == 403) {
          lastFailureDetail =
              String.format(
                  "WhatsApp API forbidden (403). Kredensial tidak memiliki akses. Response: %s",
                  response.body());
        } else if (response.statusCode() >= 500) {
          lastFailureDetail =
              String.format(
                  "WhatsApp API error (%d). Coba lagi nanti. Response: %s",
                  response.statusCode(), response.body());
        } else {
          lastFailureDetail =
              String.format(
                  "WhatsApp API returned %d. Response: %s", response.statusCode(), response.body());
        }
        log.error(
            "Gagal mengirim pesan WhatsApp. Status: {}, Response: {}",
            response.statusCode(),
            response.body());
        return false;
      }

    } catch (IOException e) {
      log.error("IO Error saat mengirim pesan WhatsApp: {}", e.getMessage(), e);
      lastFailureDetail = "IO error saat menghubungi WhatsApp API: " + e.getMessage();
      return false;
    } catch (InterruptedException e) {
      log.error("Request WhatsApp terinterupsi: {}", e.getMessage(), e);
      Thread.currentThread().interrupt();
      lastFailureDetail = "Request ke WhatsApp API terinterupsi: " + e.getMessage();
      return false;
    } catch (Exception e) {
      log.error("Unexpected error saat mengirim pesan WhatsApp: {}", e.getMessage(), e);
      lastFailureDetail = "Unexpected error WhatsApp API: " + e.getMessage();
      return false;
    }
  }

  /**
   * Mengirim notifikasi KPR ke nomor telepon
   *
   * @param phone Nomor telepon
   * @param applicationNumber Nomor aplikasi KPR
   * @param status Status aplikasi
   * @return true jika berhasil
   */
  public boolean sendKprNotification(String phone, String applicationNumber, String status) {
    String message =
        String.format(
            "🏠 *Notifikasi KPR BNI*\n\n"
                + "Nomor Aplikasi: %s\n"
                + "Status: %s\n\n"
                + "Terima kasih telah menggunakan layanan KPR BNI.\n"
                + "Untuk informasi lebih lanjut, silakan hubungi customer service kami.",
            applicationNumber, status);

    return sendMessage(phone, message);
  }

  /**
   * Mengirim OTP via WhatsApp
   *
   * @param phone Nomor telepon
   * @param otp Kode OTP
   * @return true jika berhasil
   */
  public boolean sendOtp(String phone, String otp) {
    String message =
        String.format(
            "🔐 *Kode Verifikasi BNI*\n\n"
                + "Kode OTP Anda: *%s*\n\n"
                + "Kode ini berlaku selama 5 menit.\n"
                + "Jangan bagikan kode ini kepada siapapun.\n\n"
                + "Jika Anda tidak meminta kode ini, abaikan pesan ini.",
            otp);

    return sendMessage(phone, message);
  }

  /**
   * Format nomor telepon ke format Indonesia yang benar
   *
   * @param phone Nomor telepon input
   * @return Nomor telepon yang sudah diformat atau null jika tidak valid
   */
  private String formatPhoneNumber(String phone) {
    if (phone == null) {
      return null;
    }

    // Hapus semua karakter non-digit
    String cleanPhone = phone.replaceAll("[^0-9]", "");

    // Validasi panjang minimal
    if (cleanPhone.length() < 10) {
      return null;
    }

    // Format ke 62xxx
    if (cleanPhone.startsWith("0")) {
      return "62" + cleanPhone.substring(1);
    } else if (cleanPhone.startsWith("62")) {
      return cleanPhone;
    } else if (cleanPhone.startsWith("8")) {
      return "62" + cleanPhone;
    }

    return null;
  }

  /**
   * Test koneksi ke WhatsApp API
   *
   * @return true jika API dapat diakses
   */
  public boolean testConnection() {
    try {
      // Kirim pesan test ke nomor dummy
      return sendMessage("6281234567890", "Test connection");
    } catch (Exception e) {
      log.error("Gagal test koneksi WhatsApp API: {}", e.getMessage());
      return false;
    }
  }

  /**
   * Mendapatkan detail kegagalan terakhir (jika ada) dari panggilan WhatsApp API. Berguna untuk
   * memperkaya error detail di lapisan service/controller.
   */
  public String getLastFailureDetail() {
    return lastFailureDetail;
  }

  /** Mendapatkan status code HTTP terakhir dari WhatsApp API (0 jika tidak ada). */
  public int getLastStatusCode() {
    return lastStatusCode;
  }
}
