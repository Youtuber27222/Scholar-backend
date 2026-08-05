package com.scholarmatch.api.message.dto;

import com.scholarmatch.api.application.ApplicationStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ThreadSummaryResponse(
    UUID applicationId,
    UUID scholarshipId,
    String scholarshipTitle,
    ApplicationStatus applicationStatus,
    UUID counterpartId,
    String counterpartName,
    String lastMessagePreview,
    OffsetDateTime lastMessageAt,
    int unreadCount) {
}
