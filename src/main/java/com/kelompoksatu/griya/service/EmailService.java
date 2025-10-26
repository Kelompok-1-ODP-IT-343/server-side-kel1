package com.kelompoksatu.griya.service;

import jakarta.mail.internet.MimeMessage;
import jakarta.validation.constraints.NotEmpty;
import java.time.Year;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/** Service class for email operations */
@Service
public class EmailService {

  private final JavaMailSender mailSender;

  @Value("${app.mail.verificationBaseUrl}")
  private String baseUrl;

  @Value("${app.mail.resetPasswordUrl}")
  private String resetPasswordUrl;

  @Autowired
  public EmailService(JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  // ========================================
  // EMAIL SENDING OPERATIONS
  // ========================================

  /** Send email verification to user */
  @SneakyThrows
  public void sendEmailVerification(String to, String token) {
    String subject = "Verify your email";
    String verificationUrl = baseUrl + token;
    String bodyFormatted = generateEmailVerificationTemplate(to, verificationUrl);

    sendHtmlEmail(to, subject, bodyFormatted);
  }

  /** Send forgot password email to user */
  @SneakyThrows
  public void sendEmailForgotPassword(
      @NotEmpty(message = "Email is required") String email, String token, long expiresInMinutes) {
    String subject = "Reset Password";
    String forgotPasswordUrl = resetPasswordUrl + token;
    String bodyFormatted = generateForgotPasswordTemplate(forgotPasswordUrl, expiresInMinutes);

    sendHtmlEmail(email, subject, bodyFormatted);
  }

  // ========================================
  // PRIVATE EMAIL TEMPLATE METHODS
  // ========================================

  private String generateEmailVerificationTemplate(String to, String verificationUrl) {
    String template =
        """
        <!DOCTYPE html>
        <html>
          <head>
            <meta charset="UTF-8">
            <title>Email Verification</title>
          </head>
          <body style="font-family: Arial, sans-serif; background-color: #f7f7f7; padding: 20px;">
            <table width="100%%" cellpadding="0" cellspacing="0" style="max-width: 600px; margin: auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 6px rgba(0,0,0,0.1);">
              <tr>
                <td style="background-color: #FF6040; padding: 16px; text-align: center;">
                  <h1 style="color: #ffffff; margin: 0;">Verify Your Email</h1>
                </td>
              </tr>
              <tr>
                <td style="padding: 24px;">
                  <p style="font-size: 16px; color: #333;">Hello <b>%s</b>,</p>
                  <p style="font-size: 15px; color: #555;">
                    Thank you for registering with us! To complete your sign-up and activate your account,
                    please confirm your email address by clicking the button below:
                  </p>
                  <p style="text-align: center; margin: 32px 0;">
                    <a href="%s"
                       style="background-color: #FF6040; color: #fff; text-decoration: none;\s
                              padding: 12px 24px; border-radius: 6px; font-size: 16px; display: inline-block;">
                      Verify Email
                    </a>
                  </p>
                  <p style="font-size: 13px; color: #999; margin-top: 24px;">
                    If you did not sign up for this account, you can safely ignore this email.
                  </p>
                </td>
              </tr>
              <tr>
                <td style="background-color: #f1f1f1; padding: 12px; text-align: center; font-size: 12px; color: #666;">
                  &copy; %d Satu Atap. All rights reserved.
                </td>
              </tr>
            </table>
          </body>
        </html>
        """;

    return String.format(
        template,
        to, // %s = username
        verificationUrl, // %s = link button
        Year.now().getValue() // %d = year
        );
  }

  private String generateForgotPasswordTemplate(String forgotPasswordUrl, long expiresInMinutes) {
    String template =
        """
        <!DOCTYPE html>
        <html lang="id">
        <head>
          <meta charset="UTF-8">
          <title>Reset Password</title>
        </head>
        <body style="font-family: Arial, sans-serif; background-color: #f5f5f5; padding:20px;">
          <div style="max-width: 500px; margin:auto; background:#ffffff; padding:20px; border-radius:8px;">
            <h2 style="color:#333;">Reset Password</h2>
            <p>Kami menerima permintaan untuk mereset password akun Anda. Klik tombol di bawah untuk melanjutkan:</p>

            <p style="text-align:center; margin:30px 0;">
              <a href="%s"
                 style="background-color:#007bff; color:#ffffff; padding:12px 20px;
                        border-radius:6px; text-decoration:none; display:inline-block;">
                Reset Password
              </a>
            </p>

        <p style="font-size:12px; color:#777;">
              Tautan ini akan kedaluwarsa dalam %d menit. Jika Anda tidak meminta reset password, abaikan email ini.
            </p>

            <p style="font-size:12px; color:#777;">© %d Satu Atap</p>
          </div>
        </body>
        </html>
        """;

    return String.format(
        template,
        forgotPasswordUrl, // %s kedua → link tombol
        expiresInMinutes, // %d menit expired
        Year.now().getValue() // %d tahun sekarang
        );
  }

  // ========================================
  // PRIVATE UTILITY METHODS
  // ========================================

  @SneakyThrows
  private void sendHtmlEmail(String to, String subject, String htmlBody) {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(htmlBody, true);

    mailSender.send(message);
  }
}
