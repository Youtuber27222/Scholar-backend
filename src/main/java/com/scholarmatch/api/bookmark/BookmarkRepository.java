package com.scholarmatch.api.bookmark;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, UUID> {

  List<Bookmark> findByUserIdOrderByCreatedAtDesc(UUID userId);

  Optional<Bookmark> findByUserIdAndScholarshipId(UUID userId, UUID scholarshipId);

  @Query("SELECT b.userId, b.scholarshipId, s.title FROM Bookmark b, Scholarship s "
      + "WHERE s.id = b.scholarshipId AND s.isActive = true AND s.deadline = :deadline")
  List<Object[]> findBookmarksWithDeadline(@Param("deadline") LocalDate deadline);
}
