package com.kelompoksatu.griya.mapper;

import com.kelompoksatu.griya.dto.UpdateUserRequest;
import com.kelompoksatu.griya.dto.UserResponse;
import com.kelompoksatu.griya.entity.User;
import com.kelompoksatu.griya.entity.UserStatus;
import org.mapstruct.*;

/**
 * MapStruct mapper for User entity conversions.
 * 
 * <p>This mapper handles the conversion between: - UpdateUserRequest -> User (for updates) -
 * User -> UserResponse (for responses)
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

  /**
   * Maps UpdateUserRequest to existing User entity for updates.
   *
   * <p>This method updates only the non-null fields from the request, preserving existing values
   * for null fields. Password updates are handled separately for security.
   *
   * @param request The update request DTO
   * @param existingUser The existing user entity to update
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "passwordHash", ignore = true) // Password handled separately
  @Mapping(target = "emailVerifiedAt", ignore = true)
  @Mapping(target = "phoneVerifiedAt", ignore = true)
  @Mapping(target = "lastLoginAt", ignore = true)
  @Mapping(target = "failedLoginAttempts", ignore = true)
  @Mapping(target = "lockedUntil", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "role", ignore = true)
  @Mapping(target = "consentAt", ignore = true)
  @Mapping(
      target = "status",
      source = "status",
      qualifiedByName = "stringToUserStatus")
  void updateUserFromRequest(UpdateUserRequest request, @MappingTarget User existingUser);

  /**
   * Maps User entity to UserResponse DTO.
   *
   * @param user The user entity
   * @return UserResponse DTO for API responses
   */
  @Mapping(target = "roleId", source = "role.id")
  @Mapping(target = "roleName", source = "role.name")
  @Mapping(target = "emailVerified", expression = "java(user.getEmailVerifiedAt() != null)")
  @Mapping(target = "phoneVerified", expression = "java(user.getPhoneVerifiedAt() != null)")
  UserResponse toResponse(User user);

  /**
   * Convert string status to UserStatus enum.
   *
   * @param status String representation of status
   * @return UserStatus enum or null if status is null/empty
   */
  @Named("stringToUserStatus")
  default UserStatus stringToUserStatus(String status) {
    if (status == null || status.trim().isEmpty()) {
      return null;
    }
    try {
      return UserStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      return null; // Invalid status, ignore update
    }
  }
}
