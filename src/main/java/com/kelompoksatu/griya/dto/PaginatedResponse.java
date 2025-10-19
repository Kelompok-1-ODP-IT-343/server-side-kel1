package com.kelompoksatu.griya.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic paginated response wrapper for API responses.
 *
 * @param <T> The type of data being paginated
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description =
        "Paginated response wrapper containing data, pagination metadata, and navigation information")
public class PaginatedResponse<T> {

  @Schema(description = "List of items in the current page", example = "[]")
  private List<T> data;

  @Schema(
      description = "Pagination metadata",
      example = "{\"page\": 0, \"size\": 10, \"totalElements\": 100, \"totalPages\": 10}")
  private PaginationInfo pagination;

  @Schema(
      description = "Navigation links for pagination",
      example = "{\"first\": true, \"last\": false, \"hasNext\": true, \"hasPrevious\": false}")
  private NavigationInfo navigation;

  /**
   * Create a paginated response from Spring Data Page object.
   *
   * @param page The Spring Data Page object
   * @param <T> The type of data
   * @return PaginatedResponse with data and metadata
   */
  public static <T> PaginatedResponse<T> of(org.springframework.data.domain.Page<T> page) {
    PaginatedResponse<T> response = new PaginatedResponse<>();
    response.setData(page.getContent());
    response.setPagination(PaginationInfo.of(page));
    response.setNavigation(NavigationInfo.of(page));
    return response;
  }

  /** Pagination metadata information. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Pagination metadata containing page information")
  public static class PaginationInfo {

    @Schema(description = "Current page number (0-based)", example = "0")
    private int page;

    @Schema(description = "Number of items per page", example = "10")
    private int size;

    @Schema(description = "Total number of elements across all pages", example = "100")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "10")
    private int totalPages;

    @Schema(description = "Number of elements in current page", example = "10")
    private int numberOfElements;

    @Schema(description = "Whether current page is the first page", example = "true")
    private boolean first;

    @Schema(description = "Whether current page is the last page", example = "false")
    private boolean last;

    @Schema(description = "Whether current page is empty", example = "false")
    private boolean empty;

    /** Create PaginationInfo from Spring Data Page. */
    public static PaginationInfo of(org.springframework.data.domain.Page<?> page) {
      PaginationInfo info = new PaginationInfo();
      info.setPage(page.getNumber());
      info.setSize(page.getSize());
      info.setTotalElements(page.getTotalElements());
      info.setTotalPages(page.getTotalPages());
      info.setNumberOfElements(page.getNumberOfElements());
      info.setFirst(page.isFirst());
      info.setLast(page.isLast());
      info.setEmpty(page.isEmpty());
      return info;
    }
  }

  /** Navigation information for pagination links. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(description = "Navigation information for pagination links")
  public static class NavigationInfo {

    @Schema(description = "Whether there is a next page", example = "true")
    private boolean hasNext;

    @Schema(description = "Whether there is a previous page", example = "false")
    private boolean hasPrevious;

    @Schema(description = "Whether this is the first page", example = "true")
    private boolean first;

    @Schema(description = "Whether this is the last page", example = "false")
    private boolean last;

    @Schema(description = "Next page number (null if no next page)", example = "1")
    private Integer nextPage;

    @Schema(description = "Previous page number (null if no previous page)", example = "null")
    private Integer previousPage;

    /** Create NavigationInfo from Spring Data Page. */
    public static NavigationInfo of(org.springframework.data.domain.Page<?> page) {
      NavigationInfo info = new NavigationInfo();
      info.setHasNext(page.hasNext());
      info.setHasPrevious(page.hasPrevious());
      info.setFirst(page.isFirst());
      info.setLast(page.isLast());
      info.setNextPage(page.hasNext() ? page.getNumber() + 1 : null);
      info.setPreviousPage(page.hasPrevious() ? page.getNumber() - 1 : null);
      return info;
    }
  }
}
