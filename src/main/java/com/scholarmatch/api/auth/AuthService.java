package com.scholarmatch.api.auth;

import com.scholarmatch.api.auth.dto.AuthResponse;
import com.scholarmatch.api.auth.dto.LoginRequest;
import com.scholarmatch.api.auth.dto.MeResponse;
import com.scholarmatch.api.auth.dto.RegisterRequest;
import com.scholarmatch.api.auth.dto.UserDto;
import com.scholarmatch.api.common.ApiException;
import com.scholarmatch.api.common.Role;
import com.scholarmatch.api.common.TokenHasher;
import com.scholarmatch.api.mail.MailService;
import com.scholarmatch.api.profile.Profile;
import com.scholarmatch.api.profile.ProfileRepository;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private static final Duration EMAIL_VERIFICATION_TTL = Duration.ofMinutes(10);
  private static final Duration PASSWORD_RESET_TTL = Duration.ofHours(1);
  private static final Duration RESEND_VERIFICATION_COOLDOWN = Duration.ofSeconds(60);

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final ProfileRepository profileRepository;
  private final RevokedTokenRepository revokedTokenRepository;
  private final EmailVerificationTokenRepository emailVerificationTokenRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final RefreshTokenService refreshTokenService;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final MailService mailService;
  private final String deepLinkScheme;

  public AuthService(
      UserRepository userRepository,
      UserRoleRepository userRoleRepository,
      ProfileRepository profileRepository,
      RevokedTokenRepository revokedTokenRepository,
      EmailVerificationTokenRepository emailVerificationTokenRepository,
      PasswordResetTokenRepository passwordResetTokenRepository,
      RefreshTokenService refreshTokenService,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      MailService mailService,
      @Value("${app.frontend.deep-link-scheme}") String deepLinkScheme) {
    this.userRepository = userRepository;
    this.userRoleRepository = userRoleRepository;
    this.profileRepository = profileRepository;
    this.revokedTokenRepository = revokedTokenRepository;
    this.emailVerificationTokenRepository = emailVerificationTokenRepository;
    this.passwordResetTokenRepository = passwordResetTokenRepository;
    this.refreshTokenService = refreshTokenService;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.mailService = mailService;
    this.deepLinkScheme = deepLinkScheme;
  }

  @Transactional
  public AuthResponse register(RegisterRequest request) {
    String email = request.email().toLowerCase();
    if (userRepository.existsByEmail(email)) {
      throw ApiException.conflict("An account with that email already exists");
    }

    User user;
    try {
      user = userRepository.save(new User(email, passwordEncoder.encode(request.password())));
    } catch (DataIntegrityViolationException e) {
      throw ApiException.conflict("An account with that email already exists");
    }

    profileRepository.save(new Profile(user.getId(), request.fullName().trim(), email, null));
    userRoleRepository.save(new UserRole(user.getId(), Role.STUDENT));
    issueAndSendVerificationEmail(user);

    return issueTokenFor(user.getId(), email, List.of(Role.STUDENT), false);
  }

  public AuthResponse login(LoginRequest request) {
    String email = request.email().toLowerCase();
    User user = userRepository.findByEmail(email)
        .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
        .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));

    List<Role> roles = rolesFor(user.getId());
    return issueTokenFor(user.getId(), user.getEmail(), roles, user.getEmailVerifiedAt() != null);
  }

  public MeResponse me(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> ApiException.unauthorized("Account no longer exists"));
    List<Role> roles = rolesFor(user.getId());
    return new MeResponse(new UserDto(user.getId(), user.getEmail(), roles, user.getEmailVerifiedAt() != null));
  }

  @Transactional
  public void logout(UUID jti, java.time.Instant expiresAt, String refreshToken) {
    if (!revokedTokenRepository.existsByJti(jti)) {
      try {
        revokedTokenRepository.save(new RevokedToken(jti, OffsetDateTime.ofInstant(expiresAt, java.time.ZoneOffset.UTC)));
      } catch (DataIntegrityViolationException ignored) {
        // Already revoked by a concurrent request; logout is idempotent.
      }
    }
    if (refreshToken != null && !refreshToken.isBlank()) {
      refreshTokenService.revoke(refreshToken);
    }
  }

  @Transactional
  public AuthResponse refresh(String refreshToken) {
    RefreshTokenService.Rotated rotated = refreshTokenService.rotate(refreshToken);
    User user = userRepository.findById(rotated.userId())
        .orElseThrow(() -> ApiException.unauthorized("Account no longer exists"));
    List<Role> roles = rolesFor(user.getId());
    String accessToken = jwtService.issueAccessToken(user.getId(), user.getEmail(), roles);
    UserDto userDto = new UserDto(user.getId(), user.getEmail(), roles, user.getEmailVerifiedAt() != null);
    return new AuthResponse(accessToken, rotated.newRawToken(), userDto);
  }

  @Transactional
  public void verifyEmail(String rawToken) {
    EmailVerificationToken token = emailVerificationTokenRepository.findByTokenHash(TokenHasher.sha256Hex(rawToken))
        .filter(EmailVerificationToken::isUsable)
        .orElseThrow(() -> ApiException.badRequest("That code is invalid or has expired"));

    User user = userRepository.findById(token.getUserId())
        .orElseThrow(() -> ApiException.badRequest("That code is invalid or has expired"));
    user.setEmailVerifiedAt(OffsetDateTime.now());
    userRepository.save(user);

    token.setConsumedAt(OffsetDateTime.now());
    emailVerificationTokenRepository.save(token);
  }

  @Transactional
  public void resendVerification(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> ApiException.unauthorized("Account no longer exists"));
    if (user.getEmailVerifiedAt() != null) {
      throw ApiException.badRequest("Your email is already verified");
    }

    emailVerificationTokenRepository.findFirstByUserIdOrderByCreatedAtDesc(userId).ifPresent(latest -> {
      if (latest.getCreatedAt().isAfter(OffsetDateTime.now().minus(RESEND_VERIFICATION_COOLDOWN))) {
        throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Please wait a moment before requesting another email");
      }
    });

    issueAndSendVerificationEmail(user);
  }

  @Transactional
  public void forgotPassword(String rawEmail) {
    String email = rawEmail.toLowerCase();
    userRepository.findByEmail(email).ifPresent(user -> {
      String raw = TokenHasher.generateRawToken();
      passwordResetTokenRepository.save(
          new PasswordResetToken(user.getId(), TokenHasher.sha256Hex(raw), OffsetDateTime.now().plus(PASSWORD_RESET_TTL)));
      String link = deepLinkScheme + "://reset-password?token=" + raw;
      mailService.sendPasswordResetEmail(user.getEmail(), link);
    });
    // Always succeeds with no indication of whether the email exists, to prevent account enumeration.
  }

  @Transactional
  public void resetPassword(String rawToken, String newPassword) {
    PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(TokenHasher.sha256Hex(rawToken))
        .filter(PasswordResetToken::isUsable)
        .orElseThrow(() -> ApiException.badRequest("Invalid or expired reset link"));

    User user = userRepository.findById(token.getUserId())
        .orElseThrow(() -> ApiException.badRequest("Invalid or expired reset link"));
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);

    token.setConsumedAt(OffsetDateTime.now());
    passwordResetTokenRepository.save(token);

    refreshTokenService.revokeAllForUser(user.getId());
  }

  private void issueAndSendVerificationEmail(User user) {
    String otp = TokenHasher.generateOtp();
    emailVerificationTokenRepository.save(
        new EmailVerificationToken(user.getId(), TokenHasher.sha256Hex(otp), OffsetDateTime.now().plus(EMAIL_VERIFICATION_TTL)));
    mailService.sendVerificationEmail(user.getEmail(), otp);
  }

  private List<Role> rolesFor(UUID userId) {
    return userRoleRepository.findByUserId(userId).stream()
        .map(UserRole::getRole)
        .collect(Collectors.toList());
  }

  private AuthResponse issueTokenFor(UUID userId, String email, List<Role> roles, boolean emailVerified) {
    String accessToken = jwtService.issueAccessToken(userId, email, roles);
    String refreshToken = refreshTokenService.issue(userId);
    return new AuthResponse(accessToken, refreshToken, new UserDto(userId, email, roles, emailVerified));
  }
}
