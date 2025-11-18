package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.ApiResponse;
import com.kelompoksatu.griya.dto.StaffStatsResponse;
import com.kelompoksatu.griya.service.StatStaffService;
import com.kelompoksatu.griya.util.JwtUtil;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/v1/stat-staff")
@RequiredArgsConstructor
@Validated
public class StatStaffController {

  private final StatStaffService service;

  private final JwtUtil jwtUtil;

  @GetMapping("/dashboard")
  public ResponseEntity<ApiResponse<StaffStatsResponse>> dashboard(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
      @RequestParam(name = "range", required = false) String range,
      HttpServletRequest httpRequest) {

    String token = jwtUtil.extractTokenFromHeader(authHeader);
    String role = jwtUtil.extractUserRole(token);
    if (role == null || !"STAFF".equalsIgnoreCase(role)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error("Akses ditolak: bukan STAFF", httpRequest.getRequestURI()));
    }

    Integer staffId = jwtUtil.extractUserId(token);
    StaffStatsResponse resp = service.getDashboard(staffId, range);
    return ResponseEntity.ok(
        ApiResponse.success(resp, "Statistics fetched", httpRequest.getRequestURI()));
  }
}
