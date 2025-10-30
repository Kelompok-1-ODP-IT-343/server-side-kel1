package com.kelompoksatu.griya.config;

import com.kelompoksatu.griya.security.JwtAuthenticationEntryPoint;
import com.kelompoksatu.griya.security.JwtAuthenticationFilter;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security Configuration Implements JWT-based authentication with proper security headers
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

  @Autowired private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @Autowired private JwtAuthenticationFilter jwtAuthenticationFilter;

  // Inject CORS properties from application.properties
  @Value("${security.cors.allowed-origins}")
  private String allowedOrigins;

  @Value("${security.cors.allowed-methods}")
  private String allowedMethods;

  @Value("${security.cors.allowed-headers}")
  private String allowedHeaders;

  /** Password encoder bean using BCrypt */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // Strength 12 for better security
  }

  /** Authentication manager bean */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  /** Enhanced CORS configuration using application properties */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Parse and set allowed origins from properties
    List<String> origins = Arrays.asList(allowedOrigins.split(","));

    // Clean up origins (trim whitespace)
    origins =
        origins.stream()
            .map(String::trim)
            .filter(origin -> !origin.isEmpty())
            .collect(java.util.stream.Collectors.toList());

    if (origins.isEmpty() || allowedOrigins.trim().equals("*")) {
      // For development - allow all origins with patterns
      configuration.setAllowedOriginPatterns(Arrays.asList("*"));
      configuration.setAllowCredentials(false); // Cannot use credentials with wildcard
    } else {
      configuration.setAllowedOrigins(origins);
      configuration.setAllowCredentials(true); // Allow credentials with specific origins
    }

    // Parse and set allowed methods from properties
    List<String> methods = Arrays.asList(allowedMethods.split(","));
    methods =
        methods.stream()
            .map(String::trim)
            .filter(method -> !method.isEmpty())
            .collect(java.util.stream.Collectors.toList());
    configuration.setAllowedMethods(methods);

    // Set allowed headers - support both wildcard and specific headers
    if ("*".equals(allowedHeaders.trim())) {
      configuration.setAllowedHeaders(Arrays.asList("*"));
    } else {
      List<String> headers = Arrays.asList(allowedHeaders.split(","));
      headers =
          headers.stream()
              .map(String::trim)
              .filter(header -> !header.isEmpty())
              .collect(java.util.stream.Collectors.toList());
      configuration.setAllowedHeaders(headers);
    }

    // Add essential headers for CORS and authentication
    configuration.addAllowedHeader("Authorization");
    configuration.addAllowedHeader("Content-Type");
    configuration.addAllowedHeader("X-Requested-With");
    configuration.addAllowedHeader("Accept");
    configuration.addAllowedHeader("Origin");
    configuration.addAllowedHeader("Access-Control-Request-Method");
    configuration.addAllowedHeader("Access-Control-Request-Headers");
    configuration.addAllowedHeader("X-Custom-Header");

    // Expose headers to frontend
    configuration.setExposedHeaders(
        Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Authorization",
            "Content-Type",
            "X-Total-Count",
            "X-Page-Number",
            "X-Page-Size"));

    // Cache preflight response for 1 hour
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);

    return source;
  }

  /** Security filter chain configuration */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // Disable CSRF for stateless JWT authentication
        .csrf(AbstractHttpConfigurer::disable)

        // Configure CORS
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        // Configure session management (stateless)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // Configure authorization rules
        .authorizeHttpRequests(
            authz ->
                authz
                    // Public endpoints (no authentication required)
                    .requestMatchers(
                        "/api/v1/auth/register",
                        "/api/v1/auth/register/developer",
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/forgot-password",
                        "/api/v1/auth/reset-password",
                        "/api/v1/auth/validate",
                        "/api/v1/auth/verify",
                        "/api/v1/auth/verify-otp",
                        "/api/v1/auth/verify-login-otp",
                        "/api/v1/auth/verify-registration-otp",
                        "/api/v1/properties/**",
                        "/api/v1/features/**",
                        "/api/v1/cors-test/**",
                        "/actuator/health",
                        "/actuator/health/liveness",
                        "/actuator/health/readiness",
                        "/error",
                        "/favicon.ico",
                        // Swagger/OpenAPI documentation endpoints
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/swagger-resources/**",
                        "/webjars/**")
                    .permitAll()

                    // Admin endpoints (require ADMIN role)
                    .requestMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")

                    // User endpoints (require authentication)
                    .requestMatchers("/api/v1/user/**")
                    .authenticated()

                    // KPR Application endpoints (require verifikator role)
                    .requestMatchers("/api/v1/kpr-applications/**")
                    .authenticated()

                    // All other endpoints require authentication
                    .anyRequest()
                    .authenticated())

        // Configure exception handling
        .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))

        // Add security headers
        .headers(
            headers -> {
              headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny);
              headers.contentTypeOptions(Customizer.withDefaults());
            });

    // Add JWT authentication filter
    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
