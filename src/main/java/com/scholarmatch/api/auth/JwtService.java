package com.scholarmatch.api.auth;

import com.scholarmatch.api.common.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey signingKey;
  private final Duration accessTokenTtl;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.access-token-ttl-minutes}") long accessTokenTtlMinutes) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenTtl = Duration.ofMinutes(accessTokenTtlMinutes);
  }

  public String issueAccessToken(UUID userId, String email, List<Role> roles) {
    UUID jti = UUID.randomUUID();
    Instant now = Instant.now();
    List<String> roleValues = roles.stream().map(Role::getValue).collect(Collectors.toList());
    return Jwts.builder()
        .subject(userId.toString())
        .claim("email", email)
        .claim("roles", roleValues)
        .id(jti.toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(accessTokenTtl)))
        .signWith(signingKey)
        .compact();
  }

  public AuthPrincipal parse(String token) {
    try {
      Claims claims = Jwts.parser()
          .verifyWith(signingKey)
          .build()
          .parseSignedClaims(token)
          .getPayload();

      UUID userId = UUID.fromString(claims.getSubject());
      String email = claims.get("email", String.class);
      @SuppressWarnings("unchecked")
      List<String> roleValues = claims.get("roles", List.class);
      List<Role> roles = roleValues == null
          ? List.of()
          : roleValues.stream().map(Role::fromValue).collect(Collectors.toList());
      UUID jti = UUID.fromString(claims.getId());
      Instant expiresAt = claims.getExpiration().toInstant();
      return new AuthPrincipal(userId, email, roles, jti, expiresAt);
    } catch (JwtException | IllegalArgumentException e) {
      throw new InvalidTokenException("Invalid or expired token", e);
    }
  }

  public static class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
