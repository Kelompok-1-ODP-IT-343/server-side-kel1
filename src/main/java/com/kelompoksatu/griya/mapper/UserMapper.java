package com.kelompoksatu.griya.mapper;

import com.kelompoksatu.griya.dto.UpdateUserRequest;
import com.kelompoksatu.griya.dto.UserResponse;
import com.kelompoksatu.griya.entity.User;
import com.kelompoksatu.griya.entity.UserProfile;
import com.kelompoksatu.griya.entity.UserStatus;
import org.mapstruct.*;

/**
 * MapStruct mapper for User entity conversions.
 *
 * <p>This mapper handles the conversion between: - UpdateUserRequest -> User (for updates) - User
 * -> UserResponse (for responses)
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

  /**
   * Maps UpdateUserRequest to existing User entity for updates.
   *
   * <p>This method updates only the non-null fields from the request, preserving existing values
   * for null fields. Only user account fields are mapped, profile fields are handled separately.
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
  @Mapping(target = "status", source = "request", qualifiedByName = "requestToUserStatus")
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
   * Maps UpdateUserRequest to existing UserProfile entity for updates.
   *
   * <p>This method updates only the non-null profile fields from the request, preserving existing
   * values for null fields. Only profile fields are mapped, user account fields are handled
   * separately.
   *
   * @param request The update request DTO
   * @param existingProfile The existing user profile entity to update
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "userId", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "gender", ignore = true)
  @Mapping(target = "maritalStatus", ignore = true)
  void updateUserProfileFromRequest(
      UpdateUserRequest request, @MappingTarget UserProfile existingProfile);

  /**
   * Convert UpdateUserRequest to UserStatus enum.
   *
   * @param request The update request
   * @return UserStatus enum or null if status is null/empty
   */
  @Named("requestToUserStatus")
  default UserStatus requestToUserStatus(UpdateUserRequest request) {
    return request.getUserStatusEnum();
  }
}
