package com.scholarmatch.api.scholarship.dto;

import com.scholarmatch.api.common.AcademicLevel;
import com.scholarmatch.api.scholarship.Scholarship;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ScholarshipResponse(
    UUID id,
    UUID providerId,
    String providerName,
    String title,
    String description,
    BigDecimal fundingAmount,
    String currency,
    LocalDate deadline,
    String fieldOfStudy,
    AcademicLevel academicLevel,
    String nationality,
    BigDecimal minGpa,
    boolean financialNeed,
    String country,
    List<String> requirements,
    boolean isFeatured,
    boolean isActive,
    Integer matchScore) {

  public static ScholarshipResponse from(Scholarship scholarship) {
    return from(scholarship, null);
  }

  public static ScholarshipResponse from(Scholarship scholarship, Integer matchScore) {
    return new ScholarshipResponse(
        scholarship.getId(),
        scholarship.getProviderId(),
        scholarship.getProviderName(),
        scholarship.getTitle(),
        scholarship.getDescription(),
        scholarship.getFundingAmount(),
        scholarship.getCurrency(),
        scholarship.getDeadline(),
        scholarship.getFieldOfStudy(),
        scholarship.getAcademicLevel(),
        scholarship.getNationality(),
        scholarship.getMinGpa(),
        scholarship.isFinancialNeed(),
        scholarship.getCountry(),
        scholarship.getRequirements(),
        scholarship.isFeatured(),
        scholarship.isActive(),
        matchScore);
  }
}
