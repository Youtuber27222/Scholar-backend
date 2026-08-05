package com.scholarmatch.api.admin;

import com.scholarmatch.api.admin.dto.AdminProviderSummaryResponse;
import com.scholarmatch.api.admin.dto.ProviderInviteRequest;
import com.scholarmatch.api.auth.AuthPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/providers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProviderController {

  private final ProviderInviteService providerInviteService;

  public AdminProviderController(ProviderInviteService providerInviteService) {
    this.providerInviteService = providerInviteService;
  }

  @PostMapping("/invites")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void invite(@AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody ProviderInviteRequest request) {
    providerInviteService.invite(principal.id(), request);
  }

  @GetMapping
  public List<AdminProviderSummaryResponse> list() {
    return providerInviteService.listProviders();
  }

  @GetMapping("/{id}")
  public AdminProviderSummaryResponse get(@PathVariable UUID id) {
    return providerInviteService.getProvider(id);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revoke(@PathVariable UUID id) {
    providerInviteService.revoke(id);
  }
}
