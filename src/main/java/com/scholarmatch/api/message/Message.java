package com.scholarmatch.api.message;

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
@Table(name = "messages")
public class Message {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "application_id", nullable = false)
  private UUID applicationId;

  @Column(name = "sender_id", nullable = false)
  private UUID senderId;

  @Column(nullable = false)
  private String body;

  @Column(name = "read_at")
  private OffsetDateTime readAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  protected Message() {
  }

  public Message(UUID applicationId, UUID senderId, String body) {
    this.applicationId = applicationId;
    this.senderId = senderId;
    this.body = body;
  }

  public UUID getId() {
    return id;
  }

  public UUID getApplicationId() {
    return applicationId;
  }

  public UUID getSenderId() {
    return senderId;
  }

  public String getBody() {
    return body;
  }

  public OffsetDateTime getReadAt() {
    return readAt;
  }

  public void setReadAt(OffsetDateTime readAt) {
    this.readAt = readAt;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
