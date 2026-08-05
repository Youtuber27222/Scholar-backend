package com.scholarmatch.api.admin;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.scholarmatch.api.admin.dto.AcceptInviteRequest;
import com.scholarmatch.api.admin.dto.ProviderInviteRequest;
import com.scholarmatch.api.auth.JwtService;
import com.scholarmatch.api.auth.RefreshTokenService;
import com.scholarmatch.api.auth.User;
import com.scholarmatch.api.auth.UserRepository;
import com.scholarmatch.api.auth.UserRole;
import com.scholarmatch.api.auth.UserRoleRepository;
import com.scholarmatch.api.auth.dto.AuthResponse;
import com.scholarmatch.api.common.ApiException;
import com.scholarmatch.api.common.Role;
import com.scholarmatch.api.mail.MailService;
import com.scholarmatch.api.profile.ProfileRepository;
import com.scholarmatch.api.profile.ProviderProfileRepository;
import com.scholarmatch.api.scholarship.ScholarshipRepository;

/**
 * Covers provider onboarding: the invite/accept/revoke flow that replaced raw-SQL role grants.
 * These are the paths that create real accounts and grant real access, so the edge cases
 * (duplicate invites, expired tokens, revoking access) matter more than most.
 */
@ExtendWith(MockitoExtension.class)
class ProviderInviteServiceTest {

  @Mock private ProviderInviteRepository providerInviteRepository;
  @Mock private UserRepository userRepository;
  @Mock private UserRoleRepository userRoleRepository;
  @Mock private ProfileRepository profileRepository;
  @Mock private ProviderProfileRepository providerProfileRepository;
  @Mock private ScholarshipRepository scholarshipRepository;
  @Mock private RefreshTokenService refreshTokenService;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;
  @Mock private MailService mailService;

  private ProviderInviteService service;

  private final UUID adminId = UUID.randomUUID();

  @BeforeEach
  @SuppressWarnings("unused")
  void setUp() {
    service = new ProviderInviteService(
        providerInviteRepository, userRepository, userRoleRepository, profileRepository,
        providerProfileRepository, scholarshipRepository, refreshTokenService, passwordEncoder,
        jwtService, mailService, "scholarmatch");
  }

  // -- invite --------------------------------------------------------------

  @Test
  void inviteRejectsAnEmailThatAlreadyHasAnAccount() {
    when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

    assertThatThrownBy(() -> service.invite(adminId, new ProviderInviteRequest("taken@example.com", "Org")))
        .isInstanceOf(ApiException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    verify(providerInviteRepository, never()).save(any());
    verify(mailService, never()).sendProviderInviteEmail(anyString(), anyString(), anyString());
  }

  @Test
  void inviteRejectsWhenAPendingInviteAlreadyExists() {
    when(userRepository.existsByEmail("pending@example.com")).thenReturn(false);
    when(providerInviteRepository.existsByEmailAndAcceptedAtIsNull("pending@example.com")).thenReturn(true);

    assertThatThrownBy(() -> service.invite(adminId, new ProviderInviteRequest("pending@example.com", "Org")))
        .isInstanceOf(ApiException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    verify(providerInviteRepository, never()).save(any());
  }

  @Test
  void inviteSavesAndEmailsOnSuccess() {
    when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
    when(providerInviteRepository.existsByEmailAndAcceptedAtIsNull("new@example.com")).thenReturn(false);

    service.invite(adminId, new ProviderInviteRequest("new@example.com", "New Org"));

    verify(providerInviteRepository).save(any(ProviderInvite.class));
    verify(mailService).sendProviderInviteEmail(eq("new@example.com"), eq("New Org"), anyString());
  }

  // -- revoke ----------------------------------------------------------------

  @Test
  void revokeRejectsAUserWhoIsNotCurrentlyAProvider() {
    UUID userId = UUID.randomUUID();
    when(userRoleRepository.findByUserId(userId)).thenReturn(List.of(new UserRole(userId, Role.STUDENT)));

    assertThatThrownBy(() -> service.revoke(userId))
        .isInstanceOf(ApiException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    verify(userRoleRepository, never()).deleteByUserIdAndRole(any(), any());
    verify(refreshTokenService, never()).revokeAllForUser(any());
  }

  @Test
  void revokeDeletesRoleAndSignsOutEverywhere() {
    UUID userId = UUID.randomUUID();
    when(userRoleRepository.findByUserId(userId)).thenReturn(List.of(new UserRole(userId, Role.PROVIDER)));

    service.revoke(userId);

    verify(userRoleRepository).deleteByUserIdAndRole(userId, Role.PROVIDER);
    verify(refreshTokenService).revokeAllForUser(userId);
  }

  // -- acceptInvite ------------------------------------------------------

  @Test
  void acceptInviteRejectsAnExpiredOrConsumedToken() {
    ProviderInvite consumed = new ProviderInvite(
        "invited@example.com", "Org", adminId, "hash", OffsetDateTime.now().plusDays(7));
    consumed.setAcceptedAt(OffsetDateTime.now());
    when(providerInviteRepository.findByTokenHash(anyString())).thenReturn(Optional.of(consumed));

    assertThatThrownBy(() -> service.acceptInvite("raw-token", new AcceptInviteRequest("Password123!", "Jane Doe")))
        .isInstanceOf(ApiException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
    verify(userRepository, never()).save(any());
  }

  @Test
  void acceptInviteRejectsWhenAnAccountAlreadyExistsForThatEmail() {
    ProviderInvite invite = new ProviderInvite(
        "invited@example.com", "Org", adminId, "hash", OffsetDateTime.now().plusDays(7));
    when(providerInviteRepository.findByTokenHash(anyString())).thenReturn(Optional.of(invite));
    when(userRepository.existsByEmail("invited@example.com")).thenReturn(true);

    assertThatThrownBy(() -> service.acceptInvite("raw-token", new AcceptInviteRequest("Password123!", "Jane Doe")))
        .isInstanceOf(ApiException.class)
        .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    verify(userRepository, never()).save(any());
  }

  @Test
  void acceptInviteCreatesAProviderAccountAndReturnsATokenOnSuccess() {
    ProviderInvite invite = new ProviderInvite(
        "invited@example.com", "Org", adminId, "hash", OffsetDateTime.now().plusDays(7));
    when(providerInviteRepository.findByTokenHash(anyString())).thenReturn(Optional.of(invite));
    when(userRepository.existsByEmail("invited@example.com")).thenReturn(false);
    when(passwordEncoder.encode("Password123!")).thenReturn("hashed-password");

    User savedUser = new User("invited@example.com", "hashed-password");
    UUID newUserId = UUID.randomUUID();
    ReflectionTestUtils.setField(savedUser, "id", newUserId);
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    when(jwtService.issueAccessToken(eq(newUserId), eq("invited@example.com"), eq(List.of(Role.PROVIDER))))
        .thenReturn("access-token");
    when(refreshTokenService.issue(newUserId)).thenReturn("refresh-token");

    AuthResponse response = service.acceptInvite("raw-token", new AcceptInviteRequest("Password123!", "Jane Doe"));

    assertThat(response.accessToken()).isEqualTo("access-token");
    assertThat(response.refreshToken()).isEqualTo("refresh-token");
    assertThat(response.user().roles()).containsExactly(Role.PROVIDER);
    assertThat(response.user().emailVerified()).isTrue();
    verify(userRoleRepository).save(argThatUserRole(newUserId, Role.PROVIDER));
    verify(providerInviteRepository).save(invite);
    assertThat(invite.getAcceptedAt()).isNotNull();
  }

  private UserRole argThatUserRole(UUID userId, Role role) {
    return org.mockito.ArgumentMatchers.argThat(
        candidate -> candidate.getUserId().equals(userId) && candidate.getRole() == role);
  }
}
