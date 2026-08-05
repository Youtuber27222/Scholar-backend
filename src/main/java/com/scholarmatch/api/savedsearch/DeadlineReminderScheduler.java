package com.scholarmatch.api.savedsearch;

import com.scholarmatch.api.bookmark.BookmarkRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Once daily, checks bookmarked scholarships hitting the 7/3/1-day-out deadline
 * thresholds and notifies each bookmarking user. Not itself @Transactional — see
 * DeadlineReminderRecorder for why each row gets its own transaction.
 */
@Component
public class DeadlineReminderScheduler {

  private static final List<Integer> THRESHOLD_DAYS = List.of(7, 3, 1);

  private final BookmarkRepository bookmarkRepository;
  private final DeadlineReminderRecorder deadlineReminderRecorder;

  public DeadlineReminderScheduler(BookmarkRepository bookmarkRepository, DeadlineReminderRecorder deadlineReminderRecorder) {
    this.bookmarkRepository = bookmarkRepository;
    this.deadlineReminderRecorder = deadlineReminderRecorder;
  }

  @Scheduled(cron = "0 0 8 * * *")
  public void sendDeadlineReminders() {
    for (int thresholdDays : THRESHOLD_DAYS) {
      run(thresholdDays);
    }
  }

  void run(int thresholdDays) {
    LocalDate targetDeadline = LocalDate.now().plusDays(thresholdDays);
    List<Object[]> rows = bookmarkRepository.findBookmarksWithDeadline(targetDeadline);
    for (Object[] row : rows) {
      UUID userId = (UUID) row[0];
      UUID scholarshipId = (UUID) row[1];
      String title = (String) row[2];
      deadlineReminderRecorder.recordIfNeeded(userId, scholarshipId, thresholdDays, title);
    }
  }
}
