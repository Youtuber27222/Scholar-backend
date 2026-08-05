package com.scholarmatch.api.savedsearch;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedSearchRepository extends JpaRepository<SavedSearch, UUID> {

  List<SavedSearch> findByUserIdOrderByCreatedAtDesc(UUID userId);

  Optional<SavedSearch> findByIdAndUserId(UUID id, UUID userId);
}
