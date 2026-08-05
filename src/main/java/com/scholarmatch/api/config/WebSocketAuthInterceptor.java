package com.scholarmatch.api.config;

import com.scholarmatch.api.auth.AuthPrincipal;
import com.scholarmatch.api.auth.JwtService;
import com.scholarmatch.api.auth.RevokedTokenRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/** Authenticates the STOMP CONNECT frame using the JWT carried in its Authorization header. */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

  private final JwtService jwtService;
  private final RevokedTokenRepository revokedTokenRepository;

  public WebSocketAuthInterceptor(JwtService jwtService, RevokedTokenRepository revokedTokenRepository) {
    this.jwtService = jwtService;
    this.revokedTokenRepository = revokedTokenRepository;
  }

  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
      return message;
    }

    String header = accessor.getFirstNativeHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) {
      throw new IllegalArgumentException("Missing Authorization header");
    }

    AuthPrincipal principal;
    try {
      principal = jwtService.parse(header.substring(7));
    } catch (JwtService.InvalidTokenException e) {
      throw new IllegalArgumentException("Invalid or expired token", e);
    }
    if (revokedTokenRepository.existsByJti(principal.jti())) {
      throw new IllegalArgumentException("Token has been revoked");
    }

    List<GrantedAuthority> authorities = principal.roles().stream()
        .map(role -> new SimpleGrantedAuthority(role.authority()))
        .collect(Collectors.toList());
    accessor.setUser(new UsernamePasswordAuthenticationToken(principal, null, authorities));
    return message;
  }
}
