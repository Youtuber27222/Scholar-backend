package com.scholarmatch.api.mail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarmatch.api.common.ApiException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.mode", havingValue = "sendgrid")
public class SendGridMailService implements MailService {

  private static final Logger log = LoggerFactory.getLogger(SendGridMailService.class);

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final String apiKey;
  private final String apiUrl;
  private final String fromAddress;

  public SendGridMailService(
      ObjectMapper objectMapper,
      @Value("${app.mail.sendgrid.api-key}") String apiKey,
      @Value("${app.mail.sendgrid.api-url}") String apiUrl,
      @Value("${app.mail.from}") String fromAddress) {
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = objectMapper;
    this.apiKey = apiKey;
    this.apiUrl = apiUrl;
    this.fromAddress = fromAddress;
  }

  @Override
  public void sendVerificationEmail(String toEmail, String otp) {
    send(toEmail, "Your ScholarMatch verification code",
        "Welcome to ScholarMatch! Enter this code in the app to verify your email:\n\n" + otp
            + "\n\nThis code expires in 10 minutes.");
  }

  @Override
  public void sendPasswordResetEmail(String toEmail, String resetLink) {
    send(toEmail, "Reset your ScholarMatch password",
        "Reset your password by opening this link:\n\n" + resetLink
            + "\n\nIf you did not request this, you can ignore this email.");
  }

  @Override
  public void sendProviderInviteEmail(String toEmail, String organizationName, String inviteLink) {
    String org = organizationName != null && !organizationName.isBlank() ? organizationName : "your organization";
    send(toEmail, "You've been invited to ScholarMatch",
        "You've been invited to join ScholarMatch as a provider for " + org + ".\n\n"
            + "Set up your account by opening this link:\n\n" + inviteLink
            + "\n\nThis link expires in 7 days.");
  }

  private void send(String toEmail, String subject, String body) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new ApiException(HttpStatus.BAD_GATEWAY,
          "Email is not configured. Set SENDGRID_API_KEY when MAIL_MODE=sendgrid.");
    }

    String payload = toJson(Map.of(
        "personalizations", List.of(Map.of("to", List.of(Map.of("email", toEmail)))),
        "from", Map.of("email", fromAddress),
        "subject", subject,
        "content", List.of(Map.of("type", "text/plain", "value", body))));

    HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl))
        .header("Authorization", "Bearer " + apiKey)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .build();

    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        log.warn("SendGrid rejected email to {} with status {}: {}", toEmail, response.statusCode(), response.body());
        throw new ApiException(HttpStatus.BAD_GATEWAY,
            "SendGrid could not send the email. Check SENDGRID_API_KEY and verify MAIL_FROM in SendGrid.");
      }
    } catch (IOException e) {
      log.warn("Unable to send email to {} through SendGrid", toEmail, e);
      throw new ApiException(HttpStatus.BAD_GATEWAY, "Unable to reach SendGrid to send the email.");
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(HttpStatus.BAD_GATEWAY, "Email delivery was interrupted. Please try again.");
    }
  }

  private String toJson(Map<String, Object> payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to build SendGrid email payload", e);
    }
  }
}
