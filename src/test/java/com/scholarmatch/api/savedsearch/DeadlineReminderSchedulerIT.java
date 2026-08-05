package com.scholarmatch.api.savedsearch;

import static org.assertj.core.api.Assertions.assertThat;

import com.scholarmatch.api.auth.User;
import com.scholarmatch.api.auth.UserRepository;
import com.scholarmatch.api.bookmark.Bookmark;
import com.scholarmatch.api.bookmark.BookmarkRepository;
import com.scholarmatch.api.notification.NotificationRepository;
import com.scholarmatch.api.scholarship.Scholarship;
import com.scholarmatch.api.scholarship.ScholarshipRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = "app.jwt.secret=test-only-secret-at-least-32-characters-long")
class DeadlineReminderSchedulerIT {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired
  private DeadlineReminderScheduler scheduler;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ScholarshipRepository scholarshipRepository;

  @Autowired
  private BookmarkRepository bookmarkRepository;

  @Autowired
  private NotificationRepository notificationRepository;

  @Test
  void rerunningTheSameThresholdDoesNotDuplicateTheReminder() {
    User user = userRepository.save(new User("reminder-test@example.com", "hash"));
    Scholarship scholarship = scholarshipRepository.save(
        new Scholarship(null, "Test Provider", "Closing Soon Scholarship", LocalDate.now().plusDays(7)));
    bookmarkRepository.save(new Bookmark(user.getId(), scholarship.getId()));

    scheduler.run(7);
    assertThat(notificationRepository.findTop100ByUserIdOrderByCreatedAtDesc(user.getId())).hasSize(1);

    scheduler.run(7);
    assertThat(notificationRepository.findTop100ByUserIdOrderByCreatedAtDesc(user.getId()))
        .as("running the same threshold again must not send a second reminder")
        .hasSize(1);
  }

  @Test
  void onlyMatchesScholarshipsAtExactlyTheThreshold() {
    User user = userRepository.save(new User("reminder-test-2@example.com", "hash"));
    Scholarship notYetDue = scholarshipRepository.save(
        new Scholarship(null, "Test Provider", "Not due yet", LocalDate.now().plusDays(10)));
    bookmarkRepository.save(new Bookmark(user.getId(), notYetDue.getId()));

    scheduler.run(7);

    assertThat(notificationRepository.findTop100ByUserIdOrderByCreatedAtDesc(user.getId())).isEmpty();
  }
}
