package com.scholarmatch.api.savedsearch;

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
@Table(name = "deadline_reminders_sent")
public class DeadlineReminderSent {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "scholarship_id", nullable = false)
  private UUID scholarshipId;

  @Column(name = "threshold_days", nullable = false)
  private int thresholdDays;

  @CreationTimestamp
  @Column(name = "sent_at", nullable = false, updatable = false)
  private OffsetDateTime sentAt;

  protected DeadlineReminderSent() {
  }

  public DeadlineReminderSent(UUID userId, UUID scholarshipId, int thresholdDays) {
    this.userId = userId;
    this.scholarshipId = scholarshipId;
    this.thresholdDays = thresholdDays;
  }

  public UUID getId() {
    return id;
  }
}
