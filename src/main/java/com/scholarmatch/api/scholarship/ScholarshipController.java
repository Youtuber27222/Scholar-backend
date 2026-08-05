package com.scholarmatch.api.scholarship;

import com.scholarmatch.api.auth.AuthPrincipal;
import com.scholarmatch.api.common.Role;
import com.scholarmatch.api.scholarship.dto.ScholarshipCreateRequest;
import com.scholarmatch.api.scholarship.dto.ScholarshipResponse;
import com.scholarmatch.api.scholarship.dto.ScholarshipUpdateRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scholarships")
public class ScholarshipController {

  private final ScholarshipService scholarshipService;

  public ScholarshipController(ScholarshipService scholarshipService) {
    this.scholarshipService = scholarshipService;
  }

  @GetMapping
  public List<ScholarshipResponse> list(
      @RequestParam(required = false) String query,
      @RequestParam(required = false) String fieldOfStudy,
      @RequestParam(required = false) BigDecimal minGpa,
      @RequestParam(required = false) Boolean financialNeed,
      @AuthenticationPrincipal AuthPrincipal principal) {
    return scholarshipService.list(query, fieldOfStudy, minGpa, financialNeed, principal);
  }

  @GetMapping("/{id}")
  public ScholarshipResponse get(@PathVariable UUID id, @AuthenticationPrincipal AuthPrincipal principal) {
    return scholarshipService.get(id, principal);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAnyRole('PROVIDER','ADMIN')")
  public ScholarshipResponse create(
      @AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody ScholarshipCreateRequest request) {
    return scholarshipService.create(principal.id(), request);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('PROVIDER','ADMIN')")
  public ScholarshipResponse update(
      @AuthenticationPrincipal AuthPrincipal principal,
      @PathVariable UUID id,
      @Valid @RequestBody ScholarshipUpdateRequest request) {
    return scholarshipService.update(principal.id(), principal.hasRole(Role.ADMIN), id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAnyRole('PROVIDER','ADMIN')")
  public void delete(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable UUID id) {
    scholarshipService.delete(principal.id(), principal.hasRole(Role.ADMIN), id);
  }
}
