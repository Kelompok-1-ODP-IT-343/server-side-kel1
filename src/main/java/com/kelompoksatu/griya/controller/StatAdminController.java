package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.AdminStatsResponse;
import com.kelompoksatu.griya.dto.ApiResponse;
import com.kelompoksatu.griya.service.StatAdminService;
import com.kelompoksatu.griya.util.JwtUtil;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stat-admin")
@RequiredArgsConstructor
@Validated
public class StatAdminController {

  private final StatAdminService service;
  private final JwtUtil jwtUtil;

  @GetMapping("/dashboard")
  public ResponseEntity<ApiResponse<AdminStatsResponse>> dashboard(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
      @RequestParam(name = "range", required = false) String range) {
    try {
      String token = jwtUtil.extractTokenFromHeader(authHeader);
      String role = jwtUtil.extractUserRole(token);
      if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error("Akses ditolak: bukan ADMIN"));
      }

      AdminStatsResponse resp = service.getDashboard(range);
      return ResponseEntity.ok(ApiResponse.success("Statistics fetched", resp));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
    }
  }
}
