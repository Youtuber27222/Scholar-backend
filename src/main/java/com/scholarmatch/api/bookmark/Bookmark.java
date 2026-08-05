package com.scholarmatch.api.bookmark;

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
@Table(name = "bookmarks")
public class Bookmark {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "scholarship_id", nullable = false)
  private UUID scholarshipId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  protected Bookmark() {
  }

  public Bookmark(UUID userId, UUID scholarshipId) {
    this.userId = userId;
    this.scholarshipId = scholarshipId;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getScholarshipId() {
    return scholarshipId;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
