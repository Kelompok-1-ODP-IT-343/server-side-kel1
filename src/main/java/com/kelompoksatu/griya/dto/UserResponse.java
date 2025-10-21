package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.UserStatus;
import java.time.LocalDateTime;

/** DTO for user response (without sensitive information) */
public class UserResponse {

  private Integer id;
  private String username;
  private String email;
  private String phone;
  private Integer roleId;
  private String roleName;
  private UserStatus status;
  private boolean emailVerified;
  private boolean phoneVerified;
  private LocalDateTime lastLoginAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  // Default constructor
  public UserResponse() {}

  // Constructor with parameters
  public UserResponse(
      Integer id,
      String username,
      String email,
      String phone,
      Integer roleId,
      String roleName,
      UserStatus status,
      boolean emailVerified,
      boolean phoneVerified,
      LocalDateTime lastLoginAt,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.phone = phone;
    this.roleId = roleId;
    this.roleName = roleName;
    this.status = status;
    this.emailVerified = emailVerified;
    this.phoneVerified = phoneVerified;
    this.lastLoginAt = lastLoginAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  // Getters and Setters
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public Integer getRoleId() {
    return roleId;
  }

  public void setRoleId(Integer roleId) {
    this.roleId = roleId;
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public boolean isEmailVerified() {
    return emailVerified;
  }

  public void setEmailVerified(boolean emailVerified) {
    this.emailVerified = emailVerified;
  }

  public boolean isPhoneVerified() {
    return phoneVerified;
  }

  public void setPhoneVerified(boolean phoneVerified) {
    this.phoneVerified = phoneVerified;
  }

  public LocalDateTime getLastLoginAt() {
    return lastLoginAt;
  }

  public void setLastLoginAt(LocalDateTime lastLoginAt) {
    this.lastLoginAt = lastLoginAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
