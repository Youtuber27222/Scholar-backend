package com.scholarmatch.api.profile.dto;

import com.scholarmatch.api.profile.ProviderProfile;
import java.util.UUID;

public record ProviderProfileResponse(
    UUID id, UUID userId, String organizationName, String organizationDescription, String website) {

  public static ProviderProfileResponse from(ProviderProfile profile) {
    return new ProviderProfileResponse(
        profile.getId(),
        profile.getUserId(),
        profile.getOrganizationName(),
        profile.getOrganizationDescription(),
        profile.getWebsite());
  }
}
