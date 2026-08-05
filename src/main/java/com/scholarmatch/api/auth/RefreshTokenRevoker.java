package com.scholarmatch.api.auth;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs the reuse-detection revocation in its own transaction so it commits even
 * though {@link RefreshTokenService#rotate} throws immediately afterward (which
 * would otherwise roll back the revocation along with the rest of that method).
 */
@Component
public class RefreshTokenRevoker {

  private final RefreshTokenRepository refreshTokenRepository;

  public RefreshTokenRevoker(RefreshTokenRepository refreshTokenRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void revokeAllNow(UUID userId) {
    refreshTokenRepository.revokeAllActiveForUser(userId, OffsetDateTime.now());
  }
}
