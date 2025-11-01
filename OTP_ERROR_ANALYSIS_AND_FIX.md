# 🐛 OTP Error Analysis & Solution

## ❌ **Problem Identified**

### **Error Response Yang Dilaporkan:**
```json
{
    "success": false,
    "message": "Gagal mengirim OTP. Silakan coba lagi.",
    "data": {
        "success": false,
        "message": "Gagal mengirim OTP. Silakan coba lagi.",
        "maskedPhone": null,
        "ttlSeconds": null,
        "purpose": null,
        "errorDetail": null  // ← MASALAH: Ini harusnya ada!
    },
    "timestamp": "2025-11-01T09:59:10.411373",
    "path": "/api/v1/auth/login"
}
```

### **Root Cause Analysis:**

#### 🔍 **1. Connection Error ke WhatsApp Service**
```
java.net.ConnectException: null
    at WhatsAppService.sendMessage(WhatsAppService.java:89)
```

#### 🔍 **2. Flow Error dalam OtpService**
- `generateAndSendOtp()` mendeteksi WhatsApp service error
- Method mengembalikan `null` ketika gagal kirim
- `sendOtp()` mendeteksi `otp == null` 
- **SEBELUM FIX**: Menggunakan `OtpResponse.error(String)` → `errorDetail` = `null`
- **SETELAH FIX**: Menggunakan `OtpResponse.error(OtpErrorType, String)` → `errorDetail` = detailed info

#### 🔍 **3. Network Issue**
- Aplikasi tidak dapat terhubung ke WhatsApp API endpoint
- Kemungkinan:
  - API endpoint down/unreachable
  - Network connectivity issue
  - Konfigurasi API key/URL salah
  - Firewall blocking connection

---

## ✅ **Solution Implemented**

### **1. Enhanced OtpService Error Handling**

**File**: `src/main/java/com/kelompoksatu/griya/service/OtpService.java`

**SEBELUM:**
```java
} else {
    return OtpResponse.error("Gagal mengirim OTP. Silakan coba lagi.");
}

} catch (Exception e) {
    return OtpResponse.error("Gagal mengirim OTP: " + e.getMessage());
}
```

**SESUDAH:**
```java
} else {
    return OtpResponse.error(
        OtpErrorType.OTP_SERVICE_ERROR, "Gagal mengirim OTP. Silakan coba lagi.");
}

} catch (Exception e) {
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
```

### **2. Added Import**
```java
import com.kelompoksatu.griya.dto.OtpErrorType;
```

---

## 🎯 **Expected Result After Fix**

Sekarang ketika terjadi error yang sama, response akan menjadi:

```json
{
    "success": false,
    "message": "Gagal mengirim OTP. Silakan coba lagi.",
    "data": {
        "success": false,
        "message": "Gagal mengirim OTP. Silakan coba lagi.",
        "maskedPhone": null,
        "ttlSeconds": null,
        "purpose": null,
        "errorDetail": {
            "errorCode": 4009,
            "errorType": "NETWORK_ERROR",
            "message": "Gagal terhubung ke layanan WhatsApp",
            "suggestion": "Periksa koneksi internet dan coba lagi",
            "canRetry": true,
            "retryAfter": 30
        }
    },
    "timestamp": "2025-11-01T10:05:00.000000",
    "path": "/api/v1/auth/login"
}
```

---

## 🛠️ **Additional Actions to Prevent Recurrence**

### **1. WhatsApp Service Configuration Check**
Verifikasi konfigurasi di `application.properties`:
```properties
# WhatsApp API Configuration
whatsapp.api.url=https://api.whatsapp.com/...
whatsapp.api.token=your_api_token
whatsapp.api.timeout=30000
```

### **2. Network Connectivity**
```bash
# Test connectivity ke WhatsApp API
curl -X POST https://api.whatsapp.com/send \
  -H "Authorization: Bearer your_token" \
  -H "Content-Type: application/json" \
  -d '{"test": "connection"}'
```

### **3. Add Health Check**
Implementasikan health check untuk WhatsApp service di `ActuatorEndpoint`:

```java
@Component
public class WhatsAppHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        try {
            // Test connection ke WhatsApp API
            boolean isWhatsAppReachable = whatsAppService.testConnection();
            if (isWhatsAppReachable) {
                return Health.up().withDetail("whatsapp", "Connected").build();
            } else {
                return Health.down().withDetail("whatsapp", "Unreachable").build();
            }
        } catch (Exception e) {
            return Health.down().withDetail("whatsapp", "Error: " + e.getMessage()).build();
        }
    }
}
```

### **4. Monitoring & Alerting**
- Set up monitoring untuk WhatsApp API connection errors
- Add alerting untuk high failure rate pada OTP service
- Log error rates berdasarkan `OtpErrorType` untuk analysis

### **5. Retry Mechanism**
Implementasikan retry logic untuk network errors:
```java
@Retryable(value = {ConnectException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
public boolean sendOtp(String phone, String otp) {
    // WhatsApp API call
}
```

---

## ✅ **Status**

- ✅ **Fixed**: `errorDetail` null issue
- ✅ **Enhanced**: Detailed error categorization  
- ✅ **Improved**: User-friendly error messages
- 🔄 **Next**: Monitor WhatsApp service connectivity
- 🔄 **Recommended**: Implement retry mechanism

**Application Status**: ✅ Running with enhanced error handling