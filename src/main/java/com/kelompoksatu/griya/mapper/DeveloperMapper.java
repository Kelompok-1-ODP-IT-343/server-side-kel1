package com.kelompoksatu.griya.mapper;

import com.kelompoksatu.griya.dto.CreateDeveloperRequest;
import com.kelompoksatu.griya.dto.DeveloperResponse;
import com.kelompoksatu.griya.dto.UpdateDeveloperRequest;
import com.kelompoksatu.griya.entity.Developer;
import org.mapstruct.*;

/**
 * MapStruct mapper for Developer entity conversions.
 *
 * <p>This mapper handles the conversion between: - CreateDeveloperRequest -> Developer (for
 * creation) - UpdateDeveloperRequest -> Developer (for updates) - Developer -> DeveloperResponse
 * (for responses)
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DeveloperMapper {

  /**
   * Maps CreateDeveloperRequest to Developer entity for creation.
   *
   * @param request The create request DTO
   * @return Developer entity ready for persistence
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "verifiedAt", ignore = true)
  @Mapping(target = "verifiedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Developer toEntity(CreateDeveloperRequest request);

  /**
   * Maps UpdateDeveloperRequest to existing Developer entity for updates.
   *
   * <p>This method updates only the non-null fields from the request, preserving existing values
   * for null fields.
   *
   * @param request The update request DTO
   * @param existingDeveloper The existing developer entity to update
   * @return Updated Developer entity
   */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "verifiedAt", ignore = true)
  @Mapping(target = "verifiedBy", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateDeveloperFromRequest(
      UpdateDeveloperRequest request, @MappingTarget Developer existingDeveloper);

  /**
   * Maps Developer entity to DeveloperResponse DTO.
   *
   * @param developer The developer entity
   * @return DeveloperResponse DTO for API responses
   */
  DeveloperResponse toResponse(Developer developer);

  /**
   * Maps Developer entity to DeveloperResponse DTO with custom mapping for null handling.
   *
   * <p>This method provides additional control over null value handling and can be used when
   * specific null handling is required.
   *
   * @param developer The developer entity
   * @return DeveloperResponse DTO for API responses
   */
  @Mapping(
      target = "description",
      source = "description",
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  DeveloperResponse toResponseWithNullCheck(Developer developer);
}
