package com.scholarmatch.api.document.dto;

import com.scholarmatch.api.document.Document;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentResponse(
    UUID id,
    UUID userId,
    String fileName,
    String fileType,
    String docType,
    String storagePath,
    Long fileSize,
    OffsetDateTime uploadedAt) {

  public static DocumentResponse from(Document document) {
    return new DocumentResponse(
        document.getId(),
        document.getUserId(),
        document.getFileName(),
        document.getFileType(),
        document.getDocType(),
        document.getStoragePath(),
        document.getFileSize(),
        document.getUploadedAt());
  }
}
