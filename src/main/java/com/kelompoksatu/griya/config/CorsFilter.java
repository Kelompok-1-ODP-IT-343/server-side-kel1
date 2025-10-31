package com.kelompoksatu.griya.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Global CORS Filter Ensures all responses include proper CORS headers This acts as a fallback in
 * case Spring Security CORS configuration doesn't apply
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CorsFilter implements Filter {

  @Value("${security.cors.allowed-origins}")
  private String allowedOrigins;

  @Value("${security.cors.allowed-methods}")
  private String allowedMethods;

  @Value("${security.cors.allowed-headers}")
  private String allowedHeaders;

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    String origin = request.getHeader("Origin");

    // Parse allowed origins
    List<String> origins = Arrays.asList(allowedOrigins.split(","));
    origins =
        origins.stream()
            .map(String::trim)
            .filter(o -> !o.isEmpty())
            .collect(java.util.stream.Collectors.toList());

    // Set CORS headers
    boolean allowCredentials = false;
    if (origin != null && origins.contains(origin)) {
      response.setHeader("Access-Control-Allow-Origin", origin);
      allowCredentials = true;
    } else if (origins.isEmpty() || allowedOrigins.trim().equals("*")) {
      // For development - allow any origin but no credentials with wildcard
      response.setHeader("Access-Control-Allow-Origin", "*");
      allowCredentials = false;
    } else {
      // Default fallback for development
      response.setHeader("Access-Control-Allow-Origin", "*");
      allowCredentials = false;
    }

    response.setHeader("Access-Control-Allow-Credentials", String.valueOf(allowCredentials));
    response.setHeader("Access-Control-Allow-Methods", allowedMethods);
    response.setHeader(
        "Access-Control-Allow-Headers",
        "Authorization, Content-Type, X-Requested-With, Accept, Origin, "
            + "Access-Control-Request-Method, Access-Control-Request-Headers, X-Custom-Header");
    response.setHeader(
        "Access-Control-Expose-Headers",
        "Access-Control-Allow-Origin, Access-Control-Allow-Credentials, Authorization, "
            + "Content-Type, X-Total-Count, X-Page-Number, X-Page-Size");
    response.setHeader("Access-Control-Max-Age", "3600");

    // Handle preflight requests
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      log.debug("Handling CORS preflight request for: {}", request.getRequestURI());
      response.setStatus(HttpServletResponse.SC_OK);
      return;
    }

    chain.doFilter(req, res);
  }

  @Override
  public void init(FilterConfig filterConfig) {
    log.info("CORS Filter initialized with allowed origins: {}", allowedOrigins);
  }

  @Override
  public void destroy() {
    // Cleanup if needed
  }
}
