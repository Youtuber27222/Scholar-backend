package com.scholarmatch.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarmatch.api.common.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Fixed-window IP rate limit on the auth endpoints most exposed to brute-force/enumeration —
 * login, registration, forgot-password, and the OTP/token-consuming endpoints (verify-email's
 * 6-digit code isn't scoped to an email, so it's guessable without a limit here). In-memory and
 * single-instance (matches the rest of this app's current scaling assumptions, e.g. the
 * WebSocket broker); swap for a shared store (Redis, etc.) before running more than one backend
 * instance behind a load balancer.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

  private record Rule(int limit, Duration window) {
  }

  private static final Map<String, Rule> RULES = Map.of(
      "/auth/login", new Rule(10, Duration.ofMinutes(5)),
      "/auth/register", new Rule(5, Duration.ofMinutes(15)),
      "/auth/forgot-password", new Rule(5, Duration.ofMinutes(15)),
      "/auth/verify-email", new Rule(10, Duration.ofMinutes(15)),
      "/auth/reset-password", new Rule(10, Duration.ofMinutes(15)));

  private static class Window {
    private final Instant start;
    private final AtomicInteger count;

    Window(Instant start) {
      this.start = start;
      this.count = new AtomicInteger(1);
    }
  }

  private final Map<String, Window> windows = new ConcurrentHashMap<>();
  private final ObjectMapper objectMapper;

  public RateLimitFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {
    Rule rule = "POST".equalsIgnoreCase(request.getMethod()) ? RULES.get(request.getServletPath()) : null;
    if (rule == null) {
      filterChain.doFilter(request, response);
      return;
    }

    String key = clientIp(request) + ":" + request.getServletPath();
    if (isOverLimit(key, rule)) {
      response.setStatus(429);
      response.setContentType("application/json");
      response.getWriter().write(objectMapper.writeValueAsString(
          ErrorResponse.of("Too many attempts. Please wait a few minutes and try again.")));
      return;
    }

    filterChain.doFilter(request, response);
  }

  private boolean isOverLimit(String key, Rule rule) {
    Instant now = Instant.now();
    Window window = windows.compute(key, (k, existing) -> {
      if (existing == null || existing.start.plus(rule.window()).isBefore(now)) {
        return new Window(now);
      }
      existing.count.incrementAndGet();
      return existing;
    });
    return window.count.get() > rule.limit();
  }

  private String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
