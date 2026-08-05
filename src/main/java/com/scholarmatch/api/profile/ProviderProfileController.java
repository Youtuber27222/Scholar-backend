package com.scholarmatch.api.profile;

import com.scholarmatch.api.auth.AuthPrincipal;
import com.scholarmatch.api.profile.dto.ProviderProfileResponse;
import com.scholarmatch.api.profile.dto.ProviderProfileUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/provider-profile")
@PreAuthorize("hasAnyRole('PROVIDER','ADMIN')")
public class ProviderProfileController {

  private final ProviderProfileService providerProfileService;

  public ProviderProfileController(ProviderProfileService providerProfileService) {
    this.providerProfileService = providerProfileService;
  }

  @GetMapping
  public ProviderProfileResponse get(@AuthenticationPrincipal AuthPrincipal principal) {
    return providerProfileService.get(principal.id());
  }

  @PutMapping
  public ProviderProfileResponse update(
      @AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody ProviderProfileUpdateRequest request) {
    return providerProfileService.upsert(principal.id(), request);
  }
}
