package com.scholarmatch.api.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "revoked_tokens")
public class RevokedToken {

  @Id
  private UUID jti;

  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;

  @CreationTimestamp
  @Column(name = "revoked_at", nullable = false, updatable = false)
  private OffsetDateTime revokedAt;

  protected RevokedToken() {
  }

  public RevokedToken(UUID jti, OffsetDateTime expiresAt) {
    this.jti = jti;
    this.expiresAt = expiresAt;
  }

  public UUID getJti() {
    return jti;
  }

  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }
}
