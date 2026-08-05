package com.scholarmatch.api.profile;

import com.scholarmatch.api.common.ApiException;
import com.scholarmatch.api.profile.dto.ProfileResponse;
import com.scholarmatch.api.profile.dto.ProfileUpdateRequest;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfileService {

  private final ProfileRepository profileRepository;
  private final AvatarStorageService avatarStorageService;

  public ProfileService(ProfileRepository profileRepository, AvatarStorageService avatarStorageService) {
    this.profileRepository = profileRepository;
    this.avatarStorageService = avatarStorageService;
  }

  public ProfileResponse get(UUID userId) {
    return ProfileResponse.from(findOrThrow(userId));
  }

  @Transactional
  public ProfileResponse update(UUID userId, ProfileUpdateRequest request) {
    if (request.isEmpty()) {
      throw ApiException.badRequest("At least one field is required");
    }
    Profile profile = findOrThrow(userId);
    if (request.fullName() != null) {
      profile.setFullName(request.fullName());
    }
    if (request.avatarUrl() != null) {
      profile.setAvatarUrl(request.avatarUrl());
    }
    return ProfileResponse.from(profileRepository.saveAndFlush(profile));
  }

  @Transactional
  public ProfileResponse updateAvatar(UUID userId, MultipartFile file) {
    Profile profile = findOrThrow(userId);
    String avatarUrl = avatarStorageService.store(userId, file);
    profile.setAvatarUrl(avatarUrl);
    return ProfileResponse.from(profileRepository.saveAndFlush(profile));
  }

  private Profile findOrThrow(UUID userId) {
    return profileRepository.findById(userId)
        .orElseThrow(() -> ApiException.notFound("Profile not found"));
  }
}
