package com.mitchtalmadge.uofu_cs_bot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  private JavaMailSender javaMailSender;
  private LogService logService;

  @Autowired
  public EmailService(JavaMailSender javaMailSender, LogService logService) {
    this.javaMailSender = javaMailSender;
    this.logService = logService;
  }

  /**
   * Sends an email to a recipient with a custom subject and body.
   *
   * @param recipient The email address of the recipient.
   * @param subject The subject of the email.
   * @param body The body of the email.
   * @throws MailException If something goes wrong while sending.
   */
  public void sendEmail(String recipient, String subject, String body) throws MailException {
    this.logService.logInfo(
        getClass(), "Sending email to " + recipient + " w/ Subject: " + subject);
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(recipient);
    message.setFrom(System.getenv("SMTP_USER"));
    message.setSubject(subject);
    message.setText(body);

    this.javaMailSender.send(message);
    this.logService.logInfo(getClass(), "Email sent!");
  }
}
