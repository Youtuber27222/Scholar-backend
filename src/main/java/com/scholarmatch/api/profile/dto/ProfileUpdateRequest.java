package com.scholarmatch.api.profile.dto;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ProfileUpdateRequest(
    @Size(max = 120) String fullName,
    @URL @Size(max = 2048) String avatarUrl) {

  public boolean isEmpty() {
    return fullName == null && avatarUrl == null;
  }
}
