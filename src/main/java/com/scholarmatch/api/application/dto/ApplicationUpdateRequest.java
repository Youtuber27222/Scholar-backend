package com.scholarmatch.api.application.dto;

import com.scholarmatch.api.application.ApplicationStatus;
import jakarta.validation.constraints.Size;

public record ApplicationUpdateRequest(
    ApplicationStatus status,
    @Size(max = 10000) String essay,
    @Size(max = 10000) String providerNotes) {

  public boolean isEmpty() {
    return status == null && essay == null && providerNotes == null;
  }
}
