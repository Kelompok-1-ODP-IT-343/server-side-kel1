package com.kelompoksatu.griya.service;

import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.verificationBaseUrl}")
    private String baseUrl;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @SneakyThrows
    public void sendEmailVerification(String to, String token) {
        String subject = "Verify your email";
        String verificationUrl = baseUrl + token;
        String body = """
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
                          &copy; %d Your Company. All rights reserved.
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
               \s""";
        String bodyFormatted = String.format(
                body,
                to,                      // %s = username
                verificationUrl,         // %s = link button
                java.time.Year.now().getValue() // %d = year
        );

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(bodyFormatted, true);

        mailSender.send(message);
    }
}
