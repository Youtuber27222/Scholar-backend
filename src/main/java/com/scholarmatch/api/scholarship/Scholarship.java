package com.scholarmatch.api.scholarship;

import com.scholarmatch.api.common.AcademicLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "scholarships")
public class Scholarship {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "provider_id")
  private UUID providerId;

  @Column(name = "provider_name", nullable = false)
  private String providerName;

  @Column(nullable = false)
  private String title;

  private String description;

  @Column(name = "funding_amount", precision = 12, scale = 2)
  private BigDecimal fundingAmount;

  @Column(nullable = false, length = 3)
  private String currency = "GHS";

  @Column(nullable = false)
  private LocalDate deadline;

  @Column(name = "field_of_study")
  private String fieldOfStudy;

  @Column(name = "academic_level")
  private AcademicLevel academicLevel;

  private String nationality;

  @Column(name = "min_gpa", precision = 3, scale = 2)
  private BigDecimal minGpa;

  @Column(name = "financial_need", nullable = false)
  private boolean financialNeed;

  private String country;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(nullable = false)
  private List<String> requirements = new ArrayList<>();

  @Column(name = "is_featured", nullable = false)
  private boolean isFeatured;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected Scholarship() {
  }

  public Scholarship(UUID providerId, String providerName, String title, LocalDate deadline) {
    this.providerId = providerId;
    this.providerName = providerName;
    this.title = title;
    this.deadline = deadline;
  }

  public UUID getId() {
    return id;
  }

  public UUID getProviderId() {
    return providerId;
  }

  public String getProviderName() {
    return providerName;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BigDecimal getFundingAmount() {
    return fundingAmount;
  }

  public void setFundingAmount(BigDecimal fundingAmount) {
    this.fundingAmount = fundingAmount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public LocalDate getDeadline() {
    return deadline;
  }

  public void setDeadline(LocalDate deadline) {
    this.deadline = deadline;
  }

  public String getFieldOfStudy() {
    return fieldOfStudy;
  }

  public void setFieldOfStudy(String fieldOfStudy) {
    this.fieldOfStudy = fieldOfStudy;
  }

  public AcademicLevel getAcademicLevel() {
    return academicLevel;
  }

  public void setAcademicLevel(AcademicLevel academicLevel) {
    this.academicLevel = academicLevel;
  }

  public String getNationality() {
    return nationality;
  }

  public void setNationality(String nationality) {
    this.nationality = nationality;
  }

  public BigDecimal getMinGpa() {
    return minGpa;
  }

  public void setMinGpa(BigDecimal minGpa) {
    this.minGpa = minGpa;
  }

  public boolean isFinancialNeed() {
    return financialNeed;
  }

  public void setFinancialNeed(boolean financialNeed) {
    this.financialNeed = financialNeed;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public List<String> getRequirements() {
    return requirements;
  }

  public void setRequirements(List<String> requirements) {
    this.requirements = requirements;
  }

  public boolean isFeatured() {
    return isFeatured;
  }

  public void setFeatured(boolean featured) {
    isFeatured = featured;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
