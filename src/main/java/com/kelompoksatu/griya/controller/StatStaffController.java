package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.StaffStatsResponse;
import com.kelompoksatu.griya.service.StatStaffService;
import com.kelompoksatu.griya.util.JwtUtil;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
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
  public ResponseEntity<StaffStatsResponse> dashboard(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
      @RequestParam("range") String range) {

    String token = jwtUtil.extractTokenFromHeader(authHeader);
    Integer staffId = jwtUtil.extractUserId(token);

    StaffStatsResponse resp = service.getDashboard(staffId, range);
    return ResponseEntity.ok(resp);
  }
}
