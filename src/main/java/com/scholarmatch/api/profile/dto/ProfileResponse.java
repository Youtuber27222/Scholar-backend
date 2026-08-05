package com.scholarmatch.api.profile.dto;

import com.scholarmatch.api.profile.Profile;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProfileResponse(
    UUID id, String fullName, String email, String avatarUrl, OffsetDateTime createdAt, OffsetDateTime updatedAt) {

  public static ProfileResponse from(Profile profile) {
    return new ProfileResponse(
        profile.getId(),
        profile.getFullName(),
        profile.getEmail(),
        profile.getAvatarUrl(),
        profile.getCreatedAt(),
        profile.getUpdatedAt());
  }
}
