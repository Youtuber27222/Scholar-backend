package com.scholarmatch.api.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "documents")
public class Document {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "file_type")
  private String fileType;

  @Column(name = "doc_type")
  private String docType;

  @Column(name = "storage_path", nullable = false)
  private String storagePath;

  @Column(name = "file_size")
  private Long fileSize;

  @CreationTimestamp
  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private OffsetDateTime uploadedAt;

  protected Document() {
  }

  public Document(UUID userId, String fileName, String fileType, String docType, String storagePath, Long fileSize) {
    this.userId = userId;
    this.fileName = fileName;
    this.fileType = fileType;
    this.docType = docType;
    this.storagePath = storagePath;
    this.fileSize = fileSize;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getFileName() {
    return fileName;
  }

  public String getFileType() {
    return fileType;
  }

  public String getDocType() {
    return docType;
  }

  public String getStoragePath() {
    return storagePath;
  }

  public Long getFileSize() {
    return fileSize;
  }

  public OffsetDateTime getUploadedAt() {
    return uploadedAt;
  }
}
