package com.kelompoksatu.griya.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kelompoksatu.griya.dto.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * JWT Authentication Entry Point Handles unauthorized access attempts and returns proper JSON error
 * response
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

  @Autowired private ObjectMapper objectMapper;

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {

    logger.error(
        "Unauthorized access attempt: {} - {}",
        request.getRequestURI(),
        authException.getMessage());

    // Set response status and content type
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    // Create error response
    ApiResponse<Object> errorResponse =
        ApiResponse.error(
            "Akses tidak diizinkan. Silakan login terlebih dahulu.", request.getRequestURI());

    // Write JSON response using Spring-configured ObjectMapper (with JavaTimeModule)
    String jsonResponse = objectMapper.writeValueAsString(errorResponse);

    response.getWriter().write(jsonResponse);
    response.getWriter().flush();
  }
}
