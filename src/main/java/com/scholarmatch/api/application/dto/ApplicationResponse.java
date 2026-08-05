package com.scholarmatch.api.application.dto;

import com.scholarmatch.api.application.Application;
import com.scholarmatch.api.application.ApplicationStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ApplicationResponse(
    UUID id,
    UUID studentId,
    UUID scholarshipId,
    ApplicationStatus status,
    String essay,
    String providerNotes,
    OffsetDateTime submittedAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {

  public static ApplicationResponse from(Application application) {
    return new ApplicationResponse(
        application.getId(),
        application.getStudentId(),
        application.getScholarshipId(),
        application.getStatus(),
        application.getEssay(),
        application.getProviderNotes(),
        application.getSubmittedAt(),
        application.getCreatedAt(),
        application.getUpdatedAt());
  }
}
