package com.kelompoksatu.griya.entity.converter;

import com.kelompoksatu.griya.entity.PropertyFeature;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

@Converter(autoApply = false)
public class FeatureCategoryConverter
    implements AttributeConverter<PropertyFeature.FeatureCategory, String> {

  @Override
  public String convertToDatabaseColumn(PropertyFeature.FeatureCategory attribute) {
    return attribute == null ? null : attribute.name().toLowerCase(Locale.ROOT);
  }

  @Override
  public PropertyFeature.FeatureCategory convertToEntityAttribute(String dbData) {
    return dbData == null
        ? null
        : PropertyFeature.FeatureCategory.valueOf(dbData.toUpperCase(Locale.ROOT));
  }
}
