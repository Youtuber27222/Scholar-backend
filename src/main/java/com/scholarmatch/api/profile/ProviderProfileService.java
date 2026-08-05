package com.scholarmatch.api.profile;

import com.scholarmatch.api.common.ApiException;
import com.scholarmatch.api.profile.dto.ProviderProfileResponse;
import com.scholarmatch.api.profile.dto.ProviderProfileUpdateRequest;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProviderProfileService {

  private final ProviderProfileRepository providerProfileRepository;

  public ProviderProfileService(ProviderProfileRepository providerProfileRepository) {
    this.providerProfileRepository = providerProfileRepository;
  }

  public ProviderProfileResponse get(UUID userId) {
    ProviderProfile profile = providerProfileRepository.findByUserId(userId)
        .orElseThrow(() -> ApiException.notFound("Provider profile not found"));
    return ProviderProfileResponse.from(profile);
  }

  @Transactional
  public ProviderProfileResponse upsert(UUID userId, ProviderProfileUpdateRequest request) {
    if (request.isEmpty()) {
      throw ApiException.badRequest("At least one field is required");
    }
    ProviderProfile profile = providerProfileRepository.findByUserId(userId)
        .orElseGet(() -> new ProviderProfile(userId));
    if (request.organizationName() != null) {
      profile.setOrganizationName(request.organizationName());
    }
    if (request.organizationDescription() != null) {
      profile.setOrganizationDescription(request.organizationDescription());
    }
    if (request.website() != null) {
      profile.setWebsite(request.website());
    }
    return ProviderProfileResponse.from(providerProfileRepository.save(profile));
  }
}
