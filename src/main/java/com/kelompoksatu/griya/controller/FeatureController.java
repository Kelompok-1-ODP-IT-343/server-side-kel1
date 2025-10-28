package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Spring Boot Feature Information Provides information about available Spring
 * Boot features and configurations
 */
@Tag(
    name = "System Features",
    description = "Spring Boot feature information and system capabilities")
@RestController
@RequestMapping("/api/v1/features")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class FeatureController {

  private final Environment environment;

  /** Get list of available Spring Boot features GET /api/v1/features */
  @Operation(
      summary = "Get Spring Boot Features",
      description =
          "Retrieve information about available Spring Boot features, active profiles, and system capabilities")
  @ApiResponses(
      value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Features retrieved successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiResponse.class)))
      })
  @GetMapping
  public ResponseEntity<ApiResponse<Map<String, Object>>> getSpringBootFeatures() {
    try {
      log.info("Retrieving Spring Boot features information");

      Map<String, Object> features = new HashMap<>();

      // Active Profiles
      String[] activeProfiles = environment.getActiveProfiles();
      features.put(
          "activeProfiles",
          activeProfiles.length > 0 ? Arrays.asList(activeProfiles) : Arrays.asList("default"));

      // Spring Boot Features
      Map<String, Object> springBootFeatures = new HashMap<>();

      // Web Features
      springBootFeatures.put("webMvc", isFeatureEnabled("spring.mvc"));
      springBootFeatures.put("restApi", true); // Always true since we have REST controllers

      // Data Features
      springBootFeatures.put("jpa", isFeatureEnabled("spring.jpa"));
      springBootFeatures.put("flyway", isFeatureEnabled("spring.flyway"));
      springBootFeatures.put(
          "postgresql",
          isPropertySet("spring.datasource.url")
              && environment.getProperty("spring.datasource.url", "").contains("postgresql"));

      // Security Features
      springBootFeatures.put("security", isFeatureEnabled("spring.security"));
      springBootFeatures.put("jwt", isPropertySet("jwt.secret"));
      springBootFeatures.put("cors", isPropertySet("security.cors.allowed-origins"));

      // Actuator Features
      springBootFeatures.put("actuator", isFeatureEnabled("management.endpoints"));
      springBootFeatures.put("healthCheck", isPropertySet("management.endpoint.health"));
      springBootFeatures.put(
          "livenessProbe",
          "true".equals(environment.getProperty("management.health.livenessstate.enabled")));
      springBootFeatures.put(
          "readinessProbe",
          "true".equals(environment.getProperty("management.health.readinessstate.enabled")));

      // Documentation Features
      springBootFeatures.put("swagger", isFeatureEnabled("springdoc"));
      springBootFeatures.put("openApi", isPropertySet("springdoc.api-docs.path"));

      // File Upload Features
      springBootFeatures.put(
          "multipart",
          "true".equals(environment.getProperty("spring.servlet.multipart.enabled", "true")));
      springBootFeatures.put("fileUpload", isPropertySet("app.file.upload-dir"));

      // Email Features
      springBootFeatures.put("email", isFeatureEnabled("spring.mail"));

      // AWS Features
      springBootFeatures.put(
          "s3Integration", isClassPresent("software.amazon.awssdk.services.s3.S3Client"));

      features.put("springBootFeatures", springBootFeatures);

      // Application Configuration
      Map<String, Object> appConfig = new HashMap<>();
      appConfig.put("applicationName", environment.getProperty("spring.application.name", "griya"));
      appConfig.put("serverPort", environment.getProperty("server.port", "18080"));
      appConfig.put(
          "baseUrl", environment.getProperty("app.server.baseUrl", "http://localhost:18080"));
      appConfig.put(
          "maxFileSize", environment.getProperty("spring.servlet.multipart.max-file-size", "10MB"));
      appConfig.put(
          "maxRequestSize",
          environment.getProperty("spring.servlet.multipart.max-request-size", "50MB"));

      appConfig.put("lastUpdate", "Reset password for developer");

      features.put("applicationConfiguration", appConfig);

      // Available Endpoints
      List<String> availableEndpoints =
          Arrays.asList(
              "/api/v1/auth/**",
              "/api/v1/user/**",
              "/api/v1/admin/**",
              "/api/v1/developers/**",
              "/api/v1/properties/**",
              "/api/v1/kpr-applications/**",
              "/api/v1/features/**",
              "/actuator/health",
              "/actuator/info",
              "/swagger-ui.html",
              "/v3/api-docs");
      features.put("availableEndpoints", availableEndpoints);

      // System Information
      Map<String, Object> systemInfo = new HashMap<>();
      systemInfo.put("javaVersion", System.getProperty("java.version"));
      systemInfo.put("springBootVersion", getSpringBootVersion());
      systemInfo.put("operatingSystem", System.getProperty("os.name"));
      systemInfo.put("architecture", System.getProperty("os.arch"));

      features.put("systemInformation", systemInfo);

      ApiResponse<Map<String, Object>> response =
          ApiResponse.success("Spring Boot features retrieved successfully", features);

      log.info("Successfully retrieved Spring Boot features information");
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error retrieving Spring Boot features: {}", e.getMessage(), e);
      ApiResponse<Map<String, Object>> response =
          ApiResponse.error("Failed to retrieve Spring Boot features: " + e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /** Get detailed actuator endpoints information GET /api/v1/features/actuator */
  @Operation(
      summary = "Get Actuator Endpoints",
      description = "Retrieve information about available Spring Boot Actuator endpoints")
  @GetMapping("/actuator")
  public ResponseEntity<ApiResponse<Map<String, Object>>> getActuatorInfo() {
    try {
      log.info("Retrieving actuator endpoints information");

      Map<String, Object> actuatorInfo = new HashMap<>();

      // Exposed endpoints
      String exposedEndpoints =
          environment.getProperty("management.endpoints.web.exposure.include", "health,info");
      actuatorInfo.put("exposedEndpoints", Arrays.asList(exposedEndpoints.split(",")));

      // Health endpoint configuration
      Map<String, Object> healthConfig = new HashMap<>();
      healthConfig.put("enabled", isPropertySet("management.endpoint.health"));
      healthConfig.put(
          "showDetails",
          environment.getProperty("management.endpoint.health.show-details", "never"));
      healthConfig.put(
          "probesEnabled",
          "true".equals(environment.getProperty("management.endpoint.health.probes.enabled")));
      healthConfig.put(
          "livenessEnabled",
          "true".equals(environment.getProperty("management.health.livenessstate.enabled")));
      healthConfig.put(
          "readinessEnabled",
          "true".equals(environment.getProperty("management.health.readinessstate.enabled")));

      actuatorInfo.put("healthConfiguration", healthConfig);

      // Available actuator URLs
      String baseUrl = environment.getProperty("app.server.baseUrl", "http://localhost:18080");
      List<String> actuatorUrls =
          Arrays.asList(
              baseUrl + "/actuator",
              baseUrl + "/actuator/health",
              baseUrl + "/actuator/health/liveness",
              baseUrl + "/actuator/health/readiness",
              baseUrl + "/actuator/info");
      actuatorInfo.put("availableUrls", actuatorUrls);

      ApiResponse<Map<String, Object>> response =
          ApiResponse.success("Actuator information retrieved successfully", actuatorInfo);

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error retrieving actuator information: {}", e.getMessage(), e);
      ApiResponse<Map<String, Object>> response =
          ApiResponse.error("Failed to retrieve actuator information: " + e.getMessage());
      return ResponseEntity.internalServerError().body(response);
    }
  }

  // Helper methods
  private boolean isFeatureEnabled(String propertyPrefix) {
    return environment.getProperty(propertyPrefix) != null;
  }

  private boolean isPropertySet(String propertyName) {
    return environment.getProperty(propertyName) != null;
  }

  private boolean isClassPresent(String className) {
    try {
      Class.forName(className);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private String getSpringBootVersion() {
    try {
      return org.springframework.boot.SpringBootVersion.getVersion();
    } catch (Exception e) {
      return "Unknown";
    }
  }
}
