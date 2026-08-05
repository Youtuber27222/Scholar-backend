package com.scholarmatch.api.auth;

import com.scholarmatch.api.common.ApiException;
import com.scholarmatch.api.common.TokenHasher;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;
  private final RefreshTokenRevoker refreshTokenRevoker;
  private final Duration refreshTokenTtl;

  public RefreshTokenService(
      RefreshTokenRepository refreshTokenRepository,
      RefreshTokenRevoker refreshTokenRevoker,
      @Value("${app.jwt.refresh-token-ttl-days}") long refreshTokenTtlDays) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.refreshTokenRevoker = refreshTokenRevoker;
    this.refreshTokenTtl = Duration.ofDays(refreshTokenTtlDays);
  }

  @Transactional
  public String issue(UUID userId) {
    String raw = TokenHasher.generateRawToken();
    RefreshToken token = new RefreshToken(userId, TokenHasher.sha256Hex(raw), OffsetDateTime.now().plus(refreshTokenTtl));
    refreshTokenRepository.save(token);
    return raw;
  }

  public record Rotated(UUID userId, String newRawToken) {
  }

  @Transactional
  public Rotated rotate(String rawToken) {
    String hash = TokenHasher.sha256Hex(rawToken);
    RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
        .orElseThrow(() -> ApiException.unauthorized("Invalid refresh token"));

    if (existing.getRevokedAt() != null) {
      // Presenting an already-rotated token is a signal of token theft: burn the whole chain.
      // Runs in its own transaction (see RefreshTokenRevoker) so it isn't undone by the
      // rollback that follows this method throwing.
      refreshTokenRevoker.revokeAllNow(existing.getUserId());
      throw ApiException.unauthorized("Refresh token has already been used; please log in again");
    }
    if (existing.getExpiresAt().isBefore(OffsetDateTime.now())) {
      throw ApiException.unauthorized("Refresh token has expired");
    }

    String newRaw = TokenHasher.generateRawToken();
    RefreshToken replacement = new RefreshToken(
        existing.getUserId(), TokenHasher.sha256Hex(newRaw), OffsetDateTime.now().plus(refreshTokenTtl));
    refreshTokenRepository.save(replacement);

    existing.setRevokedAt(OffsetDateTime.now());
    existing.setReplacedById(replacement.getId());
    refreshTokenRepository.save(existing);

    return new Rotated(existing.getUserId(), newRaw);
  }

  @Transactional
  public void revoke(String rawToken) {
    refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex(rawToken))
        .ifPresent(token -> {
          token.setRevokedAt(OffsetDateTime.now());
          refreshTokenRepository.save(token);
        });
  }

  @Transactional
  public void revokeAllForUser(UUID userId) {
    refreshTokenRepository.revokeAllActiveForUser(userId, OffsetDateTime.now());
  }
}
