package com.scholarmatch.api.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.scholarmatch.api.application.Application;
import com.scholarmatch.api.application.ApplicationRepository;
import com.scholarmatch.api.application.ApplicationStatus;
import com.scholarmatch.api.auth.AuthPrincipal;
import com.scholarmatch.api.common.ApiException;
import com.scholarmatch.api.common.Role;
import com.scholarmatch.api.message.dto.MessageCreateRequest;
import com.scholarmatch.api.message.dto.MessageResponse;
import com.scholarmatch.api.notification.NotificationService;
import com.scholarmatch.api.profile.ProfileRepository;
import com.scholarmatch.api.scholarship.Scholarship;
import com.scholarmatch.api.scholarship.ScholarshipRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 * Covers the ownership/role-scoping rules that decide who may read, send, and mark read on a
 * message thread — the highest-risk logic in the messaging feature, previously verified only by
 * hand (curl + direct DB checks) during development.
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

  @Mock private MessageRepository messageRepository;
  @Mock private ApplicationRepository applicationRepository;
  @Mock private ScholarshipRepository scholarshipRepository;
  @Mock private ProfileRepository profileRepository;
  @Mock private NotificationService notificationService;
  @Mock private SimpMessagingTemplate messagingTemplate;

  private MessageService service;

  private final UUID studentId = UUID.randomUUID();
  private final UUID providerId = UUID.randomUUID();
  private final UUID outsiderId = UUID.randomUUID();
  private final UUID adminId = UUID.randomUUID();
  private final UUID applicationId = UUID.randomUUID();
  private final UUID scholarshipId = UUID.randomUUID();

  private Application application;
  private Scholarship scholarship;

  @BeforeEach
  void setUp() {
    service = new MessageService(
        messageRepository, applicationRepository, scholarshipRepository, profileRepository,
        notificationService, messagingTemplate);

    application = new Application(studentId, scholarshipId);
    application.setStatus(ApplicationStatus.SUBMITTED);

    scholarship = new Scholarship(providerId, "Provider Inc", "Test Scholarship", LocalDate.now().plusDays(30));

    when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
    when(scholarshipRepository.findById(scholarshipId)).thenReturn(Optional.of(scholarship));
  }

  private AuthPrincipal principal(UUID id, Role... roles) {
    return new AuthPrincipal(id, id + "@example.com", List.of(roles), UUID.randomUUID(), Instant.now().plusSeconds(900));
  }

  // -- listMessages --------------------------------------------------------

  @Test
  void ownerCanListMessages() {
    when(messageRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId)).thenReturn(List.of());
    assertThat(service.listMessages(principal(studentId, Role.STUDENT), applicationId)).isEmpty();
  }

  @Test
  void providerCanListMessages() {
    when(messageRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId)).thenReturn(List.of());
    assertThat(service.listMessages(principal(providerId, Role.PROVIDER), applicationId)).isEmpty();
  }

  @Test
  void adminCanListMessagesForOversight() {
    when(messageRepository.findByApplicationIdOrderByCreatedAtAsc(applicationId)).thenReturn(List.of());
    assertThat(service.listMessages(principal(adminId, Role.ADMIN), applicationId)).isEmpty();
  }

  @Test
  void unrelatedUserGetsNotFoundNotForbidden() {
    assertThatThrownBy(() -> service.listMessages(principal(outsiderId, Role.STUDENT), applicationId))
        .isInstanceOf(ApiException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
  }

  @Test
  void draftingApplicationRejectsRead() {
    application.setStatus(ApplicationStatus.DRAFTING);
    assertThatThrownBy(() -> service.listMessages(principal(studentId, Role.STUDENT), applicationId))
        .isInstanceOf(ApiException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
  }

  // -- send ------------------------------------------------------------

  @Test
  void studentSendingNotifiesAndPushesToProvider() {
    MessageCreateRequest request = new MessageCreateRequest("Hello provider");
    MessageResponse response = service.send(principal(studentId, Role.STUDENT), applicationId, request);

    assertThat(response.body()).isEqualTo("Hello provider");
    assertThat(response.senderId()).isEqualTo(studentId);
    verify(messageRepository).saveAndFlush(any(Message.class));
    verify(notificationService).create(eq(providerId), eq(applicationId), eq("new_message"), anyString(), anyString());
    verify(messagingTemplate).convertAndSendToUser(eq(providerId.toString()), eq("/queue/messages"), any());
  }

  @Test
  void providerSendingNotifiesAndPushesToStudent() {
    MessageCreateRequest request = new MessageCreateRequest("Hello student");
    service.send(principal(providerId, Role.PROVIDER), applicationId, request);

    verify(notificationService).create(eq(studentId), eq(applicationId), eq("new_message"), anyString(), anyString());
    verify(messagingTemplate).convertAndSendToUser(eq(studentId.toString()), eq("/queue/messages"), any());
  }

  @Test
  void unrelatedUserCannotSend() {
    MessageCreateRequest request = new MessageCreateRequest("I shouldn't be able to send this");
    assertThatThrownBy(() -> service.send(principal(outsiderId, Role.STUDENT), applicationId, request))
        .isInstanceOf(ApiException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    verify(messageRepository, never()).saveAndFlush(any());
  }

  @Test
  void adminCannotSendOnApplicationTheyAreNotPartyTo() {
    MessageCreateRequest request = new MessageCreateRequest("Admin trying to send");
    assertThatThrownBy(() -> service.send(principal(adminId, Role.ADMIN), applicationId, request))
        .isInstanceOf(ApiException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
    verify(messageRepository, never()).saveAndFlush(any());
  }

  @Test
  void sendingOnDraftingApplicationIsRejected() {
    application.setStatus(ApplicationStatus.DRAFTING);
    MessageCreateRequest request = new MessageCreateRequest("Too early");
    assertThatThrownBy(() -> service.send(principal(studentId, Role.STUDENT), applicationId, request))
        .isInstanceOf(ApiException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    verify(messageRepository, never()).saveAndFlush(any());
  }

  // -- markRead ----------------------------------------------------------

  @Test
  void ownerCanMarkThreadRead() {
    service.markRead(principal(studentId, Role.STUDENT), applicationId);
    verify(messageRepository, times(1)).markThreadRead(applicationId, studentId);
  }

  @Test
  void unrelatedUserCannotMarkThreadRead() {
    assertThatThrownBy(() -> service.markRead(principal(outsiderId, Role.STUDENT), applicationId))
        .isInstanceOf(ApiException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    verify(messageRepository, never()).markThreadRead(any(), any());
  }
}
