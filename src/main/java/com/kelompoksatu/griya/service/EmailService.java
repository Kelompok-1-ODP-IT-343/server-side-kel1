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
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Email Verification</title>
          <style>
              body {
                  font-family: Arial, sans-serif;
                  background-color: #f4f4f4;
                  margin: 0;
                  padding: 0;
              }
              .email-container {
                  max-width: 600px;
                  margin: 0 auto;
                  background-color: #ffffff;
                  padding: 20px;
                  border-radius: 10px;
                  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
              }
              .email-header {
                  text-align: center;
                  margin-bottom: 20px;
              }
              .email-header h1 {
                  color: #333333;
              }
              .email-content {
                  margin-bottom: 30px;
                  color: #555555;
                  line-height: 1.6;
              }
              .verify-button {
                  display: inline-block;
                  padding: 10px 20px;
                  font-size: 16px;
                  color: #ffffff;
                  background-color: #28a745;
                  text-decoration: none;
                  border-radius: 5px;
              }
              .email-footer {
                  text-align: center;
                  margin-top: 20px;
                  color: #999999;
                  font-size: 12px;
              }
          </style>
        </head>
        <body>
          <div class="email-container">
              <div class="email-header">
                  <h1>Email Verification</h1>
              </div>
              <div class="email-content">
                  <p>Hi, %s,</p>
                  <p>Thank you for registering on our website. To complete your registration, please verify your email address by clicking the button below:</p>
                  <p style="text-align: center;">
                      <a href="%s" class="verify-button">Verify Email</a>
                  </p>
                  <p>If you did not register on our website, please ignore this email.</p>
              </div>
              <div class="email-footer">
                  <p>© %d Satu Atap. All rights reserved.</p>
              </div>
          </div>
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
    helper.setFrom("adityarizky.ramadhan@clips.id"); // Set the sender email
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(htmlBody, true);

    mailSender.send(message);
  }
}
