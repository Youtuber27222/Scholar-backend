package com.scholarmatch.api.profile;

import com.scholarmatch.api.common.AcademicLevel;
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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "student_profiles")
public class StudentProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false, unique = true)
  private UUID userId;

  @Column(precision = 3, scale = 2)
  private BigDecimal gpa;

  @Column(name = "field_of_study")
  private String fieldOfStudy;

  @Column(name = "academic_level")
  private AcademicLevel academicLevel;

  private String nationality;

  @Column(name = "financial_need", nullable = false)
  private boolean financialNeed;

  private String institution;

  private String bio;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected StudentProfile() {
  }

  public StudentProfile(UUID userId) {
    this.userId = userId;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public BigDecimal getGpa() {
    return gpa;
  }

  public void setGpa(BigDecimal gpa) {
    this.gpa = gpa;
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

  public boolean isFinancialNeed() {
    return financialNeed;
  }

  public void setFinancialNeed(boolean financialNeed) {
    this.financialNeed = financialNeed;
  }

  public String getInstitution() {
    return institution;
  }

  public void setInstitution(String institution) {
    this.institution = institution;
  }

  public String getBio() {
    return bio;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }
}
