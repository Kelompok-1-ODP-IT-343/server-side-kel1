package com.kelompoksatu.griya;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Main application context test Verifies that the Spring Boot application context loads
 * successfully
 *
 * <p>This test uses a minimal configuration to avoid database connectivity issues during CI/CD or
 * when database is not available
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "spring.flyway.enabled=false",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
      "spring.datasource.driverClassName=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
    })
class GriyaApplicationTests {

  @Test
  @DisplayName("Should load application context successfully")
  void contextLoads() {
    // This test verifies that the Spring Boot application context loads without errors
    // It ensures all beans are properly configured and dependencies are resolved
    //
    // If this test passes, it means:
    // - All @Component, @Service, @Repository, @Controller beans are properly configured
    // - Dependency injection is working correctly
    // - Configuration properties are loaded successfully
    // - No circular dependencies exist
  }
}
