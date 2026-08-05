package com.scholarmatch.api.document;

import com.scholarmatch.api.auth.AuthPrincipal;
import com.scholarmatch.api.document.dto.DocumentResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/documents")
public class DocumentController {

  private final DocumentService documentService;

  public DocumentController(DocumentService documentService) {
    this.documentService = documentService;
  }

  @GetMapping
  public List<DocumentResponse> list(@AuthenticationPrincipal AuthPrincipal principal) {
    return documentService.list(principal.id());
  }

  @PostMapping(consumes = "multipart/form-data")
  @ResponseStatus(HttpStatus.CREATED)
  public DocumentResponse upload(
      @AuthenticationPrincipal AuthPrincipal principal,
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "doc_type", required = false) String docType) {
    return documentService.upload(principal.id(), file, docType);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
    documentService.delete(principal.id(), id);
  }
}
