package com.kelompoksatu.griya.dto;

/**
 * DTO for authentication response
 */
public class AuthResponse {
    
    private String token;
    private String type = "Bearer";
    private UserResponse user;
    private String message;
    
    // Default constructor
    public AuthResponse() {}
    
    // Constructor with token and user
    public AuthResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }
    
    // Constructor with token, user, and message
    public AuthResponse(String token, UserResponse user, String message) {
        this.token = token;
        this.user = user;
        this.message = message;
    }
    
    // Constructor with message only (for error responses)
    public AuthResponse(String message) {
        this.message = message;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public UserResponse getUser() {
        return user;
    }
    
    public void setUser(UserResponse user) {
        this.user = user;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}