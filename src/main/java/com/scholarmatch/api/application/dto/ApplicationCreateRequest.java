package com.scholarmatch.api.application.dto;

import com.scholarmatch.api.application.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ApplicationCreateRequest(
    @NotNull UUID scholarshipId,
    @Size(max = 10000) String essay,
    ApplicationStatus status) {

  public ApplicationStatus statusOrDefault() {
    return status == null ? ApplicationStatus.DRAFTING : status;
  }
}
