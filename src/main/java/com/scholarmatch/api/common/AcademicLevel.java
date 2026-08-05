package com.scholarmatch.api.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AcademicLevel {
  UNDERGRADUATE("undergraduate"),
  POSTGRADUATE("postgraduate"),
  PHD("phd");

  private final String value;

  AcademicLevel(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static AcademicLevel fromValue(String value) {
    for (AcademicLevel level : values()) {
      if (level.value.equalsIgnoreCase(value)) {
        return level;
      }
    }
    throw new IllegalArgumentException("Unknown academic level: " + value);
  }
}
