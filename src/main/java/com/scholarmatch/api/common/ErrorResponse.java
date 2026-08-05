package com.scholarmatch.api.common;

import java.util.Map;

public record ErrorResponse(String message, Map<String, ?> errors) {

  public static ErrorResponse of(String message) {
    return new ErrorResponse(message, null);
  }
}
