package com.scholarmatch.api.auth;

import com.scholarmatch.api.auth.dto.AuthResponse;
import com.scholarmatch.api.auth.dto.ForgotPasswordRequest;
import com.scholarmatch.api.auth.dto.LoginRequest;
import com.scholarmatch.api.auth.dto.LogoutRequest;
import com.scholarmatch.api.auth.dto.MeResponse;
import com.scholarmatch.api.auth.dto.RefreshRequest;
import com.scholarmatch.api.auth.dto.RegisterRequest;
import com.scholarmatch.api.auth.dto.ResetPasswordRequest;
import com.scholarmatch.api.auth.dto.VerifyEmailRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@AuthenticationPrincipal AuthPrincipal principal, @RequestBody(required = false) LogoutRequest request) {
    String refreshToken = request == null ? null : request.refreshToken();
    authService.logout(principal.jti(), principal.expiresAt(), refreshToken);
  }

  @GetMapping("/me")
  public MeResponse me(@AuthenticationPrincipal AuthPrincipal principal) {
    return authService.me(principal.id());
  }

  @PostMapping("/refresh")
  public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
    return authService.refresh(request.refreshToken());
  }

  @PostMapping("/verify-email")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
    authService.verifyEmail(request.token());
  }

  @PostMapping("/resend-verification")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void resendVerification(@AuthenticationPrincipal AuthPrincipal principal) {
    authService.resendVerification(principal.id());
  }

  @PostMapping("/forgot-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    authService.forgotPassword(request.email());
  }

  @PostMapping("/reset-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    authService.resetPassword(request.token(), request.newPassword());
  }
}
