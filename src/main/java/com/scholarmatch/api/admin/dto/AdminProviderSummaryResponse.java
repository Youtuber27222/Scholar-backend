package com.scholarmatch.api.admin.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminProviderSummaryResponse(
    UUID userId,
    String email,
    String fullName,
    String organizationName,
    String organizationDescription,
    String website,
    long scholarshipCount,
    OffsetDateTime providerSince) {
}
