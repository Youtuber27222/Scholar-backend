package com.scholarmatch.api.savedsearch.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record SavedSearchCreateRequest(
    @Size(max = 120) String name,
    @Size(max = 100) String query,
    @Size(max = 120) String fieldOfStudy,
    @DecimalMin("0") @DecimalMax("4") BigDecimal minGpa,
    Boolean financialNeed) {
}
