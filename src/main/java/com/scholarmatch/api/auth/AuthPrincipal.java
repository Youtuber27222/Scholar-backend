package com.scholarmatch.api.auth;

import com.scholarmatch.api.common.Role;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AuthPrincipal(UUID id, String email, List<Role> roles, UUID jti, Instant expiresAt) implements Principal {

  public boolean hasRole(Role role) {
    return roles.contains(role);
  }

  public boolean hasAnyRole(Role... candidates) {
    for (Role candidate : candidates) {
      if (roles.contains(candidate)) {
        return true;
      }
    }
    return false;
  }

  /** Stable STOMP/WebSocket destination name — matches the id used with convertAndSendToUser. */
  @Override
  public String getName() {
    return id().toString();
  }
}
