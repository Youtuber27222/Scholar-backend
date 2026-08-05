package com.scholarmatch.api.mail;

public interface MailService {

  void sendVerificationEmail(String toEmail, String otp);

  void sendPasswordResetEmail(String toEmail, String resetLink);

  void sendProviderInviteEmail(String toEmail, String organizationName, String inviteLink);
}
