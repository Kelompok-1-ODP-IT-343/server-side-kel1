package com.kelompoksatu.griya.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Component to log important application URLs when the application starts */
@Component
public class ApplicationStartupLogger {

  private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupLogger.class);

  private final Environment environment;

  public ApplicationStartupLogger(Environment environment) {
    this.environment = environment;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void logApplicationUrls() {
    String port = environment.getProperty("server.port", "8080");
    String contextPath = environment.getProperty("server.servlet.context-path", "");

    // Build base URL
    String baseUrl = "http://localhost:" + port + contextPath;

    logger.info("=".repeat(80));
    logger.info("🚀 GRIYA PROPERTY MANAGEMENT API STARTED SUCCESSFULLY");
    logger.info("=".repeat(80));
    logger.info("📚 API Documentation (Swagger UI): {}", baseUrl + "/swagger-ui/index.html");
    logger.info("📋 OpenAPI JSON Schema: {}", baseUrl + "/v3/api-docs");
    logger.info("🏥 Health Check: {}", baseUrl + "/actuator/health");
    logger.info("📊 Application Info: {}", baseUrl + "/actuator/info");
    logger.info("=".repeat(80));
    logger.info("💡 Quick Start:");
    logger.info("   1. Open Swagger UI: {}", baseUrl + "/swagger-ui/index.html");
    logger.info("   2. Try the 'Register Developer' endpoint");
    logger.info("   3. Use the interactive API testing interface");
    logger.info("=".repeat(80));
  }
}
