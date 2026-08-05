package com.scholarmatch.api.savedsearch;

import com.scholarmatch.api.notification.NotificationService;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs the exists-check, notification insert, and dedup-row insert for one
 * (user, scholarship, threshold) combination as a single atomic unit. Called
 * per-row from a non-transactional scheduler loop so one row's unique-constraint
 * violation can't poison the DB transaction for the rest of the batch.
 */
@Component
public class DeadlineReminderRecorder {

  private final DeadlineReminderSentRepository deadlineReminderSentRepository;
  private final NotificationService notificationService;

  public DeadlineReminderRecorder(
      DeadlineReminderSentRepository deadlineReminderSentRepository, NotificationService notificationService) {
    this.deadlineReminderSentRepository = deadlineReminderSentRepository;
    this.notificationService = notificationService;
  }

  @Transactional
  public void recordIfNeeded(UUID userId, UUID scholarshipId, int thresholdDays, String scholarshipTitle) {
    if (deadlineReminderSentRepository.existsByUserIdAndScholarshipIdAndThresholdDays(userId, scholarshipId, thresholdDays)) {
      return;
    }

    String dayWord = thresholdDays == 1 ? "day" : "days";
    String message = scholarshipTitle + " closes in " + thresholdDays + " " + dayWord + ".";
    notificationService.create(userId, null, "deadline_reminder", "Deadline approaching", message);

    try {
      deadlineReminderSentRepository.save(new DeadlineReminderSent(userId, scholarshipId, thresholdDays));
    } catch (DataIntegrityViolationException ignored) {
      // A concurrent run already recorded this reminder; the notification above
      // may be a rare duplicate, which is harmless compared to missing one entirely.
    }
  }
}
