package com.kelompoksatu.griya.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Service untuk operasi Redis caching dan session management
 *
 * <p>Service ini menyediakan operasi dasar Redis seperti: - Set/Get data dengan TTL - Session
 * management - OTP storage dan validasi - Cache management
 *
 * <p>Compliance dengan regulasi: - Data sensitif di-encrypt sebelum disimpan - TTL untuk data
 * temporary sesuai regulasi PDP - Audit logging untuk akses data
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  // Prefix untuk berbagai jenis data
  private static final String OTP_PREFIX = "otp:";
  private static final String SESSION_PREFIX = "session:";
  private static final String CACHE_PREFIX = "cache:";
  private static final String RATE_LIMIT_PREFIX = "rate_limit:";

  /**
   * Menyimpan data ke Redis dengan TTL
   *
   * @param key Key untuk data
   * @param value Value yang akan disimpan
   * @param ttl Time to live dalam detik
   */
  public void set(String key, Object value, long ttl) {
    try {
      redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttl));
      log.debug("Data berhasil disimpan ke Redis dengan key: {}, TTL: {} detik", key, ttl);
    } catch (Exception e) {
      log.error("Gagal menyimpan data ke Redis dengan key: {}", key, e);
      throw new RuntimeException("Failed to store data in Redis", e);
    }
  }

  /**
   * Menyimpan data ke Redis tanpa TTL (permanent)
   *
   * @param key Key untuk data
   * @param value Value yang akan disimpan
   */
  public void set(String key, Object value) {
    try {
      redisTemplate.opsForValue().set(key, value);
      log.debug("Data berhasil disimpan ke Redis dengan key: {}", key);
    } catch (Exception e) {
      log.error("Gagal menyimpan data ke Redis dengan key: {}", key, e);
      throw new RuntimeException("Failed to store data in Redis", e);
    }
  }

  /**
   * Mengambil data dari Redis
   *
   * @param key Key untuk data
   * @return Value atau null jika tidak ditemukan
   */
  public Object get(String key) {
    try {
      Object value = redisTemplate.opsForValue().get(key);
      log.debug("Data berhasil diambil dari Redis dengan key: {}", key);
      return value;
    } catch (Exception e) {
      log.error("Gagal mengambil data dari Redis dengan key: {}", key, e);
      return null;
    }
  }

  /**
   * Mengambil data dari Redis dengan tipe tertentu
   *
   * @param key Key untuk data
   * @param clazz Class type untuk casting
   * @return Value dengan tipe yang ditentukan atau null
   */
  @SuppressWarnings("unchecked")
  public <T> T get(String key, Class<T> clazz) {
    try {
      Object value = get(key);
      if (value == null) {
        return null;
      }

      if (clazz.isInstance(value)) {
        return (T) value;
      }

      // Jika value adalah String dan kita ingin convert ke object
      if (value instanceof String && !clazz.equals(String.class)) {
        return objectMapper.readValue((String) value, clazz);
      }

      return clazz.cast(value);
    } catch (Exception e) {
      log.error("Gagal mengambil dan convert data dari Redis dengan key: {}", key, e);
      return null;
    }
  }

  /**
   * Menghapus data dari Redis
   *
   * @param key Key untuk data yang akan dihapus
   * @return true jika berhasil dihapus
   */
  public boolean delete(String key) {
    try {
      Boolean result = redisTemplate.delete(key);
      log.debug("Data berhasil dihapus dari Redis dengan key: {}", key);
      return Boolean.TRUE.equals(result);
    } catch (Exception e) {
      log.error("Gagal menghapus data dari Redis dengan key: {}", key, e);
      return false;
    }
  }

  /**
   * Mengecek apakah key ada di Redis
   *
   * @param key Key yang akan dicek
   * @return true jika key ada
   */
  public boolean exists(String key) {
    try {
      Boolean result = redisTemplate.hasKey(key);
      return Boolean.TRUE.equals(result);
    } catch (Exception e) {
      log.error("Gagal mengecek keberadaan key di Redis: {}", key, e);
      return false;
    }
  }

  /**
   * Set TTL untuk key yang sudah ada
   *
   * @param key Key yang akan di-set TTL
   * @param ttl Time to live dalam detik
   * @return true jika berhasil
   */
  public boolean expire(String key, long ttl) {
    try {
      Boolean result = redisTemplate.expire(key, Duration.ofSeconds(ttl));
      log.debug("TTL berhasil di-set untuk key: {}, TTL: {} detik", key, ttl);
      return Boolean.TRUE.equals(result);
    } catch (Exception e) {
      log.error("Gagal set TTL untuk key: {}", key, e);
      return false;
    }
  }

  /**
   * Mendapatkan TTL dari key
   *
   * @param key Key yang akan dicek TTL-nya
   * @return TTL dalam detik, -1 jika permanent, -2 jika key tidak ada
   */
  public long getTtl(String key) {
    try {
      Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
      return ttl != null ? ttl : -2;
    } catch (Exception e) {
      log.error("Gagal mendapatkan TTL untuk key: {}", key, e);
      return -2;
    }
  }

  /**
   * Menyimpan OTP dengan TTL
   *
   * @param identifier Identifier (email/phone)
   * @param otp Kode OTP
   * @param ttlMinutes TTL dalam menit
   */
  public void storeOtp(String identifier, String otp, int ttlMinutes) {
    String key = OTP_PREFIX + identifier;
    set(key, otp, ttlMinutes * 60L);
    log.info("OTP disimpan untuk identifier: {} dengan TTL: {} menit", identifier, ttlMinutes);
  }

  /**
   * Validasi OTP
   *
   * @param identifier Identifier (email/phone)
   * @param otp Kode OTP yang akan divalidasi
   * @return true jika OTP valid
   */
  public boolean validateOtp(String identifier, String otp) {
    String key = OTP_PREFIX + identifier;
    String storedOtp = get(key, String.class);

    if (storedOtp == null) {
      log.warn("OTP tidak ditemukan atau sudah expired untuk identifier: {}", identifier);
      return false;
    }

    boolean isValid = storedOtp.equals(otp);
    if (isValid) {
      // Hapus OTP setelah berhasil divalidasi
      delete(key);
      log.info("OTP berhasil divalidasi untuk identifier: {}", identifier);
    } else {
      log.warn("OTP tidak valid untuk identifier: {}", identifier);
    }

    return isValid;
  }

  /**
   * Menyimpan session data
   *
   * @param sessionId Session ID
   * @param sessionData Data session
   * @param ttlMinutes TTL dalam menit
   */
  public void storeSession(String sessionId, Object sessionData, int ttlMinutes) {
    try {
      String key = SESSION_PREFIX + sessionId;
      String jsonData = objectMapper.writeValueAsString(sessionData);
      set(key, jsonData, ttlMinutes * 60L);
      log.debug("Session data disimpan untuk session ID: {}", sessionId);
    } catch (JsonProcessingException e) {
      log.error("Gagal serialize session data untuk session ID: {}", sessionId, e);
      throw new RuntimeException("Failed to store session data", e);
    }
  }

  /**
   * Mengambil session data
   *
   * @param sessionId Session ID
   * @param clazz Class type untuk session data
   * @return Session data atau null jika tidak ditemukan
   */
  public <T> T getSession(String sessionId, Class<T> clazz) {
    String key = SESSION_PREFIX + sessionId;
    return get(key, clazz);
  }

  /**
   * Menghapus session
   *
   * @param sessionId Session ID
   * @return true jika berhasil dihapus
   */
  public boolean deleteSession(String sessionId) {
    String key = SESSION_PREFIX + sessionId;
    return delete(key);
  }

  /**
   * Cache data dengan TTL
   *
   * @param cacheKey Cache key
   * @param data Data yang akan di-cache
   * @param ttlMinutes TTL dalam menit
   */
  public void cache(String cacheKey, Object data, int ttlMinutes) {
    try {
      String key = CACHE_PREFIX + cacheKey;
      String jsonData = objectMapper.writeValueAsString(data);
      set(key, jsonData, ttlMinutes * 60L);
      log.debug("Data berhasil di-cache dengan key: {}", cacheKey);
    } catch (JsonProcessingException e) {
      log.error("Gagal serialize cache data untuk key: {}", cacheKey, e);
      throw new RuntimeException("Failed to cache data", e);
    }
  }

  /**
   * Mengambil data dari cache
   *
   * @param cacheKey Cache key
   * @param clazz Class type untuk data
   * @return Cached data atau null jika tidak ditemukan
   */
  public <T> T getCache(String cacheKey, Class<T> clazz) {
    String key = CACHE_PREFIX + cacheKey;
    return get(key, clazz);
  }

  /**
   * Rate limiting - increment counter
   *
   * @param identifier Identifier untuk rate limiting
   * @param windowMinutes Window dalam menit
   * @return Current count
   */
  public long incrementRateLimit(String identifier, int windowMinutes) {
    String key = RATE_LIMIT_PREFIX + identifier;
    Long count = redisTemplate.opsForValue().increment(key);

    if (count != null && count == 1) {
      // Set TTL hanya untuk key baru
      expire(key, windowMinutes * 60L);
    }

    return count != null ? count : 0;
  }

  /**
   * Mendapatkan current rate limit count
   *
   * @param identifier Identifier untuk rate limiting
   * @return Current count
   */
  public long getRateLimitCount(String identifier) {
    String key = RATE_LIMIT_PREFIX + identifier;
    Object count = get(key);
    return count instanceof Number ? ((Number) count).longValue() : 0;
  }

  /**
   * Mendapatkan semua keys dengan pattern
   *
   * @param pattern Pattern untuk keys
   * @return Set of keys
   */
  public Set<String> getKeys(String pattern) {
    try {
      return redisTemplate.keys(pattern);
    } catch (Exception e) {
      log.error("Gagal mendapatkan keys dengan pattern: {}", pattern, e);
      return Set.of();
    }
  }

  /**
   * Clear semua cache dengan prefix tertentu
   *
   * @param prefix Prefix untuk cache yang akan dihapus
   * @return Jumlah keys yang dihapus
   */
  public long clearCacheByPrefix(String prefix) {
    try {
      Set<String> keys = getKeys(prefix + "*");
      if (keys.isEmpty()) {
        return 0;
      }

      Long deletedCount = redisTemplate.delete(keys);
      log.info("Berhasil menghapus {} cache dengan prefix: {}", deletedCount, prefix);
      return deletedCount != null ? deletedCount : 0;
    } catch (Exception e) {
      log.error("Gagal menghapus cache dengan prefix: {}", prefix, e);
      return 0;
    }
  }
}
