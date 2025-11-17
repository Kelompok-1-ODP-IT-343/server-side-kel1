package com.kelompoksatu.griya.exception;

import com.kelompoksatu.griya.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.webjars.NotFoundException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final NotificationService notificationService;

  private ResponseEntity<Map<String, Object>> apiError(
      HttpStatus status, String message, WebRequest req, Object data) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("success", false);
    body.put("message", message);
    body.put("data", data);
    body.put("timestamp", OffsetDateTime.now());
    body.put("path", req != null ? req.getDescription(false).replace("uri=", "") : null);
    return ResponseEntity.status(status).body(body);
  }

  /**
   * Send Telegram notification for errors that should be monitored Only sends notifications for
   * server errors (5xx) and critical client errors
   */
  private void sendErrorNotification(
      HttpStatus status, String message, WebRequest req, Exception ex) {
    try {
      // Only send notifications for server errors and critical client errors
      if (status.is5xxServerError()
          || status == HttpStatus.UNAUTHORIZED
          || status == HttpStatus.FORBIDDEN) {
        String endpoint = req != null ? req.getDescription(false).replace("uri=", "") : "Unknown";
        String httpMethod = "Unknown";
        String userAgent = null;
        String ipAddress = null;

        // Extract HTTP method and additional info if available
        if (req instanceof org.springframework.web.context.request.ServletWebRequest) {
          HttpServletRequest httpRequest =
              ((org.springframework.web.context.request.ServletWebRequest) req).getRequest();
          httpMethod = httpRequest.getMethod();
          userAgent = httpRequest.getHeader("User-Agent");
          ipAddress = getClientIpAddress(httpRequest);
        }

        notificationService.sendEndpointErrorNotification(
            endpoint,
            httpMethod,
            status.value(),
            message + (ex != null ? " - " + ex.getClass().getSimpleName() : ""),
            userAgent,
            ipAddress);
      }
    } catch (Exception e) {
      // Don't let notification errors break the main error handling
      log.warn("Failed to send error notification: {}", e.getMessage());
    }
  }

  /** Extract client IP address from request */
  private String getClientIpAddress(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
      return xForwardedFor.split(",")[0].trim();
    }

    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty()) {
      return xRealIp;
    }

    return request.getRemoteAddr();
  }

  /* ========== VALIDATION ========== */

  // @Valid @RequestBody
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, WebRequest req) {
    Map<String, String> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    FieldError::getDefaultMessage,
                    (a, b) -> a,
                    LinkedHashMap::new));
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String message = "Validation failed";

    // Send notification for validation errors (optional - can be disabled for less critical errors)
    // sendErrorNotification(status, message, req, ex);

    return apiError(status, message, req, fieldErrors);
  }

  // @ModelAttribute / form-data
  @ExceptionHandler(BindException.class)
  public ResponseEntity<Map<String, Object>> handleBindException(BindException ex, WebRequest req) {
    Map<String, String> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(
                    FieldError::getField,
                    FieldError::getDefaultMessage,
                    (a, b) -> a,
                    LinkedHashMap::new));
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String message = "Validation failed";

    // Send notification for bind errors (optional - can be disabled for less critical errors)
    // sendErrorNotification(status, message, req, ex);

    return apiError(status, message, req, fieldErrors);
  }

  // @RequestParam / @PathVariable type mismatch
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Map<String, Object>> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, WebRequest req) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String message = "Invalid parameter: " + ex.getName() + " with value '" + ex.getValue() + "'";

    Map<String, Object> data = new LinkedHashMap<>();
    data.put("parameter", ex.getName());
    data.put("value", ex.getValue());
    data.put(
        "requiredType", ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : null);

    // Treat as client error, but still notify for visibility if needed
    // sendErrorNotification(status, message, req, ex);

    return apiError(status, message, req, data);
  }

  // @RequestParam / @PathVariable (aktifkan @Validated di controller/class)
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, Object>> handleConstraintViolation(
      ConstraintViolationException ex, WebRequest req) {
    Map<String, String> violations =
        ex.getConstraintViolations().stream()
            .collect(
                Collectors.toMap(
                    v -> v.getPropertyPath().toString(),
                    v -> v.getMessage(),
                    (a, b) -> a,
                    LinkedHashMap::new));
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String message = "Validation failed";

    // Send notification for constraint violations (optional - can be disabled for less critical
    // errors)
    // sendErrorNotification(status, message, req, ex);

    return apiError(status, message, req, violations);
  }

  // Body kosong / JSON invalid
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleUnreadable(
      HttpMessageNotReadableException ex, WebRequest req) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String message = "Malformed JSON request";

    // Send notification for malformed JSON (could indicate attack attempts)
    sendErrorNotification(status, message, req, ex);

    return apiError(status, message, req, null);
  }

  /* ========== DUPLICATE / DB CONSTRAINT ========== */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(
      DataIntegrityViolationException ex, WebRequest req) {
    String message = "Data sudah ada";

    // Fallback dari message
    String errorMessage = ex.getMessage() != null ? ex.getMessage() : "";

    if (errorMessage.contains("users_username_key")) {
      message = "Username sudah digunakan";
    } else if (errorMessage.contains("users_email_key")) {
      message = "Email sudah digunakan";
    } else if (errorMessage.contains("users_phone_key")) {
      message = "Nomor telepon sudah digunakan";
    } else if (errorMessage.contains("user_profiles_nik_key")) {
      message = "NIK sudah terdaftar";
    } else if (errorMessage.contains("user_profiles_npwp_key")) {
      message = "NPWP sudah terdaftar";
    }

    HttpStatus status = HttpStatus.CONFLICT;

    // Send notification for data integrity violations (could indicate data issues)
    sendErrorNotification(status, message, req, ex);

    // 409 CONFLICT untuk duplicate
    return apiError(status, message, req, null);
  }

  /* ========== SERVICE THROWS STATUS ========== */

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleResponseStatus(
      ResponseStatusException ex, WebRequest req) {
    HttpStatus status = (HttpStatus) ex.getStatusCode();
    String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();

    // Send notification for response status exceptions
    sendErrorNotification(status, message, req, ex);

    return apiError(status, message, req, null);
  }

  /* ========== VALIDATION EXCEPTION LEGACY ========== */

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(
      ValidationException ex, WebRequest req) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String message = ex.getMessage();

    // Send notification for validation exceptions (optional - can be disabled for less critical
    // errors)
    // sendErrorNotification(status, message, req, ex);

    return apiError(status, message, req, null);
  }

  /* ========== FALLBACK ========== */

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, WebRequest req) {
    // log.info("Unhandled exception: {}", ex.getMessage(), ex);
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    String message = "Internal server error";

    // Always send notification for unhandled exceptions - these are critical
    sendErrorNotification(status, message, req, ex);

    return apiError(status, message, req, null);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Map<String, Object>> handleAuthenticationException(
      AuthenticationException ex, WebRequest req) {
    HttpStatus status = HttpStatus.UNAUTHORIZED;
    String message = ex.getMessage();

    // Send notification for authentication failures (security concern)
    sendErrorNotification(status, message, req, ex);

    return apiError(status, message, req, null);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(
      IllegalArgumentException ex, WebRequest req) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String message = ex.getMessage();

    // Send notification for illegal arguments (could indicate programming errors)
    sendErrorNotification(status, message, req, ex);

    return apiError(status, message, req, null);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalState(
      IllegalStateException ex, WebRequest req) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    String message = ex.getMessage();

    // Send notification for illegal state (could indicate programming errors)
    sendErrorNotification(status, message, req, ex);

    return apiError(status, message, req, null);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex, WebRequest req) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    String message = ex.getMessage();

    sendErrorNotification(status, message, req, ex);

    return apiError(status, message, req, null);
  }
}
