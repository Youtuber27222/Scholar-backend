package com.scholarmatch.api.profile.dto;

import com.scholarmatch.api.common.AcademicLevel;
import com.scholarmatch.api.profile.StudentProfile;
import java.math.BigDecimal;
import java.util.UUID;

public record StudentProfileResponse(
    UUID id,
    UUID userId,
    BigDecimal gpa,
    String fieldOfStudy,
    AcademicLevel academicLevel,
    String nationality,
    boolean financialNeed,
    String institution,
    String bio) {

  public static StudentProfileResponse from(StudentProfile profile) {
    return new StudentProfileResponse(
        profile.getId(),
        profile.getUserId(),
        profile.getGpa(),
        profile.getFieldOfStudy(),
        profile.getAcademicLevel(),
        profile.getNationality(),
        profile.isFinancialNeed(),
        profile.getInstitution(),
        profile.getBio());
  }
}
