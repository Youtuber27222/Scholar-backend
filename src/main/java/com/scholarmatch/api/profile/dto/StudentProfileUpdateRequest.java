package com.scholarmatch.api.profile.dto;

import com.scholarmatch.api.common.AcademicLevel;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record StudentProfileUpdateRequest(
    @DecimalMin("0") @DecimalMax("4") BigDecimal gpa,
    @Size(max = 120) String fieldOfStudy,
    AcademicLevel academicLevel,
    @Size(max = 80) String nationality,
    Boolean financialNeed,
    @Size(max = 180) String institution,
    @Size(max = 3000) String bio) {

  public boolean isEmpty() {
    return gpa == null
        && fieldOfStudy == null
        && academicLevel == null
        && nationality == null
        && financialNeed == null
        && institution == null
        && bio == null;
  }
}
