package com.kelompoksatu.griya.controller;

import com.kelompoksatu.griya.entity.SystemNotification;
import com.kelompoksatu.griya.service.SystemNotificationService;
import com.kelompoksatu.griya.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest; // Import yang umum digunakan di Controller lain

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class SystemNotificationController {

    private final SystemNotificationService notificationService;

    /**
     * [POST] /api/v1/notifications: Membuat notifikasi baru.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SystemNotification>> createNotification(
            @RequestBody SystemNotification notification,
            HttpServletRequest request
    ) {
        log.info("CONTROLLER: Received POST request to create notification at path: {}", request.getRequestURI());

        SystemNotification savedNotification = notificationService.saveNotification(notification);

        // Menggunakan static method success(String message, T data) dari ApiResponse
        ApiResponse<SystemNotification> response = ApiResponse.success(
                "Notification successfully created and scheduled.",
                savedNotification
        );
        response.setPath(request.getRequestURI()); // Set path pada response

        return new ResponseEntity<>(response, HttpStatus.CREATED); // Menggunakan HttpStatus 201
    }

    /**
     * [GET] /api/v1/notifications/user/{userId}: Mendapatkan semua notifikasi untuk user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<SystemNotification>>> getNotificationsForUser(
            @PathVariable Integer userId,
            HttpServletRequest request
    ) {
        log.info("CONTROLLER: Received GET request for user ID: {} at path: {}", userId, request.getRequestURI());

        List<SystemNotification> notifications = notificationService.getNotificationsByUserId(userId);

        // Menggunakan static method success(String message, T data) dari ApiResponse
        ApiResponse<List<SystemNotification>> response = ApiResponse.success(
                "Notifications retrieved successfully.",
                notifications
        );
        response.setPath(request.getRequestURI());

        return new ResponseEntity<>(response, HttpStatus.OK); // Menggunakan HttpStatus 200
    }

    /**
     * [PATCH] /api/v1/notifications/{id}/read: Menandai notifikasi sebagai sudah dibaca.
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<SystemNotification>> markNotificationAsRead(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        log.info("CONTROLLER: Received PATCH request to mark notification ID {} as read at path: {}", id, request.getRequestURI());

        try {
            SystemNotification updatedNotification = notificationService.markAsRead(id);

            // Menggunakan static method success(String message, T data)
            ApiResponse<SystemNotification> response = ApiResponse.success(
                    "Notification marked as read.",
                    updatedNotification
            );
            response.setPath(request.getRequestURI());

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            // Error handling, menggunakan static method error(String message)
            log.error("CONTROLLER ERROR: Notification ID {} not found or error: {}", id, e.getMessage());

            ApiResponse<SystemNotification> response = ApiResponse.error(
                    e.getMessage() + " | Please check the ID.",
                    request.getRequestURI() // Menggunakan error(String message, String path)
            );

            // Mengembalikan status 404 NOT FOUND untuk kasus RuntimeException (Not Found)
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }
}