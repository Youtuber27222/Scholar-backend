package com.scholarmatch.api.notification.dto;

import com.scholarmatch.api.notification.Notification;
import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
    UUID id,
    UUID userId,
    UUID applicationId,
    String type,
    String title,
    String message,
    OffsetDateTime readAt,
    OffsetDateTime createdAt) {

  public static NotificationResponse from(Notification notification) {
    return new NotificationResponse(
        notification.getId(),
        notification.getUserId(),
        notification.getApplicationId(),
        notification.getType(),
        notification.getTitle(),
        notification.getMessage(),
        notification.getReadAt(),
        notification.getCreatedAt());
  }
}
