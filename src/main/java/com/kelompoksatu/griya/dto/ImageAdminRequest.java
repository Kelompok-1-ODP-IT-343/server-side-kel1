package com.kelompoksatu.griya.dto;

import com.kelompoksatu.griya.entity.ImageCategory;
import com.kelompoksatu.griya.entity.ImageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Request body for uploading admin image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageAdminRequest {

  @Schema(description = "Property ID that this image belongs to", example = "1")
  private Integer propertyId;

  @Schema(
      description =
          "Type of the image (EXTERIOR, INTERIOR, FLOOR_PLAN, SITE_PLAN, LOCATION, AMENITIES, 360_VIEW)",
      example = "EXTERIOR")
  private ImageType imageType;

  @Schema(description = "Category of the image (MAIN, GALLERY, THUMBNAIL)", example = "GALLERY")
  private ImageCategory imageCategory;

  @Schema(
      description = "Optional caption or description for the image",
      example = "Tampak depan rumah")
  private String caption;
}
