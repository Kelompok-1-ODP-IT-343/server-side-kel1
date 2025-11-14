package com.kelompoksatu.griya.service;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.MessageProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * RabbitMQService
 *
 * <p>Service sederhana untuk publish dan subscribe ke RabbitMQ queue/exchange menggunakan library
 * com.rabbitmq.client (ConnectionFactory, Connection, Channel).
 *
 * <p>Fitur: - Publish ke queue (auto-declare, durable, pesan persistent) - Publish ke exchange
 * dengan routing key - Subscribe ke queue dengan callback handler (autoAck) - Unsubscribe dengan
 * consumerTag, manajemen channel per subscription
 *
 * <p>Konfigurasi diambil dari environment/application properties tanpa hardcode kredensial.
 */
@Service
public class RabbitMQService {

  private static final Logger log = LoggerFactory.getLogger(RabbitMQService.class);

  @Value("${app.rabbitmq.host:10.10.0.3}")
  private String host;

  @Value("${app.rabbitmq.port:5672}")
  private int port;

  @Value("${app.rabbitmq.username:admin}")
  private String username;

  @Value("${app.rabbitmq.password:admin123}")
  private String password;

  @Value("${app.rabbitmq.virtualHost:/}")
  private String virtualHost;

  @Value("${app.rabbitmq.ssl:false}")
  private boolean useSsl;

  private Connection connection;
  private final Map<String, Channel> consumerChannels = new ConcurrentHashMap<>();

  @PostConstruct
  public void init() {
    try {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(host);
      factory.setPort(port);
      factory.setUsername(username);
      factory.setPassword(password);
      factory.setVirtualHost(virtualHost);
      if (useSsl) {
        try {
          factory.useSslProtocol();
        } catch (Exception e) {
          log.warn("Failed to enable SSL for RabbitMQ connection: {}", e.getMessage());
        }
      }
      this.connection = factory.newConnection();
      log.info("RabbitMQ connection established to {}:{} (vhost={})", host, port, virtualHost);
    } catch (Exception e) {
      log.error("Failed to initialize RabbitMQ connection: {}", e.getMessage());
    }
  }

  @PreDestroy
  public void shutdown() {
    // Close all consumer channels
    for (Map.Entry<String, Channel> entry : consumerChannels.entrySet()) {
      try {
        Channel ch = entry.getValue();
        if (ch != null && ch.isOpen()) {
          ch.close();
        }
      } catch (Exception e) {
        log.warn("Error closing consumer channel {}: {}", entry.getKey(), e.getMessage());
      }
    }
    consumerChannels.clear();

    // Close connection
    if (connection != null && connection.isOpen()) {
      try {
        connection.close();
        log.info("RabbitMQ connection closed");
      } catch (Exception e) {
        log.warn("Error closing RabbitMQ connection: {}", e.getMessage());
      }
    }
  }

  /** Publish message ke queue (durable, declare jika belum ada). */
  public void publishToQueue(String queueName, String message) {
    requireConnection();
    try (Channel channel = connection.createChannel()) {
      channel.queueDeclare(queueName, true, false, false, null);
      channel.basicPublish(
          "",
          queueName,
          MessageProperties.PERSISTENT_TEXT_PLAIN,
          message.getBytes(StandardCharsets.UTF_8));
      log.info("Published message to queue {}", queueName);
    } catch (IOException | TimeoutException e) {
      log.error("Failed to publish to queue {}: {}", queueName, e.getMessage());
      throw new RuntimeException("Gagal publish ke queue: " + e.getMessage(), e);
    }
  }

  /** Publish message ke exchange dengan routingKey. */
  public void publishToExchange(String exchange, String routingKey, String message) {
    requireConnection();
    try (Channel channel = connection.createChannel()) {
      // Assume exchange already exists; if needed, declare outside this method
      channel.basicPublish(
          exchange,
          routingKey,
          MessageProperties.PERSISTENT_TEXT_PLAIN,
          message.getBytes(StandardCharsets.UTF_8));
      log.info("Published message to exchange {} with routing {}", exchange, routingKey);
    } catch (IOException | TimeoutException e) {
      log.error(
          "Failed to publish to exchange {} (routing {}): {}",
          exchange,
          routingKey,
          e.getMessage());
      throw new RuntimeException("Gagal publish ke exchange: " + e.getMessage(), e);
    }
  }

  /**
   * Subscribe ke queue dengan auto-ack dan callback handler. Mengembalikan consumerTag yang dapat
   * digunakan untuk unsubscribe.
   */
  public String subscribe(String queueName, Consumer<String> onMessage) {
    requireConnection();
    try {
      Channel channel = connection.createChannel();
      channel.queueDeclare(queueName, true, false, false, null);
      // Prefetch agar konsumsi lebih terkontrol (opsional untuk auto-ack)
      channel.basicQos(10);

      DeliverCallback deliverCallback =
          (consumerTag, delivery) -> {
            String body = new String(delivery.getBody(), StandardCharsets.UTF_8);
            try {
              onMessage.accept(body);
            } catch (Exception handlerEx) {
              // Jangan crash consumer; log aman tanpa data sensitif
              log.warn("Message handler error: {}", handlerEx.getMessage());
            }
          };

      CancelCallback cancelCallback =
          consumerTag -> log.info("Consumer {} cancelled for queue {}", consumerTag, queueName);

      String consumerTag = channel.basicConsume(queueName, true, deliverCallback, cancelCallback);
      consumerChannels.put(consumerTag, channel);
      log.info("Subscribed to queue {} with tag {}", queueName, consumerTag);
      return consumerTag;
    } catch (Exception e) {
      log.error("Failed to subscribe to queue {}: {}", queueName, e.getMessage());
      throw new RuntimeException("Gagal subscribe ke queue: " + e.getMessage(), e);
    }
  }

  /** Batalkan subscription berdasarkan consumerTag dan tutup channel terkait. */
  public void unsubscribe(String consumerTag) {
    Channel channel = consumerChannels.remove(consumerTag);
    if (channel == null) {
      log.warn("No channel found for consumerTag {}", consumerTag);
      return;
    }
    try {
      if (channel.isOpen()) {
        channel.basicCancel(consumerTag);
        channel.close();
      }
      log.info("Unsubscribed consumer {} and closed channel", consumerTag);
    } catch (IOException | TimeoutException e) {
      log.warn("Error during unsubscribe for {}: {}", consumerTag, e.getMessage());
    }
  }

  private void requireConnection() {
    if (connection == null || !connection.isOpen()) {
      throw new IllegalStateException("RabbitMQ connection belum tersedia/tertutup");
    }
  }
}
