package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.dto.ApiResponse;
import com.kelompoksatu.griya.entity.SystemNotification;
import com.kelompoksatu.griya.service.SystemNotificationService;
import com.kelompoksatu.griya.util.JwtUtil;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest; // Import yang umum digunakan di Controller lain
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
public class SystemNotificationController {

  private final SystemNotificationService notificationService;
  private final JwtUtil jwtUtil;

  /** [POST] /api/v1/notifications: Membuat notifikasi baru. */
  @PostMapping
  public ResponseEntity<ApiResponse<SystemNotification>> createNotification(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
      @RequestBody SystemNotification notification,
      HttpServletRequest request) {
    log.info(
        "CONTROLLER: Received POST request to create notification at path: {}",
        request.getRequestURI());

    String token = jwtUtil.extractTokenFromHeader(authHeader);
    Integer userId = jwtUtil.extractUserId(token);
    if (userId == null) {
      ApiResponse<SystemNotification> unauthorized =
          ApiResponse.error("Token tidak valid", request.getRequestURI());
      return new ResponseEntity<>(unauthorized, HttpStatus.UNAUTHORIZED);
    }

    if (notification.getUserId() == null) {
      notification.setUserId(userId);
    }

    SystemNotification savedNotification = notificationService.saveNotification(notification);

    // Menggunakan static method success(String message, T data) dari ApiResponse
    ApiResponse<SystemNotification> response =
        ApiResponse.success("Notification successfully created and scheduled.", savedNotification);
    response.setPath(request.getRequestURI()); // Set path pada response

    return new ResponseEntity<>(response, HttpStatus.CREATED); // Menggunakan HttpStatus 201
  }

  /** [GET] /api/v1/notifications/user/{userId}: Mendapatkan semua notifikasi untuk user. */
  @GetMapping("/user")
  public ResponseEntity<ApiResponse<List<SystemNotification>>> getNotificationsForUser(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
      HttpServletRequest request) {

    String token = jwtUtil.extractTokenFromHeader(authHeader);

    // Extract user ID from token
    Integer userId = jwtUtil.extractUserId(token);
    if (userId == null) {
      // Handle case where user ID is not found in token
      ApiResponse<List<SystemNotification>> response =
          ApiResponse.error("User ID not found in token.", request.getRequestURI());
      return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 UNAUTHORIZED
    }

    List<SystemNotification> notifications = notificationService.getNotificationsByUserId(userId);

    // Menggunakan static method success(String message, T data) dari ApiResponse
    ApiResponse<List<SystemNotification>> response =
        ApiResponse.success("Notifications retrieved successfully.", notifications);
    response.setPath(request.getRequestURI());

    return new ResponseEntity<>(response, HttpStatus.OK); // Menggunakan HttpStatus 200
  }

  /** [PATCH] /api/v1/notifications/{id}/read: Menandai notifikasi sebagai sudah dibaca. */
  @PatchMapping("/{id}/read")
  public ResponseEntity<ApiResponse<SystemNotification>> markNotificationAsRead(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader,
      @PathVariable Long id,
      HttpServletRequest request) {
    log.info(
        "CONTROLLER: Received PATCH request to mark notification ID {} as read at path: {}",
        id,
        request.getRequestURI());

    try {
      SystemNotification updatedNotification = notificationService.markAsRead(id);

      // Menggunakan static method success(String message, T data)
      ApiResponse<SystemNotification> response =
          ApiResponse.success("Notification marked as read.", updatedNotification);
      response.setPath(request.getRequestURI());

      return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (RuntimeException e) {
      // Error handling, menggunakan static method error(String message)
      log.error("CONTROLLER ERROR: Notification ID {} not found or error: {}", id, e.getMessage());

      ApiResponse<SystemNotification> response =
          ApiResponse.error(
              e.getMessage() + " | Please check the ID.",
              request.getRequestURI() // Menggunakan error(String message, String path)
              );

      // Mengembalikan status 404 NOT FOUND untuk kasus RuntimeException (Not Found)
      return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
  }
}
