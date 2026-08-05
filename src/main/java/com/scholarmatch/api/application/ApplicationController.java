package com.scholarmatch.api.application;

import com.scholarmatch.api.application.dto.ApplicationCreateRequest;
import com.scholarmatch.api.application.dto.ApplicationResponse;
import com.scholarmatch.api.application.dto.ApplicationUpdateRequest;
import com.scholarmatch.api.auth.AuthPrincipal;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

  private final ApplicationService applicationService;

  public ApplicationController(ApplicationService applicationService) {
    this.applicationService = applicationService;
  }

  @GetMapping
  public List<ApplicationResponse> list(@AuthenticationPrincipal AuthPrincipal principal) {
    return applicationService.list(principal);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('STUDENT')")
  public ApplicationResponse create(
      @AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody ApplicationCreateRequest request) {
    return applicationService.create(principal.id(), request);
  }

  @GetMapping("/{id}")
  public ApplicationResponse get(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
    return applicationService.get(principal, id);
  }

  @PutMapping("/{id}")
  public ApplicationResponse update(
      @AuthenticationPrincipal AuthPrincipal principal,
      @PathVariable UUID id,
      @Valid @RequestBody ApplicationUpdateRequest request) {
    return applicationService.update(principal, id, request);
  }
}
