package com.scholarmatch.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarmatch.api.auth.JwtAuthFilter;
import com.scholarmatch.api.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
      throws java.io.IOException {
    Object attribute = request.getAttribute(JwtAuthFilter.AUTH_ERROR_ATTRIBUTE);
    String message = attribute != null ? attribute.toString() : "Authentication required";
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getWriter(), ErrorResponse.of(message));
  }
}
