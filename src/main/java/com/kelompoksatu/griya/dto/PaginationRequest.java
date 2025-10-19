package com.kelompoksatu.griya.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for pagination parameters. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pagination request parameters for list endpoints")
public class PaginationRequest {

  @Schema(description = "Page number (0-based)", example = "0", minimum = "0", defaultValue = "0")
  @Min(value = 0, message = "Page number must be 0 or greater")
  private int page = 0;

  @Schema(
      description = "Number of items per page",
      example = "10",
      minimum = "1",
      maximum = "100",
      defaultValue = "10")
  @Min(value = 1, message = "Page size must be at least 1")
  @Max(value = 100, message = "Page size cannot exceed 100")
  private int size = 10;

  @Schema(description = "Sort field name", example = "companyName", defaultValue = "createdAt")
  private String sortBy = "createdAt";

  @Schema(
      description = "Sort direction",
      example = "desc",
      allowableValues = {"asc", "desc"},
      defaultValue = "desc")
  private String sortDirection = "desc";

  /**
   * Get the sort direction as Spring Data Sort.Direction.
   *
   * @return Sort.Direction enum value
   */
  public org.springframework.data.domain.Sort.Direction getSortDirectionEnum() {
    return "asc".equalsIgnoreCase(sortDirection)
        ? org.springframework.data.domain.Sort.Direction.ASC
        : org.springframework.data.domain.Sort.Direction.DESC;
  }

  /**
   * Create Spring Data Sort object from this request.
   *
   * @return Sort object for Spring Data repositories
   */
  public org.springframework.data.domain.Sort toSort() {
    return org.springframework.data.domain.Sort.by(getSortDirectionEnum(), sortBy);
  }

  /**
   * Create Spring Data Pageable object from this request.
   *
   * @return Pageable object for Spring Data repositories
   */
  public org.springframework.data.domain.Pageable toPageable() {
    return org.springframework.data.domain.PageRequest.of(page, size, toSort());
  }
}
