package com.kelompoksatu.griya.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Konfigurasi Redis untuk aplikasi BNI KPR
 *
 * <p>Konfigurasi ini menyediakan: - RedisTemplate dengan serializer yang tepat - Connection factory
 * configuration - Serialization strategy untuk key dan value
 *
 * <p>Security considerations: - Menggunakan JSON serializer untuk compatibility - String serializer
 * untuk key agar mudah di-debug - Connection pooling untuk performance
 */
@Configuration
public class RedisConfig {

  /**
   * Konfigurasi RedisTemplate dengan serializer yang tepat
   *
   * @param connectionFactory Redis connection factory
   * @return RedisTemplate yang sudah dikonfigurasi
   */
  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    // Serializer untuk key - menggunakan String serializer
    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());

    // Serializer untuk value - menggunakan JSON serializer
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

    // Enable transaction support
    template.setEnableTransactionSupport(true);

    template.afterPropertiesSet();
    return template;
  }
}
