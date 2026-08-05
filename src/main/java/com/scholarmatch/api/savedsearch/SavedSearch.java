package com.scholarmatch.api.savedsearch;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "saved_searches")
public class SavedSearch {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  private String name;

  private String query;

  @Column(name = "field_of_study")
  private String fieldOfStudy;

  @Column(name = "min_gpa")
  private BigDecimal minGpa;

  @Column(name = "financial_need")
  private Boolean financialNeed;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  protected SavedSearch() {
  }

  public SavedSearch(UUID userId, String name, String query, String fieldOfStudy, BigDecimal minGpa, Boolean financialNeed) {
    this.userId = userId;
    this.name = name;
    this.query = query;
    this.fieldOfStudy = fieldOfStudy;
    this.minGpa = minGpa;
    this.financialNeed = financialNeed;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getName() {
    return name;
  }

  public String getQuery() {
    return query;
  }

  public String getFieldOfStudy() {
    return fieldOfStudy;
  }

  public BigDecimal getMinGpa() {
    return minGpa;
  }

  public Boolean getFinancialNeed() {
    return financialNeed;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
