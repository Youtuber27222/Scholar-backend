package com.scholarmatch.api.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.mail.mode", havingValue = "log", matchIfMissing = true)
public class LoggingMailService implements MailService {

  private static final Logger log = LoggerFactory.getLogger(LoggingMailService.class);

  @Override
  public void sendVerificationEmail(String toEmail, String otp) {
    log.info("[dev mail] Verification code for {}: {}", toEmail, otp);
  }

  @Override
  public void sendPasswordResetEmail(String toEmail, String resetLink) {
    log.info("[dev mail] Password reset link for {}: {}", toEmail, resetLink);
  }

  @Override
  public void sendProviderInviteEmail(String toEmail, String organizationName, String inviteLink) {
    log.info("[dev mail] Provider invite ({}) for {}: {}", organizationName, toEmail, inviteLink);
  }
}
