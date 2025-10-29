package com.kelompoksatu.griya.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration class for Swagger/OpenAPI documentation */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Griya Property Management API")
                .description(
                    "A comprehensive REST API for property management, KPR applications, and developer management")
                .version("1.0.0")
                .contact(
                    new Contact()
                        .name("Kelompok Satu Development Team")
                        .email("dev@kelompoksatu.com")
                        .url("https://www.kelompoksatu.com"))
                .license(
                    new License().name("MIT License").url("https://opensource.org/licenses/MIT")))
        .servers(
            List.of(
                new Server().url("http://localhost:18080").description("Development Server"),
                new Server()
                    .url("https://api.griya.kelompoksatu.com")
                    .description("Production Server")))
        .tags(
            List.of(
                new Tag()
                    .name("Authentication")
                    .description("User authentication and registration operations"),
                new Tag()
                    .name("Developer Management")
                    .description("Developer profile and management operations"),
                new Tag()
                    .name("Property Management")
                    .description("Property listing and management operations"),
                new Tag()
                    .name("KPR Applications")
                    .description("KPR application submission and processing operations")));
  }
}
