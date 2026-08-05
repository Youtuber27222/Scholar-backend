package com.scholarmatch.api.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {

  @Override
  public String convertToDatabaseColumn(Role attribute) {
    return attribute == null ? null : attribute.getValue();
  }

  @Override
  public Role convertToEntityAttribute(String dbData) {
    return dbData == null ? null : Role.fromValue(dbData);
  }
}
