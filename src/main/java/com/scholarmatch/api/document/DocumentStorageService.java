package com.scholarmatch.api.document;

import com.scholarmatch.api.common.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentStorageService {

  private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

  private final Path uploadsRoot;

  public DocumentStorageService(@Value("${app.uploads.root}") String uploadsRoot) {
    this.uploadsRoot = Path.of(uploadsRoot).toAbsolutePath();
  }

  public StoredFile store(UUID userId, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw ApiException.badRequest("A document file is required");
    }
    if (file.getSize() > MAX_SIZE_BYTES) {
      throw ApiException.badRequest("File is too large");
    }

    try {
      Path userFolder = uploadsRoot.resolve(userId.toString());
      Files.createDirectories(userFolder);
      String extension = extensionOf(file.getOriginalFilename());
      String storedName = UUID.randomUUID() + extension;
      Path target = userFolder.resolve(storedName);
      try (InputStream input = file.getInputStream()) {
        Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
      }
      String relativePath = userId + "/" + storedName;
      return new StoredFile(relativePath, file.getOriginalFilename(), file.getContentType(), file.getSize());
    } catch (IOException e) {
      throw new java.io.UncheckedIOException("Failed to store document", e);
    }
  }

  private String extensionOf(String originalFilename) {
    if (originalFilename == null) {
      return "";
    }
    int dotIndex = originalFilename.lastIndexOf('.');
    return dotIndex >= 0 ? originalFilename.substring(dotIndex) : "";
  }

  public record StoredFile(String storagePath, String originalFilename, String contentType, long size) {
  }
}
