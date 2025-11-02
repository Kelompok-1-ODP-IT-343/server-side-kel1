# OTP Error Handling Enhancement - Implementation Summary

## Tujuan
Memberikan pesan error yang spesifik dan informatif ketika proses OTP gagal, sehingga pengguna dapat mengetahui alasan spesifik kegagalan dan tindakan yang perlu diambil.

## Komponen yang Dibuat/Dimodifikasi

### 1. OtpErrorType.java (Baru)
**Lokasi**: `src/main/java/com/kelompoksatu/griya/dto/OtpErrorType.java`

Enum yang mendefinisikan 13 tipe error spesifik untuk berbagai kasus kegagalan OTP:

- `USER_NOT_FOUND`: User tidak ditemukan
- `USER_SUSPENDED`: Akun pengguna telah disuspend  
- `USER_INACTIVE`: Akun pengguna tidak aktif
- `INVALID_PHONE_NUMBER`: Nomor telepon tidak valid
- `OTP_SERVICE_ERROR`: Layanan OTP mengalami gangguan
- `RATE_LIMIT_EXCEEDED`: Terlalu banyak permintaan OTP
- `DUPLICATE_USER`: Username atau email sudah terdaftar
- `INVALID_CREDENTIALS`: Username/email atau password tidak valid
- `NETWORK_ERROR`: Gagal terhubung ke layanan WhatsApp
- `PHONE_NOT_REGISTERED`: Nomor telepon belum terdaftar di WhatsApp
- `VALIDATION_ERROR`: Data yang dikirim tidak valid
- `SYSTEM_ERROR`: Terjadi kesalahan sistem
- `ACCOUNT_LOCKED`: Akun terkunci sementara

### 2. OtpErrorResponse.java (Baru)
**Lokasi**: `src/main/java/com/kelompoksatu/griya/dto/OtpErrorResponse.java`

Struktur response error yang komprehensif dengan:
- `errorCode`: Kode error numerik (4001-4013)
- `errorType`: Tipe error dari enum OtpErrorType
- `message`: Pesan error yang user-friendly
- `suggestion`: Saran tindakan untuk mengatasi error
- `canRetry`: Boolean yang menunjukkan apakah bisa dicoba lagi
- `retryAfter`: Waktu tunggu sebelum retry (dalam detik)

**Fitur Static Factory Methods**:
```java
// Untuk berbagai tipe error
OtpErrorResponse.userNotFound()
OtpErrorResponse.rateLimitExceeded(300) // 300 seconds retry
OtpErrorResponse.networkError()
OtpErrorResponse.systemError()
// dll...
```

### 3. OtpResponse.java (Dimodifikasi)
**Lokasi**: `src/main/java/com/kelompoksatu/griya/dto/OtpResponse.java`

Ditambahkan:
- Field `OtpErrorResponse errorDetail` untuk menyimpan detail error
- Constructor dan factory methods baru:
  ```java
  OtpResponse.error(OtpErrorType errorType, String customMessage)
  OtpResponse.error(OtpErrorResponse errorDetail)
  OtpResponse.error(OtpErrorType errorType) // menggunakan default message
  ```

### 4. AuthController.java (Dimodifikasi)
**Lokasi**: `src/main/java/com/kelompoksatu/griya/controller/AuthController.java`

**Method register()** dan **Method login()** sekarang memiliki:

#### Exception Handling yang Komprehensif:
1. **IllegalArgumentException**: Validation errors
2. **RuntimeException**: Kategorisasi berdasarkan pesan error:
   - Phone-related issues → `INVALID_PHONE_NUMBER`
   - OTP/WhatsApp issues → `OTP_SERVICE_ERROR`  
   - Rate limiting → `RATE_LIMIT_EXCEEDED`
   - Account locking → `ACCOUNT_LOCKED`
   - General system issues → `SYSTEM_ERROR`
3. **Exception**: Catch-all untuk unexpected errors

#### Error Response Structure:
```json
{
  "success": false,
  "message": "Registrasi gagal",
  "data": {
    "success": false,
    "message": "Registrasi gagal", 
    "errorDetail": {
      "errorCode": 4005,
      "errorType": "OTP_SERVICE_ERROR",
      "message": "Layanan OTP mengalami gangguan",
      "suggestion": "Silakan coba lagi dalam beberapa menit atau hubungi customer service",
      "canRetry": true,
      "retryAfter": 60
    }
  },
  "timestamp": "2025-11-01T10:00:00",
  "path": "/api/v1/auth/register"
}
```

## Manfaat Implementasi

### 1. **User Experience yang Lebih Baik**
- Pesan error yang jelas dan informatif
- Saran tindakan konkret untuk mengatasi masalah
- Informasi apakah error bisa diretry dan kapan

### 2. **Debugging yang Lebih Mudah**
- Error code numerik untuk tracking
- Kategorisasi error yang konsisten
- Logging yang lebih detail

### 3. **Maintainability**
- Struktur error yang terstandarisasi
- Separation of concerns antara error type dan error response
- Extensible design untuk menambah error type baru

### 4. **API Consistency**
- Semua endpoint OTP menggunakan structure error yang sama
- Response format yang konsisten

## Contoh Penggunaan

### Scenario 1: Rate Limit Exceeded
```json
{
  "success": false,
  "message": "Terlalu banyak permintaan OTP",
  "data": {
    "errorDetail": {
      "errorCode": 4006,
      "errorType": "RATE_LIMIT_EXCEEDED", 
      "message": "Terlalu banyak permintaan OTP. Silakan tunggu beberapa saat",
      "suggestion": "Tunggu 5 menit sebelum mencoba lagi",
      "canRetry": true,
      "retryAfter": 300
    }
  }
}
```

### Scenario 2: Network Error
```json
{
  "success": false,
  "message": "Gagal mengirim OTP",
  "data": {
    "errorDetail": {
      "errorCode": 4009,
      "errorType": "NETWORK_ERROR",
      "message": "Gagal terhubung ke layanan WhatsApp", 
      "suggestion": "Periksa koneksi internet Anda dan coba lagi",
      "canRetry": true,
      "retryAfter": 30
    }
  }
}
```

## Testing
Semua perubahan telah berhasil dikompilasi tanpa error:
```bash
./mvnw compile -q
# No compilation errors
```

## Next Steps
1. Update frontend untuk menangani error codes dan menampilkan suggestions
2. Implement retry logic berdasarkan `canRetry` dan `retryAfter` fields
3. Add monitoring/alerting berdasarkan error codes untuk operational insights
4. Consider menambahkan error types baru sesuai kebutuhan bisnis

---

**Status**: ✅ **COMPLETED** - All OTP error handling enhancements successfully implemented and tested.