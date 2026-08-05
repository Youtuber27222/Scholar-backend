package com.scholarmatch.api.savedsearch.dto;

import com.scholarmatch.api.savedsearch.SavedSearch;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SavedSearchResponse(
    UUID id,
    UUID userId,
    String name,
    String query,
    String fieldOfStudy,
    BigDecimal minGpa,
    Boolean financialNeed,
    OffsetDateTime createdAt) {

  public static SavedSearchResponse from(SavedSearch savedSearch) {
    return new SavedSearchResponse(
        savedSearch.getId(),
        savedSearch.getUserId(),
        savedSearch.getName(),
        savedSearch.getQuery(),
        savedSearch.getFieldOfStudy(),
        savedSearch.getMinGpa(),
        savedSearch.getFinancialNeed(),
        savedSearch.getCreatedAt());
  }
}
