package com.scholarmatch.api.bookmark;

import com.scholarmatch.api.auth.AuthPrincipal;
import com.scholarmatch.api.bookmark.dto.BookmarkResponse;
import com.scholarmatch.api.scholarship.dto.ScholarshipResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/bookmarks")
public class BookmarkController {

  private final BookmarkService bookmarkService;

  public BookmarkController(BookmarkService bookmarkService) {
    this.bookmarkService = bookmarkService;
  }

  @GetMapping
  public List<ScholarshipResponse> list(@AuthenticationPrincipal AuthPrincipal principal) {
    return bookmarkService.list(principal.id());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BookmarkResponse add(@AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody AddBookmarkRequest request) {
    return bookmarkService.add(principal.id(), request.scholarshipId());
  }

  @DeleteMapping("/{scholarshipId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID scholarshipId) {
    bookmarkService.remove(principal.id(), scholarshipId);
  }

  public record AddBookmarkRequest(@NotNull UUID scholarshipId) {
  }
}
