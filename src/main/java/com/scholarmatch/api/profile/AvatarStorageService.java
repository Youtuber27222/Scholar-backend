package com.scholarmatch.api.profile;

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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class AvatarStorageService {

  private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;

  private final Path avatarsRoot;

  public AvatarStorageService(@Value("${app.uploads.root}") String uploadsRoot) {
    this.avatarsRoot = Path.of(uploadsRoot, "avatars").toAbsolutePath();
  }

  public String store(UUID userId, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw ApiException.badRequest("An image file is required");
    }
    if (file.getSize() > MAX_SIZE_BYTES) {
      throw ApiException.badRequest("File is too large");
    }
    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw ApiException.badRequest("An image file is required");
    }

    try {
      Files.createDirectories(avatarsRoot);
      String extension = extensionOf(file.getOriginalFilename(), ".jpg");
      Path target = avatarsRoot.resolve(userId + extension);
      try (InputStream input = file.getInputStream()) {
        Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
      }
      String filename = target.getFileName().toString();
      return ServletUriComponentsBuilder.fromCurrentContextPath()
          .path("/uploads/avatars/" + filename)
          .queryParam("v", System.currentTimeMillis())
          .toUriString();
    } catch (IOException e) {
      throw new java.io.UncheckedIOException("Failed to store avatar", e);
    }
  }

  private String extensionOf(String originalFilename, String fallback) {
    if (originalFilename == null) {
      return fallback;
    }
    int dotIndex = originalFilename.lastIndexOf('.');
    return dotIndex >= 0 ? originalFilename.substring(dotIndex) : fallback;
  }
}
