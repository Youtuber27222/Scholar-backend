package com.scholarmatch.api.savedsearch;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadlineReminderSentRepository extends JpaRepository<DeadlineReminderSent, UUID> {

  boolean existsByUserIdAndScholarshipIdAndThresholdDays(UUID userId, UUID scholarshipId, int thresholdDays);
}
