package com.scholarmatch.api.bookmark.dto;

import com.scholarmatch.api.bookmark.Bookmark;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BookmarkResponse(UUID id, UUID userId, UUID scholarshipId, OffsetDateTime createdAt) {

  public static BookmarkResponse from(Bookmark bookmark) {
    return new BookmarkResponse(bookmark.getId(), bookmark.getUserId(), bookmark.getScholarshipId(), bookmark.getCreatedAt());
  }
}
