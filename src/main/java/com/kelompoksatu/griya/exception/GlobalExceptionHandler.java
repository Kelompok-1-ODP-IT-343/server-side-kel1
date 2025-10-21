package com.kelompoksatu.griya.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
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
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
    return apiError(HttpStatus.BAD_REQUEST, "Validation failed", req, fieldErrors);
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
    return apiError(HttpStatus.BAD_REQUEST, "Validation failed", req, fieldErrors);
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
    return apiError(HttpStatus.BAD_REQUEST, "Validation failed", req, violations);
  }

  // Body kosong / JSON invalid
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleUnreadable(
      HttpMessageNotReadableException ex, WebRequest req) {
    return apiError(HttpStatus.BAD_REQUEST, "Malformed JSON request", req, null);
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

    // 409 CONFLICT untuk duplicate
    return apiError(HttpStatus.CONFLICT, message, req, null);
  }

  /* ========== SERVICE THROWS STATUS ========== */

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleResponseStatus(
      ResponseStatusException ex, WebRequest req) {
    HttpStatus status = (HttpStatus) ex.getStatusCode();
    String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
    return apiError(status, message, req, null);
  }

  /* ========== VALIDATION EXCEPTION LEGACY ========== */

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(
      ValidationException ex, WebRequest req) {
    return apiError(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null);
  }

  /* ========== FALLBACK ========== */

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, WebRequest req) {
    return apiError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", req, null);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<Map<String, Object>> handleAuthenticationException(
      AuthenticationException ex, WebRequest req) {
    return apiError(HttpStatus.UNAUTHORIZED, ex.getMessage(), req, null);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(
      IllegalArgumentException ex, WebRequest req) {
    return apiError(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalState(
      IllegalStateException ex, WebRequest req) {
    return apiError(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null);
  }
}
