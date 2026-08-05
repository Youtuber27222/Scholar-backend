package com.scholarmatch.api.profile.dto;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ProviderProfileUpdateRequest(
    @Size(max = 200) String organizationName,
    @Size(max = 3000) String organizationDescription,
    @URL @Size(max = 300) String website) {

  public boolean isEmpty() {
    return organizationName == null && organizationDescription == null && website == null;
  }
}
