package com.scholarmatch.api.admin;

import com.scholarmatch.api.admin.dto.AcceptInviteRequest;
import com.scholarmatch.api.admin.dto.InvitePreviewResponse;
import com.scholarmatch.api.auth.dto.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public, token-based — an invitee has no account (and thus no JWT) yet. */
@RestController
@RequestMapping("/provider-invites")
public class ProviderInviteController {

  private final ProviderInviteService providerInviteService;

  public ProviderInviteController(ProviderInviteService providerInviteService) {
    this.providerInviteService = providerInviteService;
  }

  @GetMapping("/{token}")
  public InvitePreviewResponse preview(@PathVariable String token) {
    return providerInviteService.previewInvite(token);
  }

  @PostMapping("/{token}/accept")
  public AuthResponse accept(@PathVariable String token, @Valid @RequestBody AcceptInviteRequest request) {
    return providerInviteService.acceptInvite(token, request);
  }
}
