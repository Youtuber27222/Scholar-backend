package com.scholarmatch.api.application;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "applications")
public class Application {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "student_id", nullable = false)
  private UUID studentId;

  @Column(name = "scholarship_id", nullable = false)
  private UUID scholarshipId;

  @Column(nullable = false)
  private ApplicationStatus status = ApplicationStatus.DRAFTING;

  private String essay;

  @Column(name = "provider_notes")
  private String providerNotes;

  @Column(name = "submitted_at")
  private OffsetDateTime submittedAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected Application() {
  }

  public Application(UUID studentId, UUID scholarshipId) {
    this.studentId = studentId;
    this.scholarshipId = scholarshipId;
  }

  public UUID getId() {
    return id;
  }

  public UUID getStudentId() {
    return studentId;
  }

  public UUID getScholarshipId() {
    return scholarshipId;
  }

  public ApplicationStatus getStatus() {
    return status;
  }

  public void setStatus(ApplicationStatus status) {
    this.status = status;
  }

  public String getEssay() {
    return essay;
  }

  public void setEssay(String essay) {
    this.essay = essay;
  }

  public String getProviderNotes() {
    return providerNotes;
  }

  public void setProviderNotes(String providerNotes) {
    this.providerNotes = providerNotes;
  }

  public OffsetDateTime getSubmittedAt() {
    return submittedAt;
  }

  public void setSubmittedAt(OffsetDateTime submittedAt) {
    this.submittedAt = submittedAt;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
