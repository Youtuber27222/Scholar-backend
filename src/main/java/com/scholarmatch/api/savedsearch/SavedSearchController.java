package com.scholarmatch.api.savedsearch;

import com.scholarmatch.api.auth.AuthPrincipal;
import com.scholarmatch.api.savedsearch.dto.SavedSearchCreateRequest;
import com.scholarmatch.api.savedsearch.dto.SavedSearchResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/saved-searches")
public class SavedSearchController {

  private final SavedSearchService savedSearchService;

  public SavedSearchController(SavedSearchService savedSearchService) {
    this.savedSearchService = savedSearchService;
  }

  @GetMapping
  public List<SavedSearchResponse> list(@AuthenticationPrincipal AuthPrincipal principal) {
    return savedSearchService.list(principal.id());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SavedSearchResponse create(
      @AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody SavedSearchCreateRequest request) {
    return savedSearchService.create(principal.id(), request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
    savedSearchService.delete(principal.id(), id);
  }
}
