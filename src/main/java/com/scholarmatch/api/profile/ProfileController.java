package com.scholarmatch.api.profile;

import com.scholarmatch.api.auth.AuthPrincipal;
import com.scholarmatch.api.profile.dto.ProfileResponse;
import com.scholarmatch.api.profile.dto.ProfileUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/profile")
public class ProfileController {

  private final ProfileService profileService;

  public ProfileController(ProfileService profileService) {
    this.profileService = profileService;
  }

  @GetMapping
  public ProfileResponse get(@AuthenticationPrincipal AuthPrincipal principal) {
    return profileService.get(principal.id());
  }

  @PutMapping
  public ProfileResponse update(
      @AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody ProfileUpdateRequest request) {
    return profileService.update(principal.id(), request);
  }

  @PostMapping(value = "/avatar", consumes = "multipart/form-data")
  public ProfileResponse uploadAvatar(
      @AuthenticationPrincipal AuthPrincipal principal, @RequestParam("file") MultipartFile file) {
    return profileService.updateAvatar(principal.id(), file);
  }
}
