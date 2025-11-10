package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.StaffStatsResponse;
import com.kelompoksatu.griya.service.StatStaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stat-staff")
public class StatStaffController {

  private final StatStaffService service;

  public StatStaffController(StatStaffService service) {
    this.service = service;
  }

  @GetMapping("/dashboard")
  public ResponseEntity<StaffStatsResponse> dashboard(
      Authentication auth, @RequestParam(value = "range", required = false) String range) {
    // Assuming principal name or a JWT-derived ID corresponds to staff user ID
    Integer staffId = extractUserId(auth);
    StaffStatsResponse resp = service.getDashboard(staffId, range);
    return ResponseEntity.ok(resp);
  }

  private Integer extractUserId(Authentication auth) {
    // This should be adapted to the actual security model. For now, try parsing principal name.
    try {
      return Integer.valueOf(auth.getName());
    } catch (Exception e) {
      // Fallback: in real code, map from a custom UserDetails or JWT claims
      return 0;
    }
  }
}
