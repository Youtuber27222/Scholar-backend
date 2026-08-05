package com.scholarmatch.api.scholarship.dto;

import com.scholarmatch.api.common.AcademicLevel;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ScholarshipCreateRequest(
    @NotBlank @Size(max = 200) String title,
    @Size(max = 10000) String description,
    @PositiveOrZero BigDecimal fundingAmount,
    @Size(min = 3, max = 3) String currency,
    @NotNull LocalDate deadline,
    @Size(max = 120) String fieldOfStudy,
    AcademicLevel academicLevel,
    @Size(max = 80) String nationality,
    @DecimalMin("0") @DecimalMax("4") BigDecimal minGpa,
    Boolean financialNeed,
    @Size(max = 80) String country,
    @Size(max = 30) List<@Size(max = 500) String> requirements,
    Boolean isActive) {
}
