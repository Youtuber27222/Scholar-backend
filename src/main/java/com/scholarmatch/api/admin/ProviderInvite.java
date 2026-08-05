package com.scholarmatch.api.admin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "provider_invites")
public class ProviderInvite {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String email;

  @Column(name = "organization_name")
  private String organizationName;

  @Column(name = "invited_by", nullable = false)
  private UUID invitedBy;

  @Column(name = "token_hash", nullable = false, unique = true)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;

  @Column(name = "accepted_at")
  private OffsetDateTime acceptedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  protected ProviderInvite() {
  }

  public ProviderInvite(String email, String organizationName, UUID invitedBy, String tokenHash, OffsetDateTime expiresAt) {
    this.email = email;
    this.organizationName = organizationName;
    this.invitedBy = invitedBy;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getOrganizationName() {
    return organizationName;
  }

  public UUID getInvitedBy() {
    return invitedBy;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  public OffsetDateTime getAcceptedAt() {
    return acceptedAt;
  }

  public void setAcceptedAt(OffsetDateTime acceptedAt) {
    this.acceptedAt = acceptedAt;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public boolean isUsable() {
    return acceptedAt == null && expiresAt.isAfter(OffsetDateTime.now());
  }
}
