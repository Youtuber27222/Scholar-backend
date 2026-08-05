package com.scholarmatch.api.savedsearch;

import com.scholarmatch.api.common.ApiException;
import com.scholarmatch.api.savedsearch.dto.SavedSearchCreateRequest;
import com.scholarmatch.api.savedsearch.dto.SavedSearchResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SavedSearchService {

  private final SavedSearchRepository savedSearchRepository;

  public SavedSearchService(SavedSearchRepository savedSearchRepository) {
    this.savedSearchRepository = savedSearchRepository;
  }

  public List<SavedSearchResponse> list(UUID userId) {
    return savedSearchRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(SavedSearchResponse::from)
        .toList();
  }

  @Transactional
  public SavedSearchResponse create(UUID userId, SavedSearchCreateRequest request) {
    SavedSearch savedSearch = new SavedSearch(
        userId, request.name(), request.query(), request.fieldOfStudy(), request.minGpa(), request.financialNeed());
    return SavedSearchResponse.from(savedSearchRepository.saveAndFlush(savedSearch));
  }

  @Transactional
  public void delete(UUID userId, UUID id) {
    SavedSearch savedSearch = savedSearchRepository.findByIdAndUserId(id, userId)
        .orElseThrow(() -> ApiException.notFound("Saved search not found"));
    savedSearchRepository.delete(savedSearch);
  }
}
