package com.scholarmatch.api.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.mode", havingValue = "smtp")
public class SmtpMailService implements MailService {

  private final JavaMailSender mailSender;
  private final String fromAddress;

  public SmtpMailService(JavaMailSender mailSender, @Value("${app.mail.from}") String fromAddress) {
    this.mailSender = mailSender;
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
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(toEmail);
    message.setSubject(subject);
    message.setText(body);
    mailSender.send(message);
  }
}
