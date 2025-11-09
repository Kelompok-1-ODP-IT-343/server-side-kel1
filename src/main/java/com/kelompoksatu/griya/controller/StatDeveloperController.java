package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.ApiResponse;
import com.kelompoksatu.griya.dto.DeveloperStatsResponse;
import com.kelompoksatu.griya.entity.User;
import com.kelompoksatu.griya.repository.UserRepository;
import com.kelompoksatu.griya.service.StatDeveloperService;
import com.kelompoksatu.griya.util.JwtUtil;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Developer Statistics", description = "Dashboard statistics for Developer")
@RestController
@RequestMapping("/api/v1/stat-developer")
@Validated
@RequiredArgsConstructor
public class StatDeveloperController {

  private final StatDeveloperService statDeveloperService;
  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;

  @GetMapping("/dashboard")
  public ResponseEntity<ApiResponse<DeveloperStatsResponse>> getDashboard(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
      HttpServletRequest request,
      @RequestParam(name = "range", required = false) String range) {
    String token = jwtUtil.extractTokenFromHeader(authHeader);
    Integer userId = jwtUtil.extractUserId(token);
    Optional<User> userOpt = userRepository.findById(userId);
    if (userOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Unauthorized"));
    }
    User user = userOpt.get();
    if (user.getDeveloper() == null || user.getDeveloper().getId() == null) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error("User is not associated with a developer"));
    }
    Integer developerId = user.getDeveloper().getId();
    DeveloperStatsResponse stats = statDeveloperService.getDashboard(developerId, range);
    return ResponseEntity.ok(ApiResponse.success("Statistics fetched", stats));
  }
}
