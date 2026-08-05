package com.scholarmatch.api.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  public static final String AUTH_ERROR_ATTRIBUTE = "authError";

  private final JwtService jwtService;
  private final RevokedTokenRepository revokedTokenRepository;

  public JwtAuthFilter(JwtService jwtService, RevokedTokenRepository revokedTokenRepository) {
    this.jwtService = jwtService;
    this.revokedTokenRepository = revokedTokenRepository;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      AuthPrincipal principal = jwtService.parse(header.substring(7));
      if (revokedTokenRepository.existsByJti(principal.jti())) {
        request.setAttribute(AUTH_ERROR_ATTRIBUTE, "Token has been revoked");
        filterChain.doFilter(request, response);
        return;
      }

      List<GrantedAuthority> authorities = principal.roles().stream()
          .map(role -> new SimpleGrantedAuthority(role.authority()))
          .collect(Collectors.toList());
      var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (JwtService.InvalidTokenException e) {
      request.setAttribute(AUTH_ERROR_ATTRIBUTE, "Invalid or expired token");
    }

    filterChain.doFilter(request, response);
  }
}
