package com.scholarmatch.api.profile;

import com.scholarmatch.api.auth.AuthPrincipal;
import com.scholarmatch.api.profile.dto.StudentProfileResponse;
import com.scholarmatch.api.profile.dto.StudentProfileUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student-profile")
@PreAuthorize("hasRole('STUDENT')")
public class StudentProfileController {

  private final StudentProfileService studentProfileService;

  public StudentProfileController(StudentProfileService studentProfileService) {
    this.studentProfileService = studentProfileService;
  }

  @GetMapping
  public StudentProfileResponse get(@AuthenticationPrincipal AuthPrincipal principal) {
    return studentProfileService.get(principal.id());
  }

  @PutMapping
  public StudentProfileResponse update(
      @AuthenticationPrincipal AuthPrincipal principal, @Valid @RequestBody StudentProfileUpdateRequest request) {
    return studentProfileService.upsert(principal.id(), request);
  }
}
