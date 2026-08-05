package com.scholarmatch.api.message.dto;

import com.scholarmatch.api.message.Message;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageResponse(
    UUID id,
    UUID applicationId,
    UUID senderId,
    String body,
    OffsetDateTime readAt,
    OffsetDateTime createdAt) {

  public static MessageResponse from(Message message) {
    return new MessageResponse(
        message.getId(),
        message.getApplicationId(),
        message.getSenderId(),
        message.getBody(),
        message.getReadAt(),
        message.getCreatedAt());
  }
}
