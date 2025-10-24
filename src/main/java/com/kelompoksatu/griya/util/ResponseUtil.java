package com.kelompoksatu.griya.util;

import org.springframework.http.HttpStatus;

public class ResponseUtil {
  private Object createErrorResponse(String message) {
    return new ErrorResponse(message, System.currentTimeMillis());
  }

  /** Determine HTTP status code based on error message */
  private HttpStatus determineErrorStatus(String errorMessage) {
    String lowerMessage = errorMessage.toLowerCase();

    if (lowerMessage.contains("tidak ditemukan") || lowerMessage.contains("not found")) {
      return HttpStatus.NOT_FOUND;
    }
    if (lowerMessage.contains("sudah ada")
        || lowerMessage.contains("already exists")
        || lowerMessage.contains("pending")) {
      return HttpStatus.CONFLICT;
    }
    if (lowerMessage.contains("tidak valid")
        || lowerMessage.contains("invalid")
        || lowerMessage.contains("authorization")) {
      return HttpStatus.UNAUTHORIZED;
    }
    if (lowerMessage.contains("tidak memenuhi") || lowerMessage.contains("validation")) {
      return HttpStatus.BAD_REQUEST;
    }
    if (lowerMessage.contains("terlalu besar") || lowerMessage.contains("too large")) {
      return HttpStatus.PAYLOAD_TOO_LARGE;
    }
    if (lowerMessage.contains("tipe file")
        || lowerMessage.contains("file type")
        || lowerMessage.contains("unsupported")) {
      return HttpStatus.UNSUPPORTED_MEDIA_TYPE;
    }

    return HttpStatus.INTERNAL_SERVER_ERROR;
  }

  /** Error response DTO */
  private static class ErrorResponse {
    private final String message;
    private final long timestamp;

    public ErrorResponse(String message, long timestamp) {
      this.message = message;
      this.timestamp = timestamp;
    }

    public String getMessage() {
      return message;
    }

    public long getTimestamp() {
      return timestamp;
    }
  }
}
