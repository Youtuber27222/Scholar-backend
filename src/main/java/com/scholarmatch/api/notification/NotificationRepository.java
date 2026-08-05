package com.scholarmatch.api.notification;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  List<Notification> findTop100ByUserIdOrderByCreatedAtDesc(UUID userId);

  int countByUserIdAndReadAtIsNull(UUID userId);

  @Modifying
  @Query("UPDATE Notification n SET n.readAt = CURRENT_TIMESTAMP "
      + "WHERE n.id = :id AND n.userId = :userId AND n.readAt IS NULL")
  int markRead(@Param("id") UUID id, @Param("userId") UUID userId);

  @Modifying
  @Query("UPDATE Notification n SET n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.readAt IS NULL")
  void markAllRead(@Param("userId") UUID userId);

  boolean existsByIdAndUserId(UUID id, UUID userId);
}
