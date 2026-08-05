package com.scholarmatch.api.notification;

import com.scholarmatch.api.common.ApiException;
import com.scholarmatch.api.notification.dto.NotificationResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

  private final NotificationRepository notificationRepository;

  public NotificationService(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  public List<NotificationResponse> list(UUID userId) {
    return notificationRepository.findTop100ByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(NotificationResponse::from)
        .toList();
  }

  public int unreadCount(UUID userId) {
    return notificationRepository.countByUserIdAndReadAtIsNull(userId);
  }

  @Transactional
  public void markRead(UUID id, UUID userId) {
    int updated = notificationRepository.markRead(id, userId);
    if (updated == 0 && !notificationRepository.existsByIdAndUserId(id, userId)) {
      throw ApiException.notFound("Notification not found");
    }
  }

  @Transactional
  public void markAllRead(UUID userId) {
    notificationRepository.markAllRead(userId);
  }

  @Transactional
  public void create(UUID userId, UUID applicationId, String type, String title, String message) {
    notificationRepository.save(new Notification(userId, applicationId, type, title, message));
  }
}
