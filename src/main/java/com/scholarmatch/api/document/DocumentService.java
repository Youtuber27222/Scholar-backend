package com.scholarmatch.api.document;

import com.scholarmatch.api.common.ApiException;
import com.scholarmatch.api.document.dto.DocumentResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

  private final DocumentRepository documentRepository;
  private final DocumentStorageService documentStorageService;

  public DocumentService(DocumentRepository documentRepository, DocumentStorageService documentStorageService) {
    this.documentRepository = documentRepository;
    this.documentStorageService = documentStorageService;
  }

  public List<DocumentResponse> list(UUID userId) {
    return documentRepository.findByUserIdOrderByUploadedAtDesc(userId).stream()
        .map(DocumentResponse::from)
        .toList();
  }

  @Transactional
  public DocumentResponse upload(UUID userId, MultipartFile file, String docType) {
    DocumentStorageService.StoredFile stored = documentStorageService.store(userId, file);
    Document document = new Document(
        userId, stored.originalFilename(), stored.contentType(), docType, stored.storagePath(), stored.size());
    return DocumentResponse.from(documentRepository.saveAndFlush(document));
  }

  @Transactional
  public void delete(UUID userId, UUID id) {
    Document document = documentRepository.findById(id)
        .filter(candidate -> candidate.getUserId().equals(userId))
        .orElseThrow(() -> ApiException.notFound("Document not found"));
    documentRepository.delete(document);
  }
}
