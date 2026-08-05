package com.scholarmatch.api.bookmark;

import com.scholarmatch.api.bookmark.dto.BookmarkResponse;
import com.scholarmatch.api.common.ApiException;
import com.scholarmatch.api.scholarship.Scholarship;
import com.scholarmatch.api.scholarship.ScholarshipRepository;
import com.scholarmatch.api.scholarship.dto.ScholarshipResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookmarkService {

  private final BookmarkRepository bookmarkRepository;
  private final ScholarshipRepository scholarshipRepository;

  public BookmarkService(BookmarkRepository bookmarkRepository, ScholarshipRepository scholarshipRepository) {
    this.bookmarkRepository = bookmarkRepository;
    this.scholarshipRepository = scholarshipRepository;
  }

  public List<ScholarshipResponse> list(UUID userId) {
    return bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(bookmark -> scholarshipRepository.findById(bookmark.getScholarshipId()))
        .flatMap(java.util.Optional::stream)
        .map(ScholarshipResponse::from)
        .toList();
  }

  @Transactional
  public BookmarkResponse add(UUID userId, UUID scholarshipId) {
    Scholarship scholarship = scholarshipRepository.findById(scholarshipId)
        .filter(Scholarship::isActive)
        .orElseThrow(() -> ApiException.notFound("Scholarship not found"));

    Bookmark bookmark = bookmarkRepository.findByUserIdAndScholarshipId(userId, scholarship.getId())
        .orElseGet(() -> bookmarkRepository.saveAndFlush(new Bookmark(userId, scholarship.getId())));
    return BookmarkResponse.from(bookmark);
  }

  @Transactional
  public void remove(UUID userId, UUID scholarshipId) {
    Bookmark bookmark = bookmarkRepository.findByUserIdAndScholarshipId(userId, scholarshipId)
        .orElseThrow(() -> ApiException.notFound("Bookmark not found"));
    bookmarkRepository.delete(bookmark);
  }
}
