package com.scholarmatch.api.message;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  List<Message> findByApplicationIdOrderByCreatedAtAsc(UUID applicationId);

  List<Message> findByApplicationIdInOrderByCreatedAtAsc(Collection<UUID> applicationIds);

  @Modifying
  @Query("UPDATE Message m SET m.readAt = CURRENT_TIMESTAMP "
      + "WHERE m.applicationId = :applicationId AND m.senderId <> :recipientId AND m.readAt IS NULL")
  int markThreadRead(@Param("applicationId") UUID applicationId, @Param("recipientId") UUID recipientId);

  @Query("SELECT COUNT(m) FROM Message m WHERE m.readAt IS NULL AND m.senderId <> :userId AND m.applicationId IN "
      + "(SELECT a.id FROM Application a WHERE a.studentId = :userId "
      + "OR a.scholarshipId IN (SELECT s.id FROM Scholarship s WHERE s.providerId = :userId))")
  int countUnreadForUser(@Param("userId") UUID userId);
}
