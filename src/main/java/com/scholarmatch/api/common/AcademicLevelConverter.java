package com.scholarmatch.api.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AcademicLevelConverter implements AttributeConverter<AcademicLevel, String> {

  @Override
  public String convertToDatabaseColumn(AcademicLevel attribute) {
    return attribute == null ? null : attribute.getValue();
  }

  @Override
  public AcademicLevel convertToEntityAttribute(String dbData) {
    return dbData == null ? null : AcademicLevel.fromValue(dbData);
  }
}
