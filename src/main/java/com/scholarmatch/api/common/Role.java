package com.scholarmatch.api.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
  STUDENT("student"),
  PROVIDER("provider"),
  ADMIN("admin");

  private final String value;

  Role(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  public String authority() {
    return "ROLE_" + name();
  }

  @JsonCreator
  public static Role fromValue(String value) {
    for (Role role : values()) {
      if (role.value.equalsIgnoreCase(value)) {
        return role;
      }
    }
    throw new IllegalArgumentException("Unknown role: " + value);
  }
}
