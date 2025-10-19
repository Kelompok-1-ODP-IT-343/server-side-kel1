package com.kelompoksatu.griya.security;

import com.kelompoksatu.griya.service.UserService;
import com.kelompoksatu.griya.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/** JWT Authentication Filter Validates JWT tokens and sets authentication context */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

  @Autowired private JwtUtil jwtUtil;

  @Autowired private UserService userService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      // Extract JWT token from request
      String jwt = getJwtFromRequest(request);

      if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
        // Extract user information from token
        String username = jwtUtil.extractUsername(jwt);
        Integer userId = jwtUtil.extractUserId(jwt);
        String role = jwtUtil.extractUserRole(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
          // Verify user still exists and is active
          userService
              .findByUsername(username)
              .ifPresent(
                  user -> {
                    if (userService.isAccountActive(user)) {
                      // Create authorities based on user role
                      List<SimpleGrantedAuthority> authorities =
                          Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));

                      // Create authentication token
                      UsernamePasswordAuthenticationToken authentication =
                          new UsernamePasswordAuthenticationToken(username, null, authorities);

                      // Set additional details
                      authentication.setDetails(
                          new WebAuthenticationDetailsSource().buildDetails(request));

                      // Set authentication in security context
                      SecurityContextHolder.getContext().setAuthentication(authentication);

                      // Add user ID to request attributes for easy access in controllers
                      request.setAttribute("userId", userId);
                      request.setAttribute("userRole", role);

                      logger.debug("Authentication set for user: {} with role: {}", username, role);
                    } else {
                      logger.warn("User account is not active: {}", username);
                    }
                  });
        }
      }
    } catch (Exception e) {
      logger.error("Cannot set user authentication: {}", e.getMessage());
      // Clear security context on error
      SecurityContextHolder.clearContext();
    }

    // Continue with the filter chain
    filterChain.doFilter(request, response);
  }

  /** Extract JWT token from Authorization header */
  private String getJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");

    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }

    return null;
  }

  /** Skip filter for certain paths */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String path = request.getRequestURI();

    // Skip JWT validation for public endpoints
    return path.equals("/api/v1/auth/register")
        || path.equals("/api/v1/auth/login")
        || path.equals("/actuator/health")
        || path.equals("/error")
        || path.equals("/favicon.ico");
  }
}
