package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** CORS Test Controller Provides endpoints to test CORS configuration */
@Tag(name = "CORS Test", description = "CORS configuration testing endpoints")
@RestController
@RequestMapping("/api/v1/cors-test")
@Slf4j
public class CorsTestController {

  /** Simple GET endpoint to test CORS */
  @GetMapping("/simple")
  @Operation(summary = "Simple CORS test", description = "Test CORS with a simple GET request")
  public ResponseEntity<ApiResponse<Map<String, Object>>> simpleTest() {
    log.info("CORS simple test endpoint called");

    Map<String, Object> data = new HashMap<>();
    data.put("message", "CORS is working correctly!");
    data.put("method", "GET");
    data.put("timestamp", System.currentTimeMillis());

    return ResponseEntity.ok(ApiResponse.success("CORS test successful", data));
  }

  /** POST endpoint to test CORS with preflight */
  @PostMapping("/preflight")
  @Operation(
      summary = "CORS preflight test",
      description = "Test CORS with a POST request that triggers preflight")
  public ResponseEntity<ApiResponse<Map<String, Object>>> preflightTest(
      @RequestBody(required = false) Map<String, Object> requestData) {
    log.info("CORS preflight test endpoint called with data: {}", requestData);

    Map<String, Object> data = new HashMap<>();
    data.put("message", "CORS preflight is working correctly!");
    data.put("method", "POST");
    data.put("receivedData", requestData);
    data.put("timestamp", System.currentTimeMillis());

    return ResponseEntity.ok(ApiResponse.success("CORS preflight test successful", data));
  }

  /** Test endpoint with custom headers */
  @PostMapping("/custom-headers")
  @Operation(summary = "CORS custom headers test", description = "Test CORS with custom headers")
  public ResponseEntity<ApiResponse<Map<String, Object>>> customHeadersTest(
      @RequestHeader(value = "X-Custom-Header", required = false) String customHeader,
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @RequestBody(required = false) Map<String, Object> requestData) {

    log.info("CORS custom headers test called with custom header: {}", customHeader);

    Map<String, Object> data = new HashMap<>();
    data.put("message", "CORS with custom headers is working!");
    data.put("method", "POST");
    data.put("customHeader", customHeader);
    data.put("hasAuthorization", authorization != null);
    data.put("receivedData", requestData);
    data.put("timestamp", System.currentTimeMillis());

    return ResponseEntity.ok(ApiResponse.success("CORS custom headers test successful", data));
  }
}
