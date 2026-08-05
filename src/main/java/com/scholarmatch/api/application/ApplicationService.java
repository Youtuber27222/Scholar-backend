package com.scholarmatch.api.application;

import com.scholarmatch.api.application.dto.ApplicationCreateRequest;
import com.scholarmatch.api.application.dto.ApplicationResponse;
import com.scholarmatch.api.application.dto.ApplicationUpdateRequest;
import com.scholarmatch.api.auth.AuthPrincipal;
import com.scholarmatch.api.common.ApiException;
import com.scholarmatch.api.common.Role;
import com.scholarmatch.api.notification.NotificationService;
import com.scholarmatch.api.scholarship.Scholarship;
import com.scholarmatch.api.scholarship.ScholarshipRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {

  private final ApplicationRepository applicationRepository;
  private final ScholarshipRepository scholarshipRepository;
  private final NotificationService notificationService;

  public ApplicationService(
      ApplicationRepository applicationRepository,
      ScholarshipRepository scholarshipRepository,
      NotificationService notificationService) {
    this.applicationRepository = applicationRepository;
    this.scholarshipRepository = scholarshipRepository;
    this.notificationService = notificationService;
  }

  public List<ApplicationResponse> list(AuthPrincipal principal) {
    List<Application> applications;
    if (principal.hasRole(Role.ADMIN)) {
      applications = applicationRepository.findAllByOrderByUpdatedAtDesc();
    } else if (principal.hasRole(Role.PROVIDER)) {
      applications = applicationRepository.findByScholarshipProviderId(principal.id());
    } else {
      applications = applicationRepository.findByStudentIdOrderByUpdatedAtDesc(principal.id());
    }
    return applications.stream().map(ApplicationResponse::from).toList();
  }

  public ApplicationResponse get(AuthPrincipal principal, UUID id) {
    Application application = applicationRepository.findById(id)
        .orElseThrow(() -> ApiException.notFound("Application not found"));
    Scholarship scholarship = scholarshipRepository.findById(application.getScholarshipId())
        .orElseThrow(() -> ApiException.notFound("Application not found"));

    boolean isOwner = application.getStudentId().equals(principal.id());
    boolean isProvider = principal.id().equals(scholarship.getProviderId());
    if (!isOwner && !isProvider && !principal.hasRole(Role.ADMIN)) {
      throw ApiException.notFound("Application not found");
    }
    return ApplicationResponse.from(application);
  }

  @Transactional
  public ApplicationResponse create(UUID studentId, ApplicationCreateRequest request) {
    Scholarship scholarship = scholarshipRepository.findById(request.scholarshipId())
        .filter(Scholarship::isActive)
        .orElseThrow(() -> ApiException.notFound("Scholarship not found"));

    Application application = new Application(studentId, scholarship.getId());
    application.setEssay(request.essay());
    application.setStatus(request.statusOrDefault());
    if (application.getStatus() == ApplicationStatus.SUBMITTED) {
      application.setSubmittedAt(OffsetDateTime.now());
    }

    try {
      return ApplicationResponse.from(applicationRepository.saveAndFlush(application));
    } catch (DataIntegrityViolationException e) {
      throw ApiException.conflict("You already have an application for this scholarship");
    }
  }

  @Transactional
  public ApplicationResponse update(AuthPrincipal principal, UUID id, ApplicationUpdateRequest request) {
    if (request.isEmpty()) {
      throw ApiException.badRequest("At least one field is required");
    }

    Application application = applicationRepository.findById(id)
        .orElseThrow(() -> ApiException.notFound("Application not found"));
    Scholarship scholarship = scholarshipRepository.findById(application.getScholarshipId())
        .orElseThrow(() -> ApiException.notFound("Application not found"));

    boolean isOwner = application.getStudentId().equals(principal.id());
    boolean isProvider = principal.id().equals(scholarship.getProviderId()) || principal.hasRole(Role.ADMIN);
    if (!isOwner && !isProvider) {
      throw ApiException.forbidden("You cannot update this application");
    }
    if (isOwner && request.providerNotes() != null) {
      throw ApiException.forbidden("Only providers may set provider notes");
    }
    if (isOwner && request.status() != null
        && request.status() != ApplicationStatus.DRAFTING && request.status() != ApplicationStatus.SUBMITTED) {
      throw ApiException.forbidden("Students may only draft or submit applications");
    }
    if (isProvider && request.essay() != null) {
      throw ApiException.forbidden("Providers may not edit the student's essay");
    }

    ApplicationStatus oldStatus = application.getStatus();
    ApplicationStatus effectiveStatus = request.status() != null ? request.status() : oldStatus;
    if (effectiveStatus == ApplicationStatus.SUBMITTED) {
      String finalEssay = request.essay() != null ? request.essay() : application.getEssay();
      if (finalEssay == null || finalEssay.isBlank()) {
        throw ApiException.badRequest("An essay is required before submitting your application.");
      }
    }

    if (request.status() != null) {
      application.setStatus(request.status());
    }
    if (request.essay() != null) {
      application.setEssay(request.essay());
    }
    if (request.providerNotes() != null) {
      application.setProviderNotes(request.providerNotes());
    }
    if (effectiveStatus == ApplicationStatus.SUBMITTED && application.getSubmittedAt() == null) {
      application.setSubmittedAt(OffsetDateTime.now());
    }

    Application saved = applicationRepository.saveAndFlush(application);

    if (isProvider && request.status() != null && request.status() != oldStatus
        && (request.status() == ApplicationStatus.AWARDED || request.status() == ApplicationStatus.REJECTED)) {
      notifyApplicationDecision(saved, scholarship.getTitle());
    }

    return ApplicationResponse.from(saved);
  }

  private void notifyApplicationDecision(Application application, String scholarshipTitle) {
    boolean awarded = application.getStatus() == ApplicationStatus.AWARDED;
    String title = awarded ? "Application awarded" : "Application update";
    String message = awarded
        ? "Congratulations! You were awarded \"" + scholarshipTitle + "\"."
        : "Your application for \"" + scholarshipTitle + "\" was not selected this time.";
    String type = awarded ? "application_awarded" : "application_rejected";
    notificationService.create(application.getStudentId(), application.getId(), type, title, message);
  }
}
