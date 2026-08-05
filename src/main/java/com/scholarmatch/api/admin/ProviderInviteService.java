package com.scholarmatch.api.admin;

import com.scholarmatch.api.admin.dto.AcceptInviteRequest;
import com.scholarmatch.api.admin.dto.AdminProviderSummaryResponse;
import com.scholarmatch.api.admin.dto.InvitePreviewResponse;
import com.scholarmatch.api.admin.dto.ProviderInviteRequest;
import com.scholarmatch.api.auth.JwtService;
import com.scholarmatch.api.auth.RefreshTokenService;
import com.scholarmatch.api.auth.User;
import com.scholarmatch.api.auth.UserRepository;
import com.scholarmatch.api.auth.UserRole;
import com.scholarmatch.api.auth.UserRoleRepository;
import com.scholarmatch.api.auth.dto.AuthResponse;
import com.scholarmatch.api.auth.dto.UserDto;
import com.scholarmatch.api.common.ApiException;
import com.scholarmatch.api.common.Role;
import com.scholarmatch.api.common.TokenHasher;
import com.scholarmatch.api.mail.MailService;
import com.scholarmatch.api.profile.Profile;
import com.scholarmatch.api.profile.ProfileRepository;
import com.scholarmatch.api.profile.ProviderProfile;
import com.scholarmatch.api.profile.ProviderProfileRepository;
import com.scholarmatch.api.scholarship.ScholarshipRepository;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProviderInviteService {

  private static final Duration INVITE_TTL = Duration.ofDays(7);

  private final ProviderInviteRepository providerInviteRepository;
  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final ProfileRepository profileRepository;
  private final ProviderProfileRepository providerProfileRepository;
  private final ScholarshipRepository scholarshipRepository;
  private final RefreshTokenService refreshTokenService;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final MailService mailService;
  private final String deepLinkScheme;

  public ProviderInviteService(
      ProviderInviteRepository providerInviteRepository,
      UserRepository userRepository,
      UserRoleRepository userRoleRepository,
      ProfileRepository profileRepository,
      ProviderProfileRepository providerProfileRepository,
      ScholarshipRepository scholarshipRepository,
      RefreshTokenService refreshTokenService,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      MailService mailService,
      @Value("${app.frontend.deep-link-scheme}") String deepLinkScheme) {
    this.providerInviteRepository = providerInviteRepository;
    this.userRepository = userRepository;
    this.userRoleRepository = userRoleRepository;
    this.profileRepository = profileRepository;
    this.providerProfileRepository = providerProfileRepository;
    this.scholarshipRepository = scholarshipRepository;
    this.refreshTokenService = refreshTokenService;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.mailService = mailService;
    this.deepLinkScheme = deepLinkScheme;
  }

  @Transactional
  public void invite(UUID adminId, ProviderInviteRequest request) {
    String email = request.email().toLowerCase();
    if (userRepository.existsByEmail(email)) {
      throw ApiException.conflict("An account with that email already exists");
    }
    if (providerInviteRepository.existsByEmailAndAcceptedAtIsNull(email)) {
      throw ApiException.conflict("There is already a pending invite for that email");
    }

    String raw = TokenHasher.generateRawToken();
    providerInviteRepository.save(new ProviderInvite(
        email, request.organizationName(), adminId, TokenHasher.sha256Hex(raw), OffsetDateTime.now().plus(INVITE_TTL)));

    String link = deepLinkScheme + "://accept-invite?token=" + raw;
    mailService.sendProviderInviteEmail(email, request.organizationName(), link);
  }

  public List<AdminProviderSummaryResponse> listProviders() {
    List<UUID> providerIds = userRoleRepository.findByRole(Role.PROVIDER).stream()
        .map(UserRole::getUserId)
        .toList();
    if (providerIds.isEmpty()) {
      return List.of();
    }
    return summarize(providerIds);
  }

  public AdminProviderSummaryResponse getProvider(UUID userId) {
    boolean isProvider = userRoleRepository.findByUserId(userId).stream()
        .anyMatch(role -> role.getRole() == Role.PROVIDER);
    if (!isProvider) {
      throw ApiException.notFound("Provider not found");
    }
    return summarize(List.of(userId)).stream().findFirst()
        .orElseThrow(() -> ApiException.notFound("Provider not found"));
  }

  @Transactional
  public void revoke(UUID userId) {
    boolean isProvider = userRoleRepository.findByUserId(userId).stream()
        .anyMatch(role -> role.getRole() == Role.PROVIDER);
    if (!isProvider) {
      throw ApiException.notFound("Provider not found");
    }
    userRoleRepository.deleteByUserIdAndRole(userId, Role.PROVIDER);
    refreshTokenService.revokeAllForUser(userId);
  }

  public InvitePreviewResponse previewInvite(String rawToken) {
    ProviderInvite invite = providerInviteRepository.findByTokenHash(TokenHasher.sha256Hex(rawToken))
        .filter(ProviderInvite::isUsable)
        .orElseThrow(() -> ApiException.badRequest("This invite link is invalid or has expired"));
    return InvitePreviewResponse.from(invite);
  }

  @Transactional
  public AuthResponse acceptInvite(String rawToken, AcceptInviteRequest request) {
    ProviderInvite invite = providerInviteRepository.findByTokenHash(TokenHasher.sha256Hex(rawToken))
        .filter(ProviderInvite::isUsable)
        .orElseThrow(() -> ApiException.badRequest("This invite link is invalid or has expired"));

    if (userRepository.existsByEmail(invite.getEmail())) {
      throw ApiException.conflict("An account with that email already exists. Sign in instead.");
    }

    User user = userRepository.save(new User(invite.getEmail(), passwordEncoder.encode(request.password())));
    user.setEmailVerifiedAt(OffsetDateTime.now());
    userRepository.save(user);

    profileRepository.save(new Profile(user.getId(), request.fullName().trim(), invite.getEmail(), null));
    userRoleRepository.save(new UserRole(user.getId(), Role.PROVIDER));

    ProviderProfile providerProfile = new ProviderProfile(user.getId());
    providerProfile.setOrganizationName(invite.getOrganizationName());
    providerProfileRepository.save(providerProfile);

    invite.setAcceptedAt(OffsetDateTime.now());
    providerInviteRepository.save(invite);

    List<Role> roles = List.of(Role.PROVIDER);
    String accessToken = jwtService.issueAccessToken(user.getId(), user.getEmail(), roles);
    String refreshToken = refreshTokenService.issue(user.getId());
    return new AuthResponse(accessToken, refreshToken, new UserDto(user.getId(), user.getEmail(), roles, true));
  }

  private List<AdminProviderSummaryResponse> summarize(Collection<UUID> providerIds) {
    Map<UUID, Profile> profilesById = profileRepository.findAllById(providerIds).stream()
        .collect(Collectors.toMap(Profile::getId, p -> p));
    Map<UUID, ProviderProfile> providerProfilesByUserId = providerProfileRepository.findByUserIdIn(providerIds).stream()
        .collect(Collectors.toMap(ProviderProfile::getUserId, p -> p));
    Map<UUID, Long> scholarshipCounts = scholarshipRepository.countByProviderIdIn(providerIds).stream()
        .collect(Collectors.toMap(row -> (UUID) row[0], row -> (Long) row[1]));

    return providerIds.stream()
        .map(id -> {
          Profile profile = profilesById.get(id);
          ProviderProfile providerProfile = providerProfilesByUserId.get(id);
          return new AdminProviderSummaryResponse(
              id,
              profile != null ? profile.getEmail() : null,
              profile != null ? profile.getFullName() : null,
              providerProfile != null ? providerProfile.getOrganizationName() : null,
              providerProfile != null ? providerProfile.getOrganizationDescription() : null,
              providerProfile != null ? providerProfile.getWebsite() : null,
              scholarshipCounts.getOrDefault(id, 0L),
              profile != null ? profile.getCreatedAt() : null);
        })
        .toList();
  }
}
