package com.scholarmatch.api.message;

import com.scholarmatch.api.application.Application;
import com.scholarmatch.api.application.ApplicationRepository;
import com.scholarmatch.api.application.ApplicationStatus;
import com.scholarmatch.api.auth.AuthPrincipal;
import com.scholarmatch.api.common.ApiException;
import com.scholarmatch.api.common.Role;
import com.scholarmatch.api.message.dto.MessageCreateRequest;
import com.scholarmatch.api.message.dto.MessageResponse;
import com.scholarmatch.api.message.dto.ThreadSummaryResponse;
import com.scholarmatch.api.notification.NotificationService;
import com.scholarmatch.api.profile.Profile;
import com.scholarmatch.api.profile.ProfileRepository;
import com.scholarmatch.api.scholarship.Scholarship;
import com.scholarmatch.api.scholarship.ScholarshipRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

  private static final int PREVIEW_LENGTH = 140;

  private final MessageRepository messageRepository;
  private final ApplicationRepository applicationRepository;
  private final ScholarshipRepository scholarshipRepository;
  private final ProfileRepository profileRepository;
  private final NotificationService notificationService;
  private final SimpMessagingTemplate messagingTemplate;

  public MessageService(
      MessageRepository messageRepository,
      ApplicationRepository applicationRepository,
      ScholarshipRepository scholarshipRepository,
      ProfileRepository profileRepository,
      NotificationService notificationService,
      SimpMessagingTemplate messagingTemplate) {
    this.messageRepository = messageRepository;
    this.applicationRepository = applicationRepository;
    this.scholarshipRepository = scholarshipRepository;
    this.profileRepository = profileRepository;
    this.notificationService = notificationService;
    this.messagingTemplate = messagingTemplate;
  }

  public List<ThreadSummaryResponse> listThreads(AuthPrincipal principal) {
    List<Application> applications = scopedApplications(principal).stream()
        .filter(application -> application.getStatus() != ApplicationStatus.DRAFTING)
        .toList();
    if (applications.isEmpty()) {
      return List.of();
    }

    List<UUID> scholarshipIds = applications.stream().map(Application::getScholarshipId).distinct().toList();
    Map<UUID, Scholarship> scholarshipsById = new HashMap<>();
    scholarshipRepository.findAllById(scholarshipIds).forEach(s -> scholarshipsById.put(s.getId(), s));

    Map<UUID, UUID> counterpartByApplication = new HashMap<>();
    for (Application application : applications) {
      Scholarship scholarship = scholarshipsById.get(application.getScholarshipId());
      UUID counterpartId = principal.id().equals(application.getStudentId())
          ? (scholarship != null ? scholarship.getProviderId() : null)
          : application.getStudentId();
      counterpartByApplication.put(application.getId(), counterpartId);
    }

    List<UUID> counterpartIds = counterpartByApplication.values().stream()
        .filter(Objects::nonNull)
        .distinct()
        .toList();
    Map<UUID, Profile> profilesById = new HashMap<>();
    profileRepository.findAllById(counterpartIds).forEach(p -> profilesById.put(p.getId(), p));

    List<UUID> applicationIds = applications.stream().map(Application::getId).toList();
    Map<UUID, List<Message>> messagesByApplication = new HashMap<>();
    messageRepository.findByApplicationIdInOrderByCreatedAtAsc(applicationIds)
        .forEach(m -> messagesByApplication.computeIfAbsent(m.getApplicationId(), k -> new ArrayList<>()).add(m));

    List<ThreadSummaryResponse> threads = applications.stream().map(application -> {
      Scholarship scholarship = scholarshipsById.get(application.getScholarshipId());
      UUID counterpartId = counterpartByApplication.get(application.getId());
      Profile counterpartProfile = counterpartId != null ? profilesById.get(counterpartId) : null;
      List<Message> thread = messagesByApplication.getOrDefault(application.getId(), List.of());
      Message last = thread.isEmpty() ? null : thread.get(thread.size() - 1);
      int unread = (int) thread.stream()
          .filter(m -> m.getReadAt() == null && !m.getSenderId().equals(principal.id()))
          .count();

      return new ThreadSummaryResponse(
          application.getId(),
          application.getScholarshipId(),
          scholarship != null ? scholarship.getTitle() : "Scholarship",
          application.getStatus(),
          counterpartId,
          counterpartProfile != null ? counterpartProfile.getFullName() : "Unknown",
          last != null ? preview(last.getBody()) : null,
          last != null ? last.getCreatedAt() : null,
          unread);
    }).sorted(Comparator.comparing(
            (ThreadSummaryResponse t) -> t.lastMessageAt() != null ? t.lastMessageAt() : OffsetDateTime.MIN)
        .reversed())
        .toList();

    return threads;
  }

  public int unreadCount(UUID userId) {
    return messageRepository.countUnreadForUser(userId);
  }

  public List<MessageResponse> listMessages(AuthPrincipal principal, UUID applicationId) {
    Application application = requireApplication(applicationId);
    Scholarship scholarship = requireScholarship(application);

    boolean isOwner = application.getStudentId().equals(principal.id());
    boolean isProvider = principal.id().equals(scholarship.getProviderId());
    if (!isOwner && !isProvider && !principal.hasRole(Role.ADMIN)) {
      throw ApiException.notFound("Application not found");
    }
    requireSubmitted(application);

    return messageRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId).stream()
        .map(MessageResponse::from)
        .toList();
  }

  @Transactional
  public MessageResponse send(AuthPrincipal principal, UUID applicationId, MessageCreateRequest request) {
    Application application = requireApplication(applicationId);
    Scholarship scholarship = requireScholarship(application);

    boolean isOwner = application.getStudentId().equals(principal.id());
    boolean isProvider = principal.id().equals(scholarship.getProviderId());
    if (!isOwner && !isProvider) {
      if (principal.hasRole(Role.ADMIN)) {
        throw ApiException.forbidden("Admins cannot send messages on an application they are not party to");
      }
      throw ApiException.notFound("Application not found");
    }
    requireSubmitted(application);

    Message message = new Message(applicationId, principal.id(), request.body());
    messageRepository.saveAndFlush(message);
    MessageResponse response = MessageResponse.from(message);

    UUID recipientId = isOwner ? scholarship.getProviderId() : application.getStudentId();
    if (recipientId != null) {
      notificationService.create(
          recipientId, applicationId, "new_message", "New message", preview(request.body()));
      messagingTemplate.convertAndSendToUser(recipientId.toString(), "/queue/messages", response);
    }

    return response;
  }

  @Transactional
  public void markRead(AuthPrincipal principal, UUID applicationId) {
    Application application = requireApplication(applicationId);
    Scholarship scholarship = requireScholarship(application);

    boolean isOwner = application.getStudentId().equals(principal.id());
    boolean isProvider = principal.id().equals(scholarship.getProviderId());
    if (!isOwner && !isProvider) {
      throw ApiException.notFound("Application not found");
    }
    requireSubmitted(application);

    messageRepository.markThreadRead(applicationId, principal.id());
  }

  private List<Application> scopedApplications(AuthPrincipal principal) {
    if (principal.hasRole(Role.ADMIN)) {
      return applicationRepository.findAllByOrderByUpdatedAtDesc();
    }
    if (principal.hasRole(Role.PROVIDER)) {
      return applicationRepository.findByScholarshipProviderId(principal.id());
    }
    return applicationRepository.findByStudentIdOrderByUpdatedAtDesc(principal.id());
  }

  private Application requireApplication(UUID applicationId) {
    return applicationRepository.findById(applicationId)
        .orElseThrow(() -> ApiException.notFound("Application not found"));
  }

  private Scholarship requireScholarship(Application application) {
    return scholarshipRepository.findById(application.getScholarshipId())
        .orElseThrow(() -> ApiException.notFound("Application not found"));
  }

  private void requireSubmitted(Application application) {
    if (application.getStatus() == ApplicationStatus.DRAFTING) {
      throw ApiException.badRequest("This application hasn't been submitted yet");
    }
  }

  private String preview(String body) {
    String trimmed = body.strip();
    return trimmed.length() > PREVIEW_LENGTH ? trimmed.substring(0, PREVIEW_LENGTH) + "…" : trimmed;
  }
}
