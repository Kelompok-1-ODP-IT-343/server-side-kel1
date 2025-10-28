package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.*;
import com.kelompoksatu.griya.service.ApprovalWorkflowService;
import com.kelompoksatu.griya.util.JwtUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Approval Management", description = "Administrative operations for approval workflows")
@RestController
@RequestMapping("/api/v1/approval")
@Validated
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class ApprovalController {
  private final ApprovalWorkflowService approvalWorkflowService;
  private final JwtUtil jwtUtil;

  @PostMapping("/developer")
  public ResponseEntity<ApiResponse<Boolean>> approveOrRejectWorkflowDeveloper(
      @Valid @RequestBody ApprovalConfirmation request,
      @RequestHeader("Authorization") String authHeader) {
    try {
      log.info("Processing workflow approval/rejection for developer");
      String token = authHeader.replace("Bearer ", "");
      Integer userId = jwtUtil.extractUserId(token);
      approvalWorkflowService.approveOrRejectWorkflowDeveloper(request, userId);
      String action = request.getIsApproved() ? "APPROVED" : "REJECTED";
      String message = "Workflow berhasil " + action;
      ApiResponse<Boolean> response = ApiResponse.success(message);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }

  @PostMapping("/verifikator")
  public ResponseEntity<ApiResponse<Boolean>> approveOrRejectWorkflowVerifikator(
      @Valid @RequestBody ApprovalConfirmation request,
      @RequestHeader("Authorization") String authHeader) {
    try {
      log.info("Processing workflow approval/rejection for verifikator");
      String token = authHeader.replace("Bearer ", "");
      Integer userId = jwtUtil.extractUserId(token);
      approvalWorkflowService.approveOrRejectWorkflowVerifikator(request, userId);
      String action = request.getIsApproved() ? "APPROVED" : "REJECTED";
      String message = "Workflow berhasil " + action;
      ApiResponse<Boolean> response = ApiResponse.success(message);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }
  }
}
