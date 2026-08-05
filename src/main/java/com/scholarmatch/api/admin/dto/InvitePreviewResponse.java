package com.scholarmatch.api.admin.dto;

import com.scholarmatch.api.admin.ProviderInvite;
import java.time.OffsetDateTime;

public record InvitePreviewResponse(String email, String organizationName, OffsetDateTime expiresAt) {

  public static InvitePreviewResponse from(ProviderInvite invite) {
    return new InvitePreviewResponse(invite.getEmail(), invite.getOrganizationName(), invite.getExpiresAt());
  }
}
