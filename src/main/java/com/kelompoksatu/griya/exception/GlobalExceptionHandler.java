package com.kelompoksatu.griya.exception;

import jakarta.validation.ValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Data already exists";

        if (ex.getMessage().contains("users_username_key")) {
            message = "Username sudah digunakan";
        } else if (ex.getMessage().contains("users.email")) {
            message = "Email sudah digunakan";
        } else if (ex.getMessage().contains("users.phone")) {
            message = "Nomor telepon sudah digunakan";
        } else if (ex.getMessage().contains("user_profiles.nik")) {
            message = "NIK sudah terdaftar";
        } else if (ex.getMessage().contains("user_profiles.npwp")) {
            message = "NPWP sudah terdaftar";
        }

        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(ValidationException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Unexpected error: " + ex.getMessage()));
    }
}

