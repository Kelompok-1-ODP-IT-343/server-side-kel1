package com.kelompoksatu.griya.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "Response for uploaded admin image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageAdminResponse {

  @Schema(description = "Image ID in database", example = "15")
  private Integer id;

  @Schema(description = "Property ID related to image", example = "1")
  private Integer propertyId;

  @Schema(
      description = "Public URL to access image",
      example = "http://localhost:18080/uploads/exterior1.jpg")
  private String imageUrl;

  @Schema(description = "Stored file name", example = "exterior1.jpg")
  private String fileName;

  @Schema(description = "Image type", example = "EXTERIOR")
  private String imageType;

  @Schema(description = "Image category", example = "MAIN")
  private String imageCategory;

  @Schema(description = "Optional caption", example = "Tampak depan rumah utama")
  private String caption;

  @Schema(description = "File size (KB)", example = "1024")
  private Integer fileSize;

  @Schema(description = "MIME type of file", example = "image/jpeg")
  private String mimeType;
}
