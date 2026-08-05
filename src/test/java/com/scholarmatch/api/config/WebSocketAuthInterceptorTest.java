package com.scholarmatch.api.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.scholarmatch.api.auth.AuthPrincipal;
import com.scholarmatch.api.auth.JwtService;
import com.scholarmatch.api.auth.RevokedTokenRepository;
import com.scholarmatch.api.common.Role;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Covers the STOMP CONNECT auth gate. A prior version of this interceptor mutated a throwaway
 * {@code StompHeaderAccessor.wrap(message)} instance instead of the message's own mutable
 * accessor, so the principal silently never attached and WebSocket pushes were dropped with no
 * error anywhere — this suite pins the current (working) behavior down so that regression can't
 * come back unnoticed.
 */
@ExtendWith(MockitoExtension.class)
class WebSocketAuthInterceptorTest {

  @Mock private JwtService jwtService;
  @Mock private RevokedTokenRepository revokedTokenRepository;

  private WebSocketAuthInterceptor interceptor;

  @BeforeEach
  void setUp() {
    interceptor = new WebSocketAuthInterceptor(jwtService, revokedTokenRepository);
  }

  private Message<byte[]> connectFrame(String authorizationHeader) {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
    if (authorizationHeader != null) {
      accessor.setNativeHeader("Authorization", authorizationHeader);
    }
    accessor.setLeaveMutable(true);
    return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
  }

  @Test
  void nonConnectFramesPassThroughWithoutAuthChecks() {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
    accessor.setLeaveMutable(true);
    Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

    Message<?> result = interceptor.preSend(message, null);

    assertThat(result).isSameAs(message);
  }

  @Test
  void missingAuthorizationHeaderIsRejected() {
    Message<byte[]> message = connectFrame(null);

    assertThatThrownBy(() -> interceptor.preSend(message, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Missing Authorization header");
  }

  @Test
  void nonBearerAuthorizationHeaderIsRejected() {
    Message<byte[]> message = connectFrame("Basic dXNlcjpwYXNz");

    assertThatThrownBy(() -> interceptor.preSend(message, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Missing Authorization header");
  }

  @Test
  void invalidTokenIsRejected() {
    when(jwtService.parse("bad-token")).thenThrow(new JwtService.InvalidTokenException("bad", new RuntimeException()));
    Message<byte[]> message = connectFrame("Bearer bad-token");

    assertThatThrownBy(() -> interceptor.preSend(message, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid or expired token");
  }

  @Test
  void revokedTokenIsRejected() {
    AuthPrincipal principal = new AuthPrincipal(
        UUID.randomUUID(), "user@example.com", List.of(Role.STUDENT), UUID.randomUUID(), Instant.now().plusSeconds(900));
    when(jwtService.parse("revoked-token")).thenReturn(principal);
    when(revokedTokenRepository.existsByJti(principal.jti())).thenReturn(true);
    Message<byte[]> message = connectFrame("Bearer revoked-token");

    assertThatThrownBy(() -> interceptor.preSend(message, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Token has been revoked");
  }

  @Test
  void validTokenAttachesAPrincipalNamedByUserId() {
    UUID userId = UUID.randomUUID();
    AuthPrincipal principal = new AuthPrincipal(
        userId, "user@example.com", List.of(Role.PROVIDER), UUID.randomUUID(), Instant.now().plusSeconds(900));
    when(jwtService.parse("good-token")).thenReturn(principal);
    when(revokedTokenRepository.existsByJti(principal.jti())).thenReturn(false);
    Message<byte[]> message = connectFrame("Bearer good-token");

    interceptor.preSend(message, null);

    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    assertThat(accessor.getUser()).isNotNull();
    // convertAndSendToUser(recipientId.toString(), ...) resolves against this name, so it must
    // be exactly the user's id, not the default Authentication#toString().
    assertThat(accessor.getUser().getName()).isEqualTo(userId.toString());
  }
}
